package com.identicum.keycloak;

import lombok.Getter;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentValidationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Integer.parseInt;

@Getter
public class RestConfiguration {

	public static final String AUTH_NONE = "NONE";
	public static final String AUTH_BASIC = "BASIC";
	public static final String AUTH_OAUTH = "OAUTH";

	public static final String PROPERTY_BASE_URL = "baseURL";
	public static final String PROPERTY_MAX_HTTP_CONNECTIONS = "maxHttpConnections";
	public static final String PROPERTY_AUTH_TYPE = "authType";
	public static final String PROPERTY_OAUTH_CLIENT_ID = "oauthClientId";
	public static final String PROPERTY_OAUTH_CLIENT_SECRET = "oauthClientSecret";
	public static final String PROPERTY_OAUTH_SCOPE = "oauthScope";
	public static final String PROPERTY_OAUTH_TOKEN_ENDPOINT = "oauthTokenEndpoint";
	public static final String PROPERTY_BASIC_USERNAME = "basicUsername";
	public static final String PROPERTY_BASIC_PASSWORD = "basicPassword";

	public static final String API_SOCKET_TIMEOUT = "apiSocketTimeout";
	public static final String API_CONNECT_TIMEOUT = "apiConnectTimeout";
	public static final String API_CONNECTION_REQUEST_TIMEOUT = "apiConnectionRequestTimeout";
	public static final String HTTP_STATS_INTERVAL = "httpStatsInterval";

	private static final Logger logger = Logger.getLogger(RestConfiguration.class);

	private String baseUrl;
	private Integer maxConnections;
	private String authType;
	private String oauthClientId;
	private String oauthClientSecret;
	private String oauthScope;
	private String oauthTokenEndpoint;
	private String basicUsername;
	private String basicPassword;
	private Integer apiSocketTimeout;
	private Integer apiConnectTimeout;
	private Integer apiConnectionRequestTimeout;
	private Integer httpStatsInterval;

	public RestConfiguration(MultivaluedHashMap<String, String> keycloakConfig) {
		this.baseUrl = keycloakConfig.getFirst(PROPERTY_BASE_URL);
		logger.infov("Loaded baseURL from module properties: {0}", baseUrl);
		if(baseUrl.endsWith("/")) {
			this.baseUrl = baseUrl.substring(0, baseUrl.length()-1);
			logger.infov("Removing trailing slash from URL: {0}", baseUrl);
		}

		this.maxConnections = parseInt(keycloakConfig.getFirst(PROPERTY_MAX_HTTP_CONNECTIONS));
		logger.infov("Loaded maxHttpConnections from module properties: {0}", maxConnections);

		this.authType = keycloakConfig.getFirst(PROPERTY_AUTH_TYPE);
		logger.infov("Loaded authType from module properties: {0}", authType);

		this.oauthClientId = keycloakConfig.getFirst(PROPERTY_OAUTH_CLIENT_ID);
		logger.infov("Loaded oauthClientId from module properties: {0}", oauthClientId);

		this.oauthClientSecret = keycloakConfig.getFirst(PROPERTY_OAUTH_CLIENT_SECRET);
		logger.infov("Loaded oauthClientId from module properties: {0}", oauthClientSecret);

		this.oauthTokenEndpoint = keycloakConfig.getFirst(PROPERTY_OAUTH_TOKEN_ENDPOINT);
		logger.infov("Loaded oauthClientId from module properties: {0}", oauthTokenEndpoint);

		this.oauthScope = keycloakConfig.getFirst(PROPERTY_OAUTH_SCOPE);
		logger.infov("Loaded oauthScope from module properties: {0}", oauthScope);

		this.basicUsername = keycloakConfig.getFirst(PROPERTY_BASIC_USERNAME);
		logger.infov("Loaded basicUsername from module properties: {0}", basicUsername);

		this.basicPassword = keycloakConfig.getFirst(PROPERTY_BASIC_PASSWORD);
		logger.infov("Loaded basicPassword from module properties: {0}", basicPassword);

		this.apiSocketTimeout = parseInt(keycloakConfig.getFirst(API_SOCKET_TIMEOUT));
		logger.infov("Loaded apiSocketTimeout from module properties: {0}", apiSocketTimeout);

		this.apiConnectTimeout = parseInt(keycloakConfig.getFirst(API_CONNECT_TIMEOUT));
		logger.infov("Loaded apiConnectTimeout from module properties: {0}", apiConnectTimeout);

		this.apiConnectionRequestTimeout = parseInt(keycloakConfig.getFirst(API_CONNECTION_REQUEST_TIMEOUT));
		logger.infov("Loaded apiConnectionRequestTimeout from module properties: {0}", apiConnectionRequestTimeout);

		this.httpStatsInterval = parseInt(keycloakConfig.getFirst(HTTP_STATS_INTERVAL));
		logger.infov("Loaded httpStatsInterval from module properties: {0}", httpStatsInterval);
	}

	public static void validate(MultivaluedHashMap<String, String> config) {
		String baseURL = config.getFirst(PROPERTY_BASE_URL);
		if (baseURL == null) throw new ComponentValidationException("BaseURL is not specified");
		try {
			HttpURLConnection urlConn = (HttpURLConnection) new URL(baseURL).openConnection();
			urlConn.connect();
			urlConn.disconnect();
		} catch (IOException e) {
			throw new ComponentValidationException("Error accessing the base url", e);
		}

		String maxConnections = config.getFirst(PROPERTY_MAX_HTTP_CONNECTIONS);
		if(maxConnections == null || !maxConnections.matches("\\d*")) {
			logger.warn("maxHttpConnections property is not valid. Enter a valid number");
			throw new ComponentValidationException("Max pool connections should be a number");
		}

		if (config.getFirst(PROPERTY_AUTH_TYPE).equals( RestConfiguration.AUTH_OAUTH)) {
			logger.warn("Auth Type set to OAUTH2. Checking required fields");
			checkPropertyNotEmpty(config, PROPERTY_OAUTH_CLIENT_ID);
			checkPropertyNotEmpty(config, PROPERTY_OAUTH_CLIENT_SECRET);
			checkPropertyNotEmpty(config, PROPERTY_OAUTH_SCOPE);
			checkPropertyNotEmpty(config, PROPERTY_OAUTH_TOKEN_ENDPOINT);
			try {
				HttpURLConnection urlConn = (HttpURLConnection) new URL(config.getFirst(PROPERTY_OAUTH_TOKEN_ENDPOINT)).openConnection();
				urlConn.connect();
				urlConn.disconnect();
			} catch (IOException e) {
				throw new ComponentValidationException("Error accessing the token endpoint", e);
			}
		}

		if (config.getFirst(PROPERTY_AUTH_TYPE).equals( RestConfiguration.AUTH_BASIC)) {
			logger.warn("Auth Type set to Basic. Checking username and password");
			checkPropertyNotEmpty(config, PROPERTY_BASIC_USERNAME);
			checkPropertyNotEmpty(config, PROPERTY_BASIC_PASSWORD);
		}
	}

	private static void checkPropertyNotEmpty(MultivaluedHashMap<String, String> config, String propertyName) {
		String propertyValue = config.getFirst(propertyName);
		if( propertyValue == null || propertyValue.isEmpty()) {
			throw new ComponentValidationException("The " + propertyValue + " field is required");
		}
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("baseUrl: " + baseUrl + "; ");
		buffer.append("maxConnections: " + maxConnections + "; ");
		buffer.append("authType: " + authType + "; ");
		buffer.append("oauthClientId: " + oauthClientId + "; ");
		buffer.append("oauthTokenEndpoint: " + oauthTokenEndpoint + "; ");
		buffer.append("oauthScope: " + oauthScope + "; ");
		buffer.append("basicUsername: " + basicUsername + "; ");
		buffer.append("apiSocketTimeout: " + apiSocketTimeout + "; ");
		buffer.append("apiConnectTimeout: " + apiConnectTimeout + "; ");
		buffer.append("apiConnectionRequestTimeout: " + apiConnectionRequestTimeout);

		return buffer.toString();
	}
}
