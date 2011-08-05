package com.kentchiu.eslpod.cmd;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Dictionary;

public class WikiCommand extends DictionaryCommand {

	private static final int	DICTIONARY_WIKI_DICTIONARY	= Dictionary.DICTIONARY_WIKI_DICTIONARY;

	public WikiCommand(Context context, Uri wordBankUri) {
		super(context, wordBankUri);
	}

	@Override
	public String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		String join = readAsOneLine(url);
		return extractContent(join);
	}

	@Override
	protected int getDictionaryId() {
		return DICTIONARY_WIKI_DICTIONARY;
	}

	@Override
	protected String getQueryUrl(String word) {
		// http://en.wiktionary.org/w/api.php?action=query&
		// prop=revisions
		// &rvprop=content
		// &format=json
		// &rvexpandtemplates=true
		// &titles=
		// http://zh.wiktionary.org/w/index.php?title=book&action=edit
		return "http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&rvexpandtemplates=true&alllinks=true&titles=" + word;
	}

	private synchronized String extractContent(String content) {
		try {
			// Drill into the JSON response to find the content body
			JSONObject response = new JSONObject(content);
			JSONObject query = response.getJSONObject("query");
			JSONObject pages = query.getJSONObject("pages");
			JSONObject page = pages.getJSONObject((String) pages.keys().next());
			JSONArray revisions = page.getJSONArray("revisions");
			JSONObject revision = revisions.getJSONObject(0);
			return revision.getString("*");
		} catch (JSONException e) {
			Log.w(EslPodApplication.TAG, "Extract json content fiil", e);
			return "";
		}
	}

}
