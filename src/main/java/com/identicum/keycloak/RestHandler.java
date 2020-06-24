package com.identicum.keycloak;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;

public class RestHandler {

	private static final Logger logger = Logger.getLogger(RestHandler.class);
	protected CloseableHttpClient httpClient;

	private String baseURL;

	public RestHandler(String baseURL, int maxConnections) {
		logger.infov("Initializing HttpClient pool.");
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setDefaultMaxPerRoute(maxConnections);
		this.httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).build();
		this.baseURL = baseURL;
	}

	public String getBaseURL() {
		return this.baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public boolean authenticate(String username, String password) {
		logger.infov("Authenticating user: {0}", username);
		HttpPost httpPost = new HttpPost(this.getBaseURL() + "/authenticate");
		JsonObject json = Json.createObjectBuilder()
				.add("username", username)
				.add("password", password)
				.build();

		CloseableHttpResponse response = null;
		try {
			StringEntity entity = new StringEntity(json.toString());
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);

			response = this.httpClient.execute(httpPost);
			logger.infov("Status received authenticating user {0}: {1}", username, response.getStatusLine().getStatusCode());
			// I have to consume the entity to apply the Keep-Alive header. For some reason it is not applied if I don't read the entity
			EntityUtils.consume(response.getEntity());
			return response.getStatusLine().getStatusCode() == 200;
		}
		catch(IOException io) {
			logger.errorv("Failed authentication of user {0}", username, io);
			return false;
		}
		finally {
			this.closeQuietly(response);
		}
	}

	public JsonObject findUserByUsername(String username) {
		logger.infov("Thread id {0} - Searching user: {1}", Thread.currentThread().getId(), username);
		HttpGet httpGet = new HttpGet(this.getBaseURL() + "/users/" + username);
		httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
		CloseableHttpResponse response = null;
		try {
			response = this.httpClient.execute(httpGet);
			this.analyzeResponse(response);

			String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
			JsonReader reader = Json.createReader(new StringReader(responseString));
			return reader.readObject();
		}
		catch(IOException io) {
			throw new RuntimeException("Error getting user " + username, io);
		}
		finally {
			this.closeQuietly(response);
		}
	}

	private void analyzeResponse(HttpResponse response) {
		logger.infov("Value received from http request: {0}", response.getStatusLine().getStatusCode());
	}

	public void close() {
		//this.httpClient.close();
	}

	public void setUserStatus(String username, boolean active) {
		logger.infov("Setting user inactive: {0}", username);

		HttpPatch httpPatch = new HttpPatch(this.getBaseURL() + "/users/" + username);
		httpPatch.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
		httpPatch.setHeader("Content-Type", "application/json");
		CloseableHttpResponse response = null;

		String body = "{ \"active\": " + String.valueOf(active) + "}";
		logger.infov("Setting patch body as: {0}", body);

		try {
			HttpEntity httpEntity = new ByteArrayEntity(body.getBytes("UTF-8"));
			httpPatch.setEntity(httpEntity);
			response = this.httpClient.execute(httpPatch);
			this.analyzeResponse(response);

			logger.infov("Status received setting inactive user {0}: {1}", username, response.getStatusLine().getStatusCode());
			// I have to consume the entity to apply the Keep-Alive header. For some reason it is not applied if I don't read the entity
			EntityUtils.consume(response.getEntity());
		}
		catch(IOException io) {
			throw new RuntimeException("Error getting user " + username, io);
		}
		finally {
			this.closeQuietly(response);
		}
	}

	public JsonArray findUsers(String username) {
		logger.infov("Finding users with username: {0}", username);

		String searchUrl = this.getBaseURL() + "/users";
		if(username != null) {
			searchUrl += "?username=" + username;
		}

		logger.infov("Using url {0} to search users", searchUrl);
		HttpGet httpGet = new HttpGet(searchUrl);
		httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
		CloseableHttpResponse response = null;
		try {
			response = this.httpClient.execute(httpGet);
			logger.infov("Response received: {0}", response);

			this.analyzeResponse(response);

			logger.infov("Reading response");
			String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
			JsonReader reader = Json.createReader(new StringReader(responseString));
			return reader.readArray();
		}
		catch(IOException io) {
			logger.error("Error calling GET to find users", io);
			throw new RuntimeException("Error getting user " + username, io);
		}
		finally {
			this.closeQuietly(response);
		}
	}

	private void closeQuietly(CloseableHttpResponse response) {
		if (response != null)
			try {
				response.close();
			} catch (IOException io) {
				logger.warn("Error closing http response", io);
			}
	}
}