package com.identicum.keycloak;

import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

import javax.json.*;
import java.io.StringReader;

public class SimpleHttpResponse {

	private int status;
	private String response;

	public SimpleHttpResponse(int status, String body) {
		this.status = status;
		this.response = body;
	}

	public int getStatus() {
		return status;
	}

	public boolean isSuccess(){
		return status == 200;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public JsonObject getResponseAsJsonObject() {
		if(this.response != null) {
			JsonReader reader = Json.createReader(new StringReader(response));
			return reader.readObject();
		}
		return null;
	}

	public JsonArray getResponseAsJsonArray() {
		if(this.response != null) {
			JsonReader reader = Json.createReader(new StringReader(response));
			return reader.readArray();
		}
		return null;
	}
}
