package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.net.MalformedURLException;
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
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public abstract class DictionaryCommand implements Runnable {

	private Uri		wordBankUri;
	private Context	context;

	public DictionaryCommand(Context context, Uri wordBankUri) {
		super();
		this.context = context;
		this.wordBankUri = wordBankUri;
	}

	public Context getContext() {
		return context;
	}

	@Override
	public void run() {
		String type = getContext().getContentResolver().getType(wordBankUri);
		if (StringUtils.equals(WordBankColumns.CONTENT_TYPE_WORD, type)) {
			Cursor c = getContext().getContentResolver().query(wordBankUri, null, null, null, null);
			if (c.moveToFirst()) {
				String word = c.getString(c.getColumnIndex(WordBankColumns.WORD));
				try {
					String content = getContent(word);
					updateDatabase(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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

	protected void updateDatabase(String content) {
		ContentValues cv = new ContentValues();
		cv.put(DictionaryColumns.DICTIONARY_ID, getDictionaryId());
		cv.put(DictionaryColumns.WORD_ID, ContentUris.parseId(wordBankUri));
		cv.put(DictionaryColumns.CONTENT, content);
		getContext().getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
	}

}