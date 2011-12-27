package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import roboguice.util.Ln;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public abstract class AbstractDictionaryCommand extends AbstractCommand {

	public static final String	WORD	= "word";

	public static Integer[] allDictionaryId() {
		return new Integer[] { Dictionary.DICTIONARY_DREYE_DICTIONARY, Dictionary.DICTIONARY_DICTIONARY_DICTIONARY, Dictionary.DICTIONARY_WIKITIONARY };
	}

	public static AbstractDictionaryCommand newDictionaryCommand(Context context, String word, int dictionaryId) {
		Intent intent = new Intent();
		intent.putExtra(WORD, word);
		switch (dictionaryId) {
		case Dictionary.DICTIONARY_DREYE_DICTIONARY:
			return new DreyeDictionaryCommand(context, intent);
		case Dictionary.DICTIONARY_DICTIONARY_DICTIONARY:
			return new DictionaryDictionaryCommand(context, intent);
		case Dictionary.DICTIONARY_WIKITIONARY:
			return new WiktionaryCommand(context, intent);
		default:
			throw new IllegalArgumentException("Unkonw dictionary id : " + dictionaryId);
		}
	}

	public static List<AbstractDictionaryCommand> newDictionaryCommands(Context context, String w) {
		Cursor c = context.getContentResolver().query(DictionaryColumns.DICTIONARY_URI, null, DictionaryColumns.WORD + "=?", new String[] { w }, null);
		Set<Integer> dictIds = Sets.newHashSet();
		while (c.moveToNext()) {
			dictIds.add(c.getInt(c.getColumnIndex(DictionaryColumns.DICTIONARY_ID)));
		}
		Set<Integer> allDictIds = Sets.newHashSet(allDictionaryId());
		Iterables.removeAll(allDictIds, dictIds);
		String word = StringUtils.trim(w);
		Ln.v("There are %d dictionary need to be update for word [%s]", allDictIds.size(), word);
		List<AbstractDictionaryCommand> cmds = Lists.newArrayList();
		for (Integer each : allDictIds) {
			AbstractDictionaryCommand cmd = newDictionaryCommand(context, word, each);
			cmds.add(cmd);
		}
		return cmds;
	}

	public static String toHtml(Context context, String word, String content, int dictId) {
		return newDictionaryCommand(context, word, dictId).render(content);
	}

	public AbstractDictionaryCommand(Context context, Intent intent) {
		this(context, intent, null);
	}

	public AbstractDictionaryCommand(Context context, Intent intent, Handler handler) {
		super(context, intent, handler);
	}

	@Override
	protected boolean execute() {
		String url = getQueryUrl();
		String word = intent.getExtras().getString(WORD);
		String content;
		try {
			Ln.v("Start fetch  word [%s] from dictionary ", word);
			if (StringUtils.isBlank(word)) {
				content = "";
			} else {
				content = getContent();
			}

			Ln.v("End fetch  word [%s] from dictionary ", word);
			if (StringUtils.isNotBlank(content)) {
				ContentValues cv = new ContentValues();
				cv.put(DictionaryColumns.DICTIONARY_ID, getDictionaryId());
				cv.put(DictionaryColumns.WORD, word);
				cv.put(DictionaryColumns.CONTENT, content);
				context.getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
				Ln.v("Save word [%s] to dictionary %d", word, getDictionaryId());
			} else {
				Ln.w("fetch word [%s] fail form dictionary %d, url:%s", word, getDictionaryId(), url);
			}
			return true;
		} catch (Exception e) {
			Ln.w("fetch word [%s] fail form dictionary %d, url:$s", word, getDictionaryId(), url);
			Ln.w(e);
			return false;
		}
	}

	protected abstract String getContent();

	public abstract int getDictionaryId();

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
}