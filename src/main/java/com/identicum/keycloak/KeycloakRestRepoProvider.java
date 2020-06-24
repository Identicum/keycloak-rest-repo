package com.identicum.keycloak;

import java.util.*;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

public class KeycloakRestRepoProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {

	private static final Logger logger = Logger.getLogger(KeycloakRestRepoProvider.class);
	
    protected KeycloakSession session;
    protected ComponentModel model;
    
    // map of loaded users in this transaction
    protected Map<String, RestUserAdapter> loadedUsers = new HashMap<>();
    
    protected RestHandler restHandler;

    public KeycloakRestRepoProvider(KeycloakSession session, ComponentModel model, RestHandler restHandler) {
    	logger.info("Initializing new RestRepoProvider");
    	this.session = session;
    	this.model = model;
    	this.restHandler = restHandler;
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
		RestUserAdapter adapter = loadedUsers.get(username);
		if (adapter == null) {
			JsonObject userJson = this.restHandler.findUserByUsername(username);
			adapter = new RestUserAdapter(session, realm, model, userJson);
			adapter.setHandler(this.restHandler);
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

	@Override
	public int getUsersCount(RealmModel realmModel) {
		return 0;
	}

	@Override
	public List<UserModel> getUsers(RealmModel realmModel) {
		return getUsers(realmModel, 0, Integer.MAX_VALUE);
	}

	@Override
	public List<UserModel> getUsers(RealmModel realmModel, int from, int pageSize) {
		return searchForUser("", realmModel, from, pageSize);
	}

	@Override
	public List<UserModel> searchForUser(String pattern, RealmModel realmModel) {
		return searchForUser(pattern, realmModel, 0, Integer.MAX_VALUE);
	}

	@Override
	public List<UserModel> searchForUser(String pattern, RealmModel realmModel, int from, int pageSize) {
		logger.infov("Searching users with pattern: {0} from {1} with pageSize {2}", pattern, from, pageSize);
		JsonArray usersJson = this.restHandler.findUsers(pattern);
		logger.infov("Found {0} users", usersJson.size());
		List<UserModel> users = new LinkedList<>();
		for(int i=from; i < Math.min(usersJson.size(), from + pageSize); i++) {
			logger.infov("Converting user {0} to UserModel", usersJson.getJsonObject(i));
			RestUserAdapter userModel = new RestUserAdapter(session, realmModel, model, usersJson.getJsonObject(i));
			userModel.setHandler(this.restHandler);
			users.add(userModel);
		}
		return users;
	}

	@Override
	public List<UserModel> searchForUser(Map<String, String> map, RealmModel realmModel) {
		return this.searchForUser(map.get("username"), realmModel);
	}

	@Override
	public List<UserModel> searchForUser(Map<String, String> map, RealmModel realmModel, int from, int pageSize) {
		return this.searchForUser(map.get("username"), realmModel, from, pageSize);
	}

	@Override
	public List<UserModel> getGroupMembers(RealmModel realmModel, GroupModel groupModel, int from, int pageSize) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<UserModel> getGroupMembers(RealmModel realmModel, GroupModel groupModel) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<UserModel> searchForUserByUserAttribute(String s, String s1, RealmModel realmModel) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public int getUsersCount(RealmModel realm, boolean includeServiceAccount) {
		return this.getUsers(realm).size();
	}

	@Override
	public List<UserModel> getRoleMembers(RealmModel realm, RoleModel role) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<UserModel> getRoleMembers(RealmModel realm, RoleModel role, int from, int pageSize) {
		return Collections.EMPTY_LIST;
	}

}
