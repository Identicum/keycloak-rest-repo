package com.identicum.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

public class KeycloakRestRepoProviderFactory implements UserStorageProviderFactory<KeycloakRestRepoProvider> {

	private static final Logger logger = Logger.getLogger(KeycloakRestRepoProviderFactory.class);
	protected static final List<ProviderConfigProperty> configMetadata;

	private MultivaluedHashMap<String, String> lastConfiguration = null;

	static {
		ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();
		builder.property().name(RestConfiguration.PROPERTY_BASE_URL)
			.type(ProviderConfigProperty.STRING_TYPE).label("Base URL")
			.defaultValue("http://rest-users-api:8081/")
			.helpText("Api url base to authenticate users")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_MAX_HTTP_CONNECTIONS)
			.type(ProviderConfigProperty.STRING_TYPE).label("Max pool connections")
			.defaultValue("5")
			.helpText("Max http connections in pool")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_AUTH_TYPE)
			.type(ProviderConfigProperty.LIST_TYPE).label("Api Authorization")
			.options(RestConfiguration.AUTH_NONE, RestConfiguration.AUTH_BASIC, RestConfiguration.AUTH_OAUTH)
			.defaultValue(RestConfiguration.AUTH_NONE)
			.helpText("Authorization method used by consumed API")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_OAUTH_TOKEN_ENDPOINT)
			.type(ProviderConfigProperty.STRING_TYPE).label("OAuth2 Token Endpoint")
			.defaultValue("http://localhost:8080/auth/realms/restrepo/protocol/openid-connect/token")
			.helpText("Endpoint to negotiate the token with client_credentials grant type (required for OAUTH authorization)")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_OAUTH_CLIENT_ID)
			.type(ProviderConfigProperty.STRING_TYPE).label("OAuth2 Client Id")
			.defaultValue("")
			.helpText("client_id to negotiate the Access Token (required for OAUTH authorization)")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_OAUTH_CLIENT_SECRET)
			.type(ProviderConfigProperty.PASSWORD).label("OAuth2 Client Secret")
			.defaultValue("")
			.helpText("client_secret to negotiate the Access Token (required for OAUTH authorization)")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_OAUTH_SCOPE)
			.type(ProviderConfigProperty.STRING_TYPE).label("OAuth2 Scope")
			.defaultValue("")
			.helpText("Required scope in the access_token request")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_BASIC_USERNAME)
			.type(ProviderConfigProperty.STRING_TYPE).label("Auth Basic Username")
			.defaultValue("")
			.helpText("Username used for Basic Authentication")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_BASIC_PASSWORD)
			.type(ProviderConfigProperty.PASSWORD).label("Auth Basic Password")
			.defaultValue("")
			.helpText("Password used for Basic Authentication")
			.add();
		builder.property().name(RestConfiguration.PROPERTY_STATS_ENABLED)
			.type(ProviderConfigProperty.LIST_TYPE).label("HTTP pool stats enabled")
			.options(RestConfiguration.STATS_ENABLED_YES, RestConfiguration.STATS_ENABLED_NO)
			.defaultValue(RestConfiguration.STATS_ENABLED_NO)
			.helpText("Log HTTP pool stats in repo-provider initialization?")
			.add();
		builder.property().name(RestConfiguration.API_SOCKET_TIMEOUT)
			.type(ProviderConfigProperty.STRING_TYPE).label("API Socket Timeout")
			.defaultValue("4500")
			.add();
		builder.property().name(RestConfiguration.API_CONNECT_TIMEOUT)
			.type(ProviderConfigProperty.STRING_TYPE).label("API Connect Timeout")
			.defaultValue("1000")
			.add();
		builder.property().name(RestConfiguration.API_CONNECTION_REQUEST_TIMEOUT)
			.type(ProviderConfigProperty.STRING_TYPE).label("API Connection Request Timeout")
			.defaultValue("1000")
			.add();
		configMetadata = builder.build();
	}

	private RestHandler restHandler;

	@Override
	public KeycloakRestRepoProvider create(KeycloakSession session, ComponentModel model) {
		if(this.restHandler == null || !model.getConfig().equals( this.lastConfiguration )) {
			logger.infov("Creating a new instance of restHandler");
			RestConfiguration configuration = new RestConfiguration(model.getConfig());
			this.restHandler = new RestHandler(configuration);
			this.lastConfiguration = model.getConfig();
		}
		else {
			logger.infov("RestHandler already instantiated");
		}
		return new KeycloakRestRepoProvider(session, model, this.restHandler);
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
		RestConfiguration.validate(config.getConfig());
	}
}
