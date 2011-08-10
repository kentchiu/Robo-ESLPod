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
import android.provider.BaseColumns;
import android.util.Log;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public abstract class AbstractDictionaryCommand implements Runnable {

	// http://m.dictionary.com/?q=book&submit-result-SEARCHD=Search
	// http://i.word.com/

	public static AbstractDictionaryCommand newDictionaryCommand(Context context, Uri wordBankUri, int dictionaryId) {
		switch (dictionaryId) {
		case Dictionary.DICTIONARY_DREYE_DICTIONARY:
			return new DreyeDictionaryCommand(context, wordBankUri);
		case Dictionary.DICTIONARY_DICTIONARY_DICTIONARY:
			return new DictionaryDictionaryCommand(context, wordBankUri);
		case Dictionary.DICTIONARY_WIKITIONARY:
			return new WiktionaryCommand(context, wordBankUri);
		default:
			throw new IllegalArgumentException("Unkonw dictionary id : " + dictionaryId);
		}
	}

	private Uri		wordBankUri;
	private Context	context;

	protected AbstractDictionaryCommand(Context context, Uri wordBankUri) {
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
				String wordId = c.getString(c.getColumnIndex(BaseColumns._ID));
				String selection = DictionaryColumns.WORD_ID + "=? and " + DictionaryColumns.DICTIONARY_ID + "=?";
				String[] projection = new String[] { BaseColumns._ID };
				String[] selectionArgs = new String[] { wordId, Integer.toString(getDictionaryId()) };
				if (getContext().getContentResolver().query(DictionaryColumns.DICTIONARY_URI, projection, selection, selectionArgs, null).getCount() == 0) {
					try {
						Log.v(EslPodApplication.TAG, "get word definition of [" + word + "] to dictionary " + getDictionaryId());
						String content = getContent(word);
						updateDatabase(content);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Log.v(EslPodApplication.TAG, "word already exists in dictionary " + getDictionaryId());
				}
			}
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

	protected void updateDatabase(String content) {
		ContentValues cv = new ContentValues();
		cv.put(DictionaryColumns.DICTIONARY_ID, getDictionaryId());
		cv.put(DictionaryColumns.WORD_ID, ContentUris.parseId(wordBankUri));
		cv.put(DictionaryColumns.CONTENT, content);
		getContext().getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
	}

}