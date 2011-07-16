package com.kentchiu.eslpod.service;

import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Dictionary;

public class DictionService extends IntentService {



	public DictionService() {
		super("Google Suggestion Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String query = intent.getStringExtra(SearchManager.QUERY);
		ContentValues values = new ContentValues();
		// FIXME refactory to Dictionary.Column.DICTIONARY_ID
		values.put("diction_id", Dictionary.DICTIONARY_GOOGLE_SUGGESTION);
		values.put("content", getContent(query));
		getContentResolver().insert(Dictionary.DICTIONARY_URI, values);
	}

	private String getContent(String query) {
		Log.d(EslPodApplication.LOG_TAG, "query " + query + " at Google Suggestion");
		String urlStr = "http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=" + query;
		//String content = HttpUtils.getContent(this, url + query);
		String content ;
		try {
			URL url = new URL(urlStr);
			List<String> lines = IOUtils.readLines(url.openStream(), "BIG5");
			String join = Joiner.on("").join(lines);
			String str1 = StringUtils.substringAfter(join, "window.google.ac.hr(");
			content = StringUtils.substringBeforeLast(str1, ")");
		} catch (Exception e) {
			content = "[\"" + query + "\",[],{\"k\":1}]";

		}

		Log.d(EslPodApplication.LOG_TAG, query +  " : " + content);
		return content;
	}

}
