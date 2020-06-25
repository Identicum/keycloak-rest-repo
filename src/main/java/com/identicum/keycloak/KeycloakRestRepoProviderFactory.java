package com.identicum.keycloak;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

public class KeycloakRestRepoProviderFactory implements UserStorageProviderFactory<KeycloakRestRepoProvider> {


	private static final Logger logger = Logger.getLogger(KeycloakRestRepoProviderFactory.class);
	protected static final List<ProviderConfigProperty> configMetadata;

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
				.options(RestConfiguration.AUTH_NONE, RestConfiguration.AUTH_OAUTH)
				.defaultValue(RestConfiguration.AUTH_NONE)
				.helpText("Authorization method used by consumed API")
				.add();
		builder.property().name("clientId")
				.type(ProviderConfigProperty.STRING_TYPE).label("Client Id")
				.defaultValue("")
				.helpText("Local client_id to negotiate the Access Token")
				.add();
		configMetadata = builder.build();
	}

	private RestHandler restHandler;

	@Override
	public KeycloakRestRepoProvider create(KeycloakSession session, ComponentModel model) {
		if(this.restHandler == null) {
			RestConfiguration configuration = new RestConfiguration(model.getConfig());
			this.restHandler = new RestHandler(configuration);
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
	 }
}
