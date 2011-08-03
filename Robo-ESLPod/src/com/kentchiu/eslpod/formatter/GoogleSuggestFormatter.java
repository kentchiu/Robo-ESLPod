package com.kentchiu.eslpod.formatter;

import org.json.JSONArray;
import org.json.JSONException;

public class GoogleSuggestFormatter {

	public static JSONArray extractShowPart(String json) throws JSONException {
		JSONArray ja = new JSONArray(json);
		JSONArray ja2 = ja.getJSONArray(1);
		JSONArray ja3 = ja2.getJSONArray(0);
		return ja3;
	}

	public static String formatText(String content) {
		try {
			JSONArray extractShowPart = extractShowPart(content);
			return extractShowPart.getString(1);
		} catch (JSONException e) {
			e.printStackTrace();
			return "查無資料";
		}
	}

}
