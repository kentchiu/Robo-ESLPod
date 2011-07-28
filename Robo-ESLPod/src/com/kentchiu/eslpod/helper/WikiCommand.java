package com.kentchiu.eslpod.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class WikiCommand implements Runnable {

	private Uri		wordBankUri;
	private Context	context;

	public WikiCommand(Context context, Uri workBankUri) {
		this.context = context;
		wordBankUri = workBankUri;
	}

	@Override
	public void run() {
		String type = getContext().getContentResolver().getType(wordBankUri);
		if (StringUtils.equals(WordBankColumns.CONTENT_TYPE_WORD, type)) {
			Cursor c = getContext().getContentResolver().query(wordBankUri, null, null, null, null);
			if (c.moveToFirst()) {
				String word = c.getString(c.getColumnIndex(WordBankColumns.WORD));
				String urlStr = "http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&rvexpandtemplates=true&titles=" + word;
				try {
					URL url = new URL(urlStr);
					InputStream is = url.openStream();
					List<String> lines = IOUtils.readLines(is);
					String join = Joiner.on("").join(lines);
					String content = extractContent(join);
					ContentValues vs = new ContentValues();
					vs.put(DictionaryColumns.DICTIONARY_ID, Dictionary.DICTIONARY_WIKI_DICTIONARY);
					vs.put(DictionaryColumns.WORD_ID, ContentUris.parseId(wordBankUri));
					vs.put(DictionaryColumns.CONTENT, content);
					getContext().getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, vs);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
			return "";
		}
	}

	private Context getContext() {
		return context;
	}
}
