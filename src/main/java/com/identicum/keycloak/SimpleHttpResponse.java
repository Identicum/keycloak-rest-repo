package com.identicum.keycloak;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static javax.json.Json.createReader;

@Getter
@AllArgsConstructor
public class SimpleHttpResponse {

	private int status;
	private String response;

	public boolean isSuccess(){
		return status == 200;
	}

	public JsonObject getResponseAsJsonObject() {
		if(response != null) {
			JsonReader reader = createReader(new StringReader(response));
			return reader.readObject();
		}
		return null;
	}

	public JsonArray getResponseAsJsonArray() {
		if(response != null) {
			JsonReader reader = createReader(new StringReader(response));
			return reader.readArray();
		}
		return null;
	}
}
