package com.ivy.lib.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class JSONFormatter {

	private String TITLE;
	private Hashtable<String, String> keyValuePair;

	public JSONFormatter(String title) {
		this.TITLE = title;
		keyValuePair = new Hashtable<String, String>();
	}

	public void addParameter(String key, String value) {
		keyValuePair.put(key, value);
	}

	public void addParameter(String key, int value) {
		keyValuePair.put(key, value + "");
	}

	public void addParameter(String key, float value) {
		keyValuePair.put(key, value + "");
	}

	public String getDataInJson() throws JSONException {
		JSONObject rootObj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		JSONObject object = new JSONObject();
		for (String key : keyValuePair.keySet()) {
			object.put(key, keyValuePair.get(key));
		}
		jsonArray.put(object);
		rootObj.put(this.TITLE, jsonArray);
		return rootObj.toString();
	}

}
