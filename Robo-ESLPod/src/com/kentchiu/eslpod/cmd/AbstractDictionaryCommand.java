package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import roboguice.util.Ln;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public abstract class AbstractDictionaryCommand implements Runnable {

	// http://i.word.com/

	public static AbstractDictionaryCommand newDictionaryCommand(Context context, String query, int dictionaryId) {
		switch (dictionaryId) {
		case Dictionary.DICTIONARY_DREYE_DICTIONARY:
			return new DreyeDictionaryCommand(context, query);
		case Dictionary.DICTIONARY_DICTIONARY_DICTIONARY:
			return new DictionaryDictionaryCommand(context, query);
		case Dictionary.DICTIONARY_WIKITIONARY:
			return new WiktionaryCommand(context, query);
		default:
			throw new IllegalArgumentException("Unkonw dictionary id : " + dictionaryId);
		}
	}

	public static List<AbstractDictionaryCommand> newDictionaryCommands(Context context, String w) {
		Cursor c2 = context.getContentResolver().query(DictionaryColumns.DICTIONARY_URI, null, DictionaryColumns.WORD + "=?", new String[] { w }, null);
		Set<Integer> dictIds = Sets.newHashSet();
		while (c2.moveToNext()) {
			dictIds.add(c2.getInt(c2.getColumnIndex(DictionaryColumns.DICTIONARY_ID)));
		}
		HashSet<Integer> allDictIds = Sets.newHashSet();
		allDictIds.add(Dictionary.DICTIONARY_DREYE_DICTIONARY);
		allDictIds.add(Dictionary.DICTIONARY_DICTIONARY_DICTIONARY);
		allDictIds.add(Dictionary.DICTIONARY_WIKITIONARY);
		Iterables.removeAll(allDictIds, dictIds);
		String query = StringUtils.trim(w);
		Ln.v("There are %d dictionary need to be update for word [%s]", allDictIds.size(), query);
		List<AbstractDictionaryCommand> cmds = Lists.newArrayList();
		for (Integer each : allDictIds) {
			AbstractDictionaryCommand cmd = newDictionaryCommand(context, query, each);
			cmds.add(cmd);
		}
		return cmds;
	}

	public static String toHtml(Context context, String query, String content, int dictId) {
		return newDictionaryCommand(context, query, dictId).render(content);
	}

	protected Context	context;
	protected String	query;

	protected AbstractDictionaryCommand(Context context, String query) {
		super();
		this.context = context;
		this.query = query;
	}

	protected abstract String getContent();

	protected abstract int getDictionaryId();

	protected abstract String getQueryUrl();

	protected String readAsOneLine(String urlStr, int retried) {
		int retry = 3;
		HttpClient httpClient = CustomHttpClient.getHttpClient();
		String url = StringUtils.trim(urlStr).replaceAll(",", "").replaceAll(" ", "%20");
		try {
			HttpGet request = new HttpGet(url);
			String page = httpClient.execute(request, new UnicodeResponseHandler());
			return page;
		} catch (IOException e) {
			e.printStackTrace();
			// retry to fetch, if timeout
			if (retried < retry) {
				Ln.w("Retry to fetch from " + url);
				return readAsOneLine(url, retried + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return "";
	}

	protected String render(String input) {
		return input;
	}

	@Override
	public void run() {
		String url = getQueryUrl();
		String content;
		try {
			Ln.v("Start fetch  word [%s] from dictionary ", query);
			if (StringUtils.isBlank(query)) {
				content = "";
			} else {
				content = getContent();
			}

			Ln.v("End fetch  word [%d] from dictionary ", getDictionaryId());
			if (StringUtils.isNotBlank(content)) {
				ContentValues cv = new ContentValues();
				cv.put(DictionaryColumns.DICTIONARY_ID, getDictionaryId());
				cv.put(DictionaryColumns.WORD, query);
				cv.put(DictionaryColumns.CONTENT, content);
				context.getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
				Ln.v("Save word [%s] to dictionary %d", query, getDictionaryId());
			} else {
				Ln.w("fetch word [%d] fail form dictionary  %d, url:%s", getDictionaryId(), url);
			}
		} catch (Exception e) {
			Ln.w("fetch word [" + query + "] fail form dictionary " + getDictionaryId() + ", url:" + url, e);
		}
	}
}