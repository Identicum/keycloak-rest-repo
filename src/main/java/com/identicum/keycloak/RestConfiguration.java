package com.identicum.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentValidationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
	public static final String PROPERTY_STATS_ENABLED = "statsEnabled";

	public static final String API_SOCKET_TIMEOUT = "apiSocketTimeout";
	public static final String API_CONNECT_TIMEOUT = "apiConnectTimeout";
	public static final String API_CONNECTION_REQUEST_TIMEOUT = "apiConnectionRequestTimeout";

	public static final String STATS_ENABLED_YES = "Yes";
	public static final String STATS_ENABLED_NO = "No";

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
	private String statsEnabled;
	private Integer apiSocketTimeout;
	private Integer apiConnectTimeout;
	private Integer apiConnectionRequestTimeout;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Integer getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	public String getOauthClientId() {
		return oauthClientId;
	}

	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}

	public String getOauthScope() {
		return oauthScope;
	}

	public void setOauthScope(String oauthScope) {
		this.oauthScope = oauthScope;
	}

	public String getOauthClientSecret() {
		return oauthClientSecret;
	}

	public void setOauthClientSecret(String oauthClientSecret) {
		this.oauthClientSecret = oauthClientSecret;
	}

	public String getOauthTokenEndpoint() {
		return oauthTokenEndpoint;
	}

	public void setOauthTokenEndpoint(String oauthTokenEndpoint) {
		this.oauthTokenEndpoint = oauthTokenEndpoint;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getBasicUsername() {
		return basicUsername;
	}

	public void setBasicUsername(String basicUsername) {
		this.basicUsername = basicUsername;
	}

	public String getBasicPassword() {
		return basicPassword;
	}

	public void setBasicPassword(String basicPassword) {
		this.basicPassword = basicPassword;
	}

	public String getStatsEnabled() {
		return statsEnabled;
	}

	public void setStatsEnabled(String statsEnabled) {
		this.statsEnabled = statsEnabled;
	}

	public Integer getApiSocketTimeout() {
		return apiSocketTimeout;
	}

	public void setApiSocketTimeout(Integer apiSocketTimeout) {
		this.apiSocketTimeout = apiSocketTimeout;
	}

	public Integer getApiConnectTimeout() {
		return apiConnectTimeout;
	}

	public void setApiConnectTimeout(Integer apiConnectTimeout) {
		this.apiConnectTimeout = apiConnectTimeout;
	}

	public Integer getApiConnectionRequestTimeout() {
		return apiConnectionRequestTimeout;
	}

	public void setApiConnectionRequestTimeout(Integer apiConnectionRequestTimeout) {
		this.apiConnectionRequestTimeout = apiConnectionRequestTimeout;
	}

	public RestConfiguration(MultivaluedHashMap<String, String> keycloakConfig) {
		this.baseUrl = keycloakConfig.getFirst(PROPERTY_BASE_URL);
		logger.infov("Loaded baseURL from module properties: {0}", this.baseUrl);
		if(this.baseUrl.endsWith("/")) {
			this.baseUrl = this.baseUrl.substring(0, this.baseUrl.length()-1);
			logger.infov("Removing trailing slash from URL: {0}", this.baseUrl);
		}

		this.maxConnections = Integer.parseInt(keycloakConfig.getFirst(PROPERTY_MAX_HTTP_CONNECTIONS));
		logger.infov("Loaded maxHttpConnections from module properties: {0}", maxConnections);

		this.authType = keycloakConfig.getFirst(PROPERTY_AUTH_TYPE);
		logger.infov("Loaded authType from module properties: {0}", this.authType);

		this.oauthClientId = keycloakConfig.getFirst(PROPERTY_OAUTH_CLIENT_ID);
		logger.infov("Loaded oauthClientId from module properties: {0}", this.oauthClientId);

		this.oauthClientSecret = keycloakConfig.getFirst(PROPERTY_OAUTH_CLIENT_SECRET);
		logger.infov("Loaded oauthClientId from module properties: {0}", this.oauthClientSecret);

		this.oauthTokenEndpoint = keycloakConfig.getFirst(PROPERTY_OAUTH_TOKEN_ENDPOINT);
		logger.infov("Loaded oauthClientId from module properties: {0}", this.oauthTokenEndpoint);

		this.oauthScope = keycloakConfig.getFirst(PROPERTY_OAUTH_SCOPE);
		logger.infov("Loaded oauthScope from module properties: {0}", this.oauthScope);

		this.basicUsername = keycloakConfig.getFirst(PROPERTY_BASIC_USERNAME);
		logger.infov("Loaded basicUsername from module properties: {0}", this.basicUsername);

		this.basicPassword = keycloakConfig.getFirst(PROPERTY_BASIC_PASSWORD);
		logger.infov("Loaded basicPassword from module properties: {0}", this.basicPassword);

		this.statsEnabled = keycloakConfig.getFirst(PROPERTY_STATS_ENABLED);
		logger.infov("Loaded statsEnabled from module properties: {0}", this.statsEnabled);

		this.apiSocketTimeout = Integer.parseInt(keycloakConfig.getFirst(API_SOCKET_TIMEOUT));
		logger.infov("Loaded apiSocketTimeout from module properties: {0}", apiSocketTimeout);

		this.apiConnectTimeout = Integer.parseInt(keycloakConfig.getFirst(API_CONNECT_TIMEOUT));
		logger.infov("Loaded apiConnectTimeout from module properties: {0}", apiConnectTimeout);

		this.apiConnectionRequestTimeout = Integer.parseInt(keycloakConfig.getFirst(API_CONNECTION_REQUEST_TIMEOUT));
		logger.infov("Loaded apiConnectionRequestTimeout from module properties: {0}", apiConnectionRequestTimeout);
	}

	public static void validate(MultivaluedHashMap<String, String> config) {
		String baseURL = config.getFirst(PROPERTY_BASE_URL);
		if (baseURL == null) throw new ComponentValidationException("BaseURL is not specified");
		try {
			URL url = new URL(baseURL);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
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
				URL url = new URL(config.getFirst(PROPERTY_OAUTH_TOKEN_ENDPOINT));
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
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
		buffer.append("baseUrl: " + this.baseUrl + "; ");
		buffer.append("maxConnections: " + this.maxConnections + "; ");
		buffer.append("authType: " + this.authType + "; ");
		buffer.append("oauthClientId: " + this.oauthClientId + "; ");
		buffer.append("oauthTokenEndpoint: " + this.oauthTokenEndpoint + "; ");
		buffer.append("oauthScope: " + this.oauthScope + "; ");
		buffer.append("basicUsername: " + this.basicUsername + "; ");
		buffer.append("statsEnabled: " + this.statsEnabled + "; ");
		buffer.append("apiSocketTimeout: " + this.apiSocketTimeout + "; ");
		buffer.append("apiConnectTimeout: " + this.apiConnectTimeout + "; ");
		buffer.append("apiConnectionRequestTimeout: " + this.statsEnabled);

		return buffer.toString();
	}
}
