package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public abstract class AbstractDictionaryCommand implements Runnable {

	// http://m.dictionary.com/?q=book&submit-result-SEARCHD=Search
	// http://i.word.com/

	public static AbstractDictionaryCommand newDictionaryCommand(Handler handler, String query, int dictionaryId) {
		switch (dictionaryId) {
		case Dictionary.DICTIONARY_DREYE_DICTIONARY:
			return new DreyeDictionaryCommand(handler, query);
		case Dictionary.DICTIONARY_DICTIONARY_DICTIONARY:
			return new DictionaryDictionaryCommand(handler, query);
		case Dictionary.DICTIONARY_WIKITIONARY:
			return new WiktionaryCommand(handler, query);
		default:
			throw new IllegalArgumentException("Unkonw dictionary id : " + dictionaryId);
		}
	}

	private Handler	handler;
	private String	query;

	protected AbstractDictionaryCommand(Handler handler, String query) {
		super();
		this.handler = handler;
		this.query = query;
	}

	@Override
	public void run() {
		try {
			String content;
			Log.v(EslPodApplication.TAG, "Start querying  word " + query + " from dictionary " + getDictionaryId());
			if (StringUtils.isBlank(query)) {
				content = "";
			} else {
				content = getContent(query);
			}
			Log.v(EslPodApplication.TAG, "End queried  word " + query + " from dictionary " + getDictionaryId());
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt(DictionaryColumns.DICTIONARY_ID, getDictionaryId());
			b.putString(DictionaryColumns.WORD, query);
			b.putString(DictionaryColumns.CONTENT, content);
			msg.setData(b);
			Preconditions.checkNotNull(handler);
			handler.sendMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toHtml(String input) {
		return input;
	}

	protected abstract String getContent(String word) throws IOException;

	protected abstract int getDictionaryId();

	protected abstract String getQueryUrl(String word);

	protected String readAsOneLine(String urlStr) throws MalformedURLException, IOException {
		URL url = new URL(urlStr);
		List<String> lines = IOUtils.readLines(url.openStream());
		return Joiner.on("").join(lines);
	}

	protected String readAsOneLine(String urlStr, String encoding) throws MalformedURLException, IOException {
		URL url = new URL(urlStr);
		List<String> lines = IOUtils.readLines(url.openStream(), encoding);
		return Joiner.on("").join(lines);
	}

}