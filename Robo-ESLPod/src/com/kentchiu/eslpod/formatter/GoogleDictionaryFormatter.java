package com.kentchiu.eslpod.formatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleDictionaryFormatter {

	public static String formatText(String json) {
		try {
			JSONObject jo = new JSONObject(json);
			JSONArray ja = jo.getJSONArray("primaries");
			JSONObject primary = ja.getJSONObject(0);
			JSONArray terms = primary.getJSONArray("terms");
			terms.getJSONObject(2);
			String kk = terms.getJSONObject(3).getString("text");
			JSONArray entries = primary.getJSONArray("entries");
			String partOfSpeech = entries.getJSONObject(0).getJSONArray("labels").getJSONObject(0).getString("text");
			String meaning = entries.getJSONObject(0).getJSONArray("entries").getJSONObject(0).getJSONArray("terms").getJSONObject(1).getString("text");
			return kk + "<br/>" + partOfSpeech + "<br/>" + meaning + "</br>";

		} catch (JSONException e) {
			return "查無資料";
		}
	}
}
