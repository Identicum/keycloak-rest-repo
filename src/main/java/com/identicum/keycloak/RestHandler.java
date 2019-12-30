package com.identicum.keycloak;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.logging.Logger;

public class RestHandler {
	
	private static final Logger logger = Logger.getLogger(RestHandler.class);
	protected CloseableHttpClient httpClient;
	
	private String baseURL = "http://localhost:8082";
	
	public RestHandler() {
		HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
    	this.httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).build();
    	
    	logger.infov("Initializing HttpClient pool.");
	}
	
	public String getBaseURL() {
		return this.baseURL;
	}
	
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	
	public boolean authenticate(String username, String password) {
		
		logger.infov("Authenticating user: {0}", username);
		
		HttpPost httpPost = new HttpPost(this.getBaseURL() + "/authenticate");
		
		JsonObject json = Json.createObjectBuilder()
				.add("username", username)
				.add("password", password)
				.build();
		
		try {
			StringEntity entity = new StringEntity(json.toString());
		    httpPost.setEntity(entity);
		    httpPost.setHeader("Accept", "application/json");
		    httpPost.setHeader("Content-type", "application/json");
			
			HttpResponse response = this.httpClient.execute(httpPost);
			logger.infov("Status received authenticating user {0}: {1}", username, response.getStatusLine().getStatusCode());
			return response.getStatusLine().getStatusCode() == 200;
		}
		catch(IOException io) {
			return false;
		}
		
	}
	
	public JsonObject findUserByUsername(String username) {
		
		logger.infov("Thread id {0} - Searching user: {1}", Thread.currentThread().getId(), username);
		
		HttpGet httpGet = new HttpGet(this.getBaseURL() + "/users/" + username);
		CloseableHttpResponse response = null;
		try {
			response = this.httpClient.execute(httpGet);
			this.analyzeResponse(response);
			
			JsonReader reader = Json.createReader(response.getEntity().getContent());
			return reader.readObject();
		}
		catch(IOException io) {
			throw new RuntimeException("Error getting user " + username, io);
		}
	}
	
	private void analyzeResponse(HttpResponse response) {
		logger.infov("Value received: {0}", response.getStatusLine().getStatusCode());
	}
	
	public void close() {
		//this.httpClient.close();
	}
	
}
