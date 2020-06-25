package com.identicum.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;

public class RestConfiguration {

	public static final String AUTH_NONE = "NONE";
	public static final String AUTH_OAUTH = "OAUTH";

	private static final Logger logger = Logger.getLogger(RestConfiguration.class);

	private String baseUrl;
	private Integer maxConnections;
	private String authType;
	private String clientId;

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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public RestConfiguration(MultivaluedHashMap<String, String> keycloakConfig) {
		this.baseUrl = keycloakConfig.getFirst("baseURL");
		logger.infov("Loaded baseURL from module properties: {0}", this.baseUrl);
		if(this.baseUrl.endsWith("/")) {
			this.baseUrl = this.baseUrl.substring(0, this.baseUrl.length()-1);
			logger.infov("Removing trailing slash from URL: {0}", this.baseUrl);
		}

		this.maxConnections = Integer.parseInt(keycloakConfig.getFirst("maxHttpConnections"));
		logger.infov("Loaded maxHttpConnections from module properties: {0}", maxConnections);

		this.authType = keycloakConfig.getFirst("authType");
		logger.infov("Loaded authType from module properties: {0}", this.authType);

		this.clientId = keycloakConfig.getFirst("clientId");
		logger.infov("Loaded clientId from module properties: {0}", this.clientId);
	}
}
