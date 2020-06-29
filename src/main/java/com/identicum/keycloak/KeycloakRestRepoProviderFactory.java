package com.identicum.keycloak;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

public class KeycloakRestRepoProviderFactory implements UserStorageProviderFactory<KeycloakRestRepoProvider> {

	private static final Logger logger = Logger.getLogger(KeycloakRestRepoProviderFactory.class);
	protected static final List<ProviderConfigProperty> configMetadata;

	private MultivaluedHashMap<String, String> lastConfiguration = null;

	static {
		ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();
		builder.property().name("baseURL")
				.type(ProviderConfigProperty.STRING_TYPE).label("Base URL")
				.defaultValue("http://localhost:8082/")
				.helpText("Api url base to authenticate users")
				.add();
		builder.property().name("maxHttpConnections")
				.type(ProviderConfigProperty.STRING_TYPE).label("Max pool connections")
				.defaultValue("5")
				.helpText("Max http connections in pool")
				.add();
		builder.property().name("authType")
				.type(ProviderConfigProperty.LIST_TYPE).label("Api Authorization")
				.options(RestConfiguration.AUTH_NONE, RestConfiguration.AUTH_BASIC, RestConfiguration.AUTH_OAUTH)
				.defaultValue(RestConfiguration.AUTH_NONE)
				.helpText("Authorization method used by consumed API")
				.add();
		builder.property().name("clientId")
				.type(ProviderConfigProperty.CLIENT_LIST_TYPE).label("OAuth2 Client Id")
				.defaultValue("")
				.helpText("Local client_id to negotiate the Access Token (required for OAUTH authorization)")
				.add();
		builder.property().name("basicUsername")
				.type(ProviderConfigProperty.STRING_TYPE).label("Auth Basic Username")
				.defaultValue("")
				.helpText("Username used for Basic Authentication")
				.add();
		builder.property().name("basicPassword")
				.type(ProviderConfigProperty.PASSWORD).label("Auth Basic Password")
				.defaultValue("")
				.helpText("Password used for Basic Authentication")
				.add();
		configMetadata = builder.build();
	}

	private Map<String,RestHandler> restHandlers = new HashMap<>();

	@Override
	public KeycloakRestRepoProvider create(KeycloakSession session, ComponentModel model) {
		String realm = session.getContext().getRealm().getName();
		logger.infov("Creating KeycloakRestRepoProvider for realm: {0}", realm);

		RestHandler handler = this.restHandlers.get(realm);
		if(handler == null || !model.getConfig().equals( this.lastConfiguration )) {
			logger.infov("Creating a new instance of restHandler for realm: {0}", realm);
			RestConfiguration configuration = new RestConfiguration(model.getConfig());
			configuration.setContext(session.getContext());
			handler = new RestHandler(configuration);
			this.restHandlers.put(realm, handler);
			this.lastConfiguration = model.getConfig();
		}
		else {
			logger.infov("RestHandler already instantiated");
		}
		return new KeycloakRestRepoProvider(session, model, handler);
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
		String baseURL = config.getConfig().getFirst("baseURL");
		if (baseURL == null) throw new ComponentValidationException("BaseURL is not specified");
		try {
			URL url = new URL(baseURL);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.connect();
			urlConn.disconnect();
		} catch (IOException e) {
			throw new ComponentValidationException("Error accessing the base url", e);
		}

		String maxConnections = config.getConfig().getFirst("maxHttpConnections");
		if(maxConnections == null || !maxConnections.matches("\\d*")) {
			logger.warn("maxHttpConnections property is not valid. Enter a valid number");
			throw new ComponentValidationException("Max pool connections should be a number");
		}

		if (config.getConfig().getFirst("authType").equals( RestConfiguration.AUTH_OAUTH)) {
			logger.warn("Auth Type set to OAUTH2. Checking client id");
			String clientId = config.getConfig().getFirst("clientId");
			if( clientId == null || clientId.isEmpty()) {
				throw new ComponentValidationException("The client_id field is required for OAuth2 authorization type");
			}
			ClientModel client = realm.getClientByClientId(clientId);
			if( client == null) {
				throw new ComponentValidationException("The client_id could not be found in this Realm");
			}
		}

		if (config.getConfig().getFirst("authType").equals( RestConfiguration.AUTH_BASIC)) {
			logger.warn("Auth Type set to Basic. Checking username and password");
			String username = config.getConfig().getFirst("basicUsername");
			if( username == null || username.isEmpty()) {
				throw new ComponentValidationException("The username field is required for Basic Authentication type");
			}
			String password = config.getConfig().getFirst("basicPassword");
			if( password == null || password.isEmpty()) {
				throw new ComponentValidationException("The password field is required for Basic Authentication type");
			}
		}
	 }
}
