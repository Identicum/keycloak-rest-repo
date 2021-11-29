package com.identicum.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;
import java.util.Timer;

import static com.identicum.keycloak.HttpStats.TO_MILLISECONDS;
import static com.identicum.keycloak.RestConfiguration.API_CONNECTION_REQUEST_TIMEOUT;
import static com.identicum.keycloak.RestConfiguration.API_CONNECT_TIMEOUT;
import static com.identicum.keycloak.RestConfiguration.API_SOCKET_TIMEOUT;
import static com.identicum.keycloak.RestConfiguration.AUTH_BASIC;
import static com.identicum.keycloak.RestConfiguration.AUTH_NONE;
import static com.identicum.keycloak.RestConfiguration.AUTH_OAUTH;
import static com.identicum.keycloak.RestConfiguration.HTTP_STATS_INTERVAL;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_AUTH_TYPE;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_BASE_URL;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_BASIC_PASSWORD;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_BASIC_USERNAME;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_MAX_HTTP_CONNECTIONS;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_OAUTH_CLIENT_ID;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_OAUTH_CLIENT_SECRET;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_OAUTH_SCOPE;
import static com.identicum.keycloak.RestConfiguration.PROPERTY_OAUTH_TOKEN_ENDPOINT;
import static com.identicum.keycloak.RestConfiguration.validate;
import static org.jboss.logging.Logger.getLogger;
import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.PASSWORD;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class KeycloakRestRepoProviderFactory implements UserStorageProviderFactory<KeycloakRestRepoProvider> {

	private static final Logger logger = getLogger(KeycloakRestRepoProviderFactory.class);
	private List<ProviderConfigProperty> configMetadata;

	private MultivaluedHashMap<String, String> lastConfiguration = null;

	private RestHandler restHandler;
	private Timer httpStats;

	@Override
	public void init(Scope config) {
		logger.infov("Initializing Keycloak Rest Repo factory version: " + getClass().getPackage().getImplementationVersion());

		ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();
		builder.property().name(PROPERTY_BASE_URL)
				.type(STRING_TYPE).label("Base URL")
				.defaultValue("http://rest-users-api:8081/")
				.helpText("Api url base to authenticate users")
				.add();
		builder.property().name(PROPERTY_MAX_HTTP_CONNECTIONS)
				.type(STRING_TYPE).label("Max pool connections")
				.defaultValue("5")
				.helpText("Max http connections in pool")
				.add();
		builder.property().name(PROPERTY_AUTH_TYPE)
				.type(LIST_TYPE).label("Api Authorization")
				.options(AUTH_NONE, AUTH_BASIC, AUTH_OAUTH)
				.defaultValue(AUTH_NONE)
				.helpText("Authorization method used by consumed API")
				.add();
		builder.property().name(PROPERTY_OAUTH_TOKEN_ENDPOINT)
				.type(STRING_TYPE).label("OAuth2 Token Endpoint")
				.defaultValue("http://localhost:8080/auth/realms/restrepo/protocol/openid-connect/token")
				.helpText("Endpoint to negotiate the token with client_credentials grant type (required for OAUTH authorization)")
				.add();
		builder.property().name(PROPERTY_OAUTH_CLIENT_ID)
				.type(STRING_TYPE).label("OAuth2 Client Id")
				.defaultValue("")
				.helpText("client_id to negotiate the Access Token (required for OAUTH authorization)")
				.add();
		builder.property().name(PROPERTY_OAUTH_CLIENT_SECRET)
				.type(PASSWORD).label("OAuth2 Client Secret")
				.defaultValue("")
				.helpText("client_secret to negotiate the Access Token (required for OAUTH authorization)")
				.add();
		builder.property().name(PROPERTY_OAUTH_SCOPE)
				.type(STRING_TYPE).label("OAuth2 Scope")
				.defaultValue("")
				.helpText("Required scope in the access_token request")
				.add();
		builder.property().name(PROPERTY_BASIC_USERNAME)
				.type(STRING_TYPE).label("Auth Basic Username")
				.defaultValue("")
				.helpText("Username used for Basic Authentication")
				.add();
		builder.property().name(PROPERTY_BASIC_PASSWORD)
				.type(PASSWORD).label("Auth Basic Password")
				.defaultValue("")
				.helpText("Password used for Basic Authentication")
				.add();
		builder.property().name(API_SOCKET_TIMEOUT)
				.type(STRING_TYPE).label("API Socket Timeout")
				.defaultValue("1000")
				.helpText("Max time [milliseconds] to wait for response")
				.add();
		builder.property().name(API_CONNECT_TIMEOUT)
				.type(STRING_TYPE).label("API Connect Timeout")
				.defaultValue("1000")
				.helpText("Max time [milliseconds] to establish the connection")
				.add();
		builder.property().name(API_CONNECTION_REQUEST_TIMEOUT)
				.type(STRING_TYPE).label("API Connection Request Timeout")
				.defaultValue("1000")
				.helpText("Max time [milliseconds] to wait until a connection in the pool is assigned to the requesting thread")
				.add();
		builder.property().name(HTTP_STATS_INTERVAL)
				.type(STRING_TYPE).label("HttpStats interval")
				.defaultValue("0")
				.helpText("How often [seconds] will the HTTP connection pool stats be displayed. 0 means disabled.")
				.add();
		configMetadata = builder.build();
		this.httpStats = new Timer();
	}

	@Override
	public KeycloakRestRepoProvider create(KeycloakSession session, ComponentModel model) {
		if(restHandler == null || !model.getConfig().equals( lastConfiguration )) {
			logger.infov("Creating a new instance of restHandler");
			RestConfiguration restConfiguration = new RestConfiguration(model.getConfig());
			restHandler = new RestHandler(restConfiguration);
			setHttpStats(logger, restHandler, restConfiguration.getHttpStatsInterval());
			lastConfiguration = model.getConfig();
		} else {
			logger.infov("RestHandler already instantiated");
		}
		return new KeycloakRestRepoProvider(session, model, restHandler);
	}

	@Override
	public String getId() {
		return "rest-repo-provider";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configMetadata;
	}

	@Override
	public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
		validate(config.getConfig());
	}

	private void setHttpStats(Logger logger, RestHandler restHandler, Integer httpStatsInterval){
		httpStats.cancel();
		if(httpStatsInterval > 0){
			this.httpStats = new Timer();
			this.httpStats.schedule(new HttpStats(logger, restHandler), 0, httpStatsInterval * TO_MILLISECONDS);
		}
	}
}
