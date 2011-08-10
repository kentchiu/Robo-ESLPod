package com.kentchiu.eslpod.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class DictionaryService extends IntentService {

	public static final String		COMMAND							= "command";
	public static final String		NO_WAIT							= "no wait";
	public static final int			COMMAND_DOWNLOAD_WORD			= 1;
	public static final int			COMMAND_DOWNLOAD_DICTIONARIES	= 2;
	public static final int			COMMAND_DOWNLOAD_DICTIONARY		= 3;
	// Using static field to ensure one and only one SingleThreadExecutor created.
	private static ExecutorService	es								= Executors.newFixedThreadPool(3);
	private static ExecutorService	es2								= Executors.newCachedThreadPool();

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
			newIntent.setData(uri);
			startService(newIntent);
			break;
		case COMMAND_DOWNLOAD_DICTIONARIES:
			Uri wordBankUri = intent.getData();
			String type = getContentResolver().getType(wordBankUri);
			Preconditions.checkState(StringUtils.equals(WordBankColumns.CONTENT_TYPE_WORD, type));
			if (intent.getBooleanExtra(NO_WAIT, false)) {
				es2.execute(AbstractDictionaryCommand.newDictionaryCommand(this, wordBankUri, Dictionary.DICTIONARY_DREYE_DICTIONARY));
				es2.execute(AbstractDictionaryCommand.newDictionaryCommand(this, wordBankUri, Dictionary.DICTIONARY_DICTIONARY_DICTIONARY));
				es2.execute(AbstractDictionaryCommand.newDictionaryCommand(this, wordBankUri, Dictionary.DICTIONARY_WIKITIONARY));
			} else {
				es.execute(AbstractDictionaryCommand.newDictionaryCommand(this, wordBankUri, Dictionary.DICTIONARY_DREYE_DICTIONARY));
				es.execute(AbstractDictionaryCommand.newDictionaryCommand(this, wordBankUri, Dictionary.DICTIONARY_DICTIONARY_DICTIONARY));
				es.execute(AbstractDictionaryCommand.newDictionaryCommand(this, wordBankUri, Dictionary.DICTIONARY_WIKITIONARY));
			}
			break;
		default:
			break;
		}
	}

}
