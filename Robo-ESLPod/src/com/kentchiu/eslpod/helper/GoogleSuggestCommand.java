package com.kentchiu.eslpod.helper;

import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class GoogleSuggestCommand implements Runnable {
	private Uri		wordBankUri;
	private Context	context;

	public GoogleSuggestCommand(Context context, Uri workBankUri) {
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
				String urlStr = "http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=" + word;
				String content;
				try {
					URL url = new URL(urlStr);
					List<String> lines = IOUtils.readLines(url.openStream(), "BIG5");
					String join = Joiner.on("").join(lines);
					String str1 = StringUtils.substringAfter(join, "window.google.ac.hr(");
					content = StringUtils.substringBeforeLast(str1, ")");
				} catch (Exception e) {
					content = "[\"" + word + "\",[],{\"k\":1}]";
				}
				ContentValues cv = new ContentValues();
				cv.put(DictionaryColumns.DICTIONARY_ID, Dictionary.DICTIONARY_GOOGLE_SUGGESTION);
				cv.put(DictionaryColumns.WORD_ID, ContentUris.parseId(wordBankUri));
				cv.put(DictionaryColumns.CONTENT, content);
				getContext().getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
			}
		}
	}

	private Context getContext() {
		return context;
	}
}
