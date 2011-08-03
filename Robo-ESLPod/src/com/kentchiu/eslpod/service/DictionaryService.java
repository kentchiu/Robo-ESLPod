package com.kentchiu.eslpod.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

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

	public DictionaryService() {
		super(DictionaryService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int cmd = intent.getIntExtra(COMMAND, -1);
		switch (cmd) {
		case COMMAND_DOWNLOAD_WORD:
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.v(EslPodApplication.TAG, "Downloading word " + query);
			Preconditions.checkNotNull(query);
			ContentValues cv = new ContentValues();
			cv.put(WordBankColumns.WORD, query);
			Uri uri = getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
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

			//			new Thread(new GoogleSuggestCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId))).start();
			//			new Thread(new GoogleDictionaryCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId))).start();
			//			new Thread(new WikiCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId))).start();
			break;
		default:
			break;
		}
	}

}
