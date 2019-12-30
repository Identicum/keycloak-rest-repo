package com.identicum.keycloak;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.jboss.logging.Logger;
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
		configMetadata = ProviderConfigurationBuilder.create().property().name("baseURL")
				.type(ProviderConfigProperty.STRING_TYPE).label("Base URL")
				.defaultValue("http://localhost:8082/")
				.helpText("URL base para métodos /authenticate y /users").add().build();
	}

	@Override
	public KeycloakRestRepoProvider create(KeycloakSession session, ComponentModel model) {
		String baseURL = model.getConfig().getFirst("baseURL");		
		logger.infov("Loaded baseURL from module properties: {0}", baseURL);
		return new KeycloakRestRepoProvider(session, model, baseURL);
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
		} catch (IOException e) {
			throw new ComponentValidationException("Error accessing the base url", e);
		}
	 }
}
