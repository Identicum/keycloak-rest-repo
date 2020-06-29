package com.identicum.keycloak;

import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;

import javax.json.*;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RestHandler {

	private static final Logger logger = Logger.getLogger(RestHandler.class);
	protected CloseableHttpClient httpClient;

	private final RestConfiguration configuration;

	private String basicToken;
	private String accessToken;
	private String refreshToken;
	private Date tokenExpiresAt;

	public RestHandler(RestConfiguration configuration) {
		logger.infov("Initializing HttpClient pool.");
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setDefaultMaxPerRoute(configuration.getMaxConnections());
		this.httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).build();
		this.configuration = configuration;
	}

	public boolean authenticate(String username, String password) {
		logger.infov("Authenticating user: {0}", username);
		HttpPost httpPost = new HttpPost(this.configuration.getBaseUrl() + "/authenticate");
		httpPost.setHeader(HttpHeaders.ACCEPT, "application/json");
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		httpPost.setHeader(HttpHeaders.CONNECTION, HTTP.CONN_KEEP_ALIVE);

		JsonObject json = Json.createObjectBuilder()
				.add("username", username)
				.add("password", password)
				.build();
		HttpEntity entity = new ByteArrayEntity(json.toString().getBytes());
		httpPost.setEntity(entity);

		SimpleHttpResponse response = this.executeCall(httpPost);
		return response.isSuccess();
	}

	public JsonObject findUserByUsername(String username) {
		logger.infov("Finding user by username: {0}", username);
		HttpGet httpGet = new HttpGet(this.configuration.getBaseUrl() + "/users/" + username);
		SimpleHttpResponse response = this.executeSecuredCall(httpGet);
		if(response.isSuccess()) {
			return response.getResponseAsJsonObject();
		}
		else {
			return null;
		}
	}

	public void setUserAttribute(String username, String attribute, String value) {
		logger.infov("Setting user {0} attribute {1}: {2}", username, attribute, value);

		HttpPatch httpPatch = new HttpPatch(this.configuration.getBaseUrl() + "/users/" + username);
		httpPatch.setHeader("Content-Type", "application/json");
		JsonObject requestJson = Json.createObjectBuilder().add(attribute, value).build();
		logger.infov("Setting patch body as: {0}", requestJson.toString());

		HttpEntity httpEntity = new ByteArrayEntity(requestJson.toString().getBytes());
		httpPatch.setEntity(httpEntity);

		this.stopOnError( this.executeSecuredCall(httpPatch) );
	}

	public JsonArray findUsers(String username) {
		logger.infov("Finding users with username: {0}", username);
		String searchUrl = this.configuration.getBaseUrl() + "/users";
		if(username != null) {
			searchUrl += "?username=" + username;
		}
		logger.infov("Using url {0} to search users", searchUrl);
		HttpGet httpGet = new HttpGet(searchUrl);
		SimpleHttpResponse response = this.executeSecuredCall(httpGet);
		this.stopOnError(response);
		return response.getResponseAsJsonArray();
	}

	public JsonObject createUser(String username) {
		logger.infov("Creating user {0}", username);

		HttpPost httpPost = new HttpPost(this.configuration.getBaseUrl() + "/users");
		httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
		httpPost.setHeader("Content-Type", "application/json");

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("username", username);
		builder.add("email", "temporary@localhost.com");
		builder.add("firstName", "TempFirstName");
		builder.add("lastName", "TempLastName");
		builder.add("password", RestUserAdapter.randomPassword());
		builder.add("active", Boolean.TRUE);

		JsonObject requestJson = builder.build();
		logger.infov("Setting create body as: {0}", requestJson.toString());
		HttpEntity httpEntity = new ByteArrayEntity(requestJson.toString().getBytes());
		httpPost.setEntity(httpEntity);
		
		SimpleHttpResponse response = this.executeSecuredCall(httpPost);
		this.stopOnError(response);
		return response.getResponseAsJsonObject();
	}

	public void deleteUser(String username) {
		logger.infov("Deleting user {0}", username);
		HttpDelete httpDelete = new HttpDelete(this.configuration.getBaseUrl() + "/users/" + username);
		this.stopOnError( this.executeSecuredCall(httpDelete) );
	}

	/* ------------------------------------------------------------------------ */
	/* HTTP calls handlers                                                      */
	/* ------------------------------------------------------------------------ */

	/**
	 * Close quietly a http response
	 * @param response Response to be closed
	 */
	private void closeQuietly(CloseableHttpResponse response) {
		if (response != null)
			try {
				response.close();
			} catch (IOException io) {
				logger.warn("Error closing http response", io);
			}
	}

	/**
	 * Execute http request throw the executeCall but adding custom Http Headers to include
	 * authorization credentials.
	 *
	 * @param request Http request to be executed
	 * @return SimpleHttpResponse with status code and response body
	 */
	private SimpleHttpResponse executeSecuredCall(HttpRequestBase request) {
		switch (this.configuration.getAuthType()) {
			case RestConfiguration.AUTH_OAUTH:
				request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.getAccessToken());
				break;
			case RestConfiguration.AUTH_BASIC:
				request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.getBasicAuthenticationToken());
				break;
		}
		return this.executeCall(request);
	}

	/**
	 * Execute http request with the connection pool and handle the received response.
	 * If the response status is not OK it throws a {@link RuntimeException} to stop the flow.
	 *
	 * @param request Request to be executed with all needed headers.
	 * @return SimpleHttpResponse with code received and body
	 * @throws RuntimeException if status code received is not 200
	 */
	private SimpleHttpResponse executeCall(HttpRequestBase request) {
		logger.debugv("Executing Http Request [{0}] on [{1}]", request.getMethod(), request.getURI());
		request.setHeader(HttpHeaders.CONNECTION, HTTP.CONN_KEEP_ALIVE);

		Stream.of( request.getAllHeaders() ).forEach(header -> logger.debugv("Request header: {0} -> {1}", header.getName(), header.getValue() ));
		CloseableHttpResponse response = null;
		try {
			response = this.httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
			logger.debugv("Response code obtained from server: {0}", response.getStatusLine().getStatusCode());
			logger.debugv("Response body obtained from server: {0}", responseString);
			return new SimpleHttpResponse(response.getStatusLine().getStatusCode(), responseString);
		}
		catch(IOException io) {
			throw new RuntimeException("Error executing request", io);
		}
		finally {
			this.closeQuietly(response);
		}
	}

	private String getBasicAuthenticationToken() {
		if(this.basicToken == null) {
			this.basicToken = this.configuration.getBasicUsername() + ":" + this.configuration.getBasicPassword();
			this.basicToken = Base64.getEncoder().encodeToString( this.basicToken.getBytes() );
		}
		return this.basicToken;
	}

	private String getAccessToken() {
		if(this.accessToken == null) {
			logger.debug("Requesting access token");
			this.requestAccessToken();
		}
		else {
			logger.debugv("Current access_token expires at {0} / Current time {1}", this.tokenExpiresAt, new Date());
			if( this.tokenExpiresAt.before( new Date())) {
				logger.debug("Refreshing access token");
				try {
					this.refreshAccessToken();
				}
				catch(RuntimeException re) {
					logger.error("Error refreshing access token. Trying to generate a new one", re);
					this.requestAccessToken();
				}
			}
		}
		return this.accessToken;
	}

	private void requestAccessToken() {
		RealmModel realm = this.configuration.getContext().getRealm();
		logger.infov("Using realm to request access token: {0}", realm);
		logger.infov("Current client_id: {0}", this.configuration.getClientId());
		ClientModel client = realm.getClientByClientId(this.configuration.getClientId());
		String endpoint = String.format("%s/realms/%s/protocol/openid-connect/token", this.configuration.getContext().getAuthServerUrl(), realm.getName());

		logger.infov("Requesting access_token to consume Rest User API: {0}", endpoint);
		HttpPost httpPost = new HttpPost(endpoint);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("grant_type", "client_credentials"));
		form.add(new BasicNameValuePair("client_id", client.getClientId()));
		form.add(new BasicNameValuePair("client_secret", client.getSecret()));
		form.add(new BasicNameValuePair("scope", "profile"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
		httpPost.setEntity(entity);

		SimpleHttpResponse response = this.executeCall(httpPost);
		this.stopOnError(response);
		JsonObject jsonResponse = response.getResponseAsJsonObject();
		this.accessToken = jsonResponse.getString("access_token");
		this.refreshToken = jsonResponse.getString("refresh_token");
		this.tokenExpiresAt = new Date(System.currentTimeMillis() + jsonResponse.getInt("expires_in") * 1000);
	}

	private void refreshAccessToken() {
		RealmModel realm = this.configuration.getContext().getRealm();
		ClientModel client = realm.getClientByClientId(this.configuration.getClientId());
		String endpoint = String.format("%s/realms/%s/protocol/openid-connect/token", this.configuration.getContext().getAuthServerUrl(), realm.getName());

		logger.infov("Requesting access_token to consume Rest User API: {0}", endpoint);
		HttpPost httpPost = new HttpPost(endpoint);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("grant_type", "refresh_token"));
		form.add(new BasicNameValuePair("client_id", client.getClientId()));
		form.add(new BasicNameValuePair("client_secret", client.getSecret()));
		form.add(new BasicNameValuePair("refresh_token", this.refreshToken));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
		httpPost.setEntity(entity);

		SimpleHttpResponse response = this.executeCall(httpPost);
		this.stopOnError(response);
		JsonObject jsonResponse = response.getResponseAsJsonObject();
		this.accessToken = jsonResponse.getString("access_token");
		this.refreshToken = jsonResponse.getString("refresh_token");
		this.tokenExpiresAt = new Date(System.currentTimeMillis() + jsonResponse.getInt("expires_in") * 1000);
	}

	private void stopOnError(SimpleHttpResponse response) {
		if(!response.isSuccess()) {
			logger.debugv("Response status code was not success. Code received: {0}", response.getStatus());
			logger.debugv("Response received: {0}", response.getResponse());
			throw new RuntimeException("Http Request was not success. Check logs to get more information");
		}
	}
}