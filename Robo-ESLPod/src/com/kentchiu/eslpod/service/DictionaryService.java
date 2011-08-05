package com.kentchiu.eslpod.service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.GoogleDictionaryCommand;
import com.kentchiu.eslpod.cmd.GoogleSuggestCommand;
import com.kentchiu.eslpod.cmd.WikiCommand;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class DictionaryService extends IntentService {

	public static final String		NO_WAIT							= "NO_WAIT";
	public static final String		COMMAND							= "command";
	public static final int			COMMAND_DOWNLOAD_WORD			= 1;
	public static final int			COMMAND_DOWNLOAD_DICTIONARIES	= 2;
	private static ExecutorService	es								= Executors.newFixedThreadPool(6);

	public static String getBasicForm(String word) {
		try {
			URL url1 = new URL("http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=xml&titles=" + word);
			List<String> lines = IOUtils.readLines(url1.openStream());
			String join = Joiner.on("").join(lines);
			String wikiContent = StringUtils.substringBetween(join, "<rev xml:space=\"preserve\">", "</rev>");
			Log.v(EslPodApplication.TAG, "wiki content:" + wikiContent);

			String form = "present participle";
			if (!StringUtils.isBlank(getMatch(wikiContent, form))) {
				return getMatch(wikiContent, form);
			}
			form = "simple past";
			if (!StringUtils.isBlank(getMatch(wikiContent, form))) {
				return getMatch(wikiContent, form);
			}
			form = "plural";
			if (!StringUtils.isBlank(getMatch(wikiContent, form))) {
				return getMatch(wikiContent, form);
			}
			return word;
		} catch (IOException e) {
			Log.e(EslPodApplication.TAG, "query for word [" + word + "] from wiktionary fail", e);
			return word;
		}
	}

	private static String getMatch(String wikiContent, String part) {

		Pattern p = Pattern.compile("\\{\\{" + part + " of\\|(\\[)*((\\w)*)(\\])*\\}\\}");
		Matcher match1 = p.matcher(wikiContent);
		if (match1.find()) {
			return match1.group(2);
		}
		return "";
	}

	public DictionaryService() {
		super(DictionaryService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int cmd = intent.getIntExtra(COMMAND, -1);
		switch (cmd) {
		case COMMAND_DOWNLOAD_WORD:
			String query = intent.getStringExtra(SearchManager.QUERY);
			Preconditions.checkNotNull(query);
			ContentValues cv = new ContentValues();
			cv.put(WordBankColumns.WORD, query);
			Log.d(EslPodApplication.TAG, "save word [" + query + "] to bank");
			Uri uri = getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
			Log.v(EslPodApplication.TAG, "word [" + query + "] insert as uri :" + uri);
			Intent newIntent = new Intent(this, DictionaryService.class);
			newIntent.putExtra(COMMAND, COMMAND_DOWNLOAD_DICTIONARIES);
			newIntent.putExtra(NO_WAIT, intent.getBooleanExtra(NO_WAIT, false));
			newIntent.setData(uri);
			startService(newIntent);
			break;
		case COMMAND_DOWNLOAD_DICTIONARIES:
			Uri wordBankUri = intent.getData();
			long wordId = ContentUris.parseId(wordBankUri);
			boolean noWait = intent.getBooleanExtra(NO_WAIT, false);
			if (noWait) {
				ExecutorService es2 = Executors.newFixedThreadPool(3);
				es2.execute(new GoogleSuggestCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId)));
				es2.execute(new GoogleDictionaryCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId)));
				es2.execute(new WikiCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId)));
			} else {
				es.execute(new GoogleSuggestCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId)));
				es.execute(new GoogleDictionaryCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId)));
				es.execute(new WikiCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId)));
			}
			break;
		default:
			break;
		}
	}

}
