package com.identicum.keycloak;

import lombok.AllArgsConstructor;
import lombok.Getter;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;

import static jakarta.json.Json.createReader;

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
