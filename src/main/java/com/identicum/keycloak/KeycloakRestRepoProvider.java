package com.identicum.keycloak;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.json.JsonObject;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

public class KeycloakRestRepoProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {

	private static final Logger logger = Logger.getLogger(KeycloakRestRepoProvider.class);
	
    protected KeycloakSession session;
    protected Properties properties;
    protected ComponentModel model;
    
    // map of loaded users in this transaction
    protected Map<String, UserModel> loadedUsers = new HashMap<>();
    
    protected RestHandler restHandler;

    public KeycloakRestRepoProvider(KeycloakSession session, ComponentModel model, String baseURL) {
    	logger.warn("Initializing new RestRepoProvider");
    	this.session = session;
    	this.model = model;
    	this.restHandler = new RestHandler();
    	this.restHandler.setBaseURL(baseURL);
    }
	
	@Override
	public void close() {
	}

	@Override
	public UserModel getUserByEmail(String email, RealmModel realm) {
		return null;
	}

	@Override
	public UserModel getUserById(String id, RealmModel realm) {
		StorageId storageId = new StorageId(id);
		String username = storageId.getExternalId();
		return getUserByUsername(username, realm);
	}

	@Override
	public UserModel getUserByUsername(String username, RealmModel realm) {	
		logger.infov("Cache size is: {0}", loadedUsers.size());
		UserModel adapter = loadedUsers.get(username);
		if (adapter == null) {
			JsonObject userJson = this.restHandler.findUserByUsername(username);
			adapter = new RestUserAdapter(session, realm, model, userJson);
			logger.infov("Setting user {0} into cache", username);
			loadedUsers.put(username, adapter);
		}
		else {
			logger.infov("Returning user {0} from cache", username);
		}
		return adapter;
	}

	@Override
	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Método que finalmente controla las credenciales
	 */
	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		logger.infov("Identicum - Validating user {0}", user.getUsername());
		if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;
		logger.infov("Identicum - Credential {0}", input.getChallengeResponse());
		//return password.equals(input.getChallengeResponse());
		return this.restHandler.authenticate(user.getUsername(), input.getChallengeResponse());
	}

	/**
	 * Indica qué tipo de credenciales puede validar, por ejemplo Password
	 */
	@Override
	public boolean supportsCredentialType(String credentialType) {
		return credentialType.equals(PasswordCredentialModel.TYPE);
	}

}
