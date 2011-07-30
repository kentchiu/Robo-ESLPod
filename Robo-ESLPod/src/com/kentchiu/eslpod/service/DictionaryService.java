package com.kentchiu.eslpod.service;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.GoogleSuggestCommand;
import com.kentchiu.eslpod.cmd.WikiCommand;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class DictionaryService extends IntentService {

	public static final String	COMMAND							= "command";
	public static final int		COMMAND_DOWNLOAD_WORD			= 1;
	public static final int		COMMAND_DOWNLOAD_DICTIONARIES	= 2;

	public DictionaryService() {
		super(DictionaryService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int cmd = intent.getIntExtra(COMMAND, -1);
		Log.i(EslPodApplication.TAG, "Execute command [" + cmd + "]");
		switch (cmd) {
		case COMMAND_DOWNLOAD_WORD:
			String query = intent.getStringExtra(SearchManager.QUERY);
			Preconditions.checkNotNull(query);
			ContentValues cv = new ContentValues();
			cv.put(WordBankColumns.WORD, query);
			Uri uri = getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
			Intent newIntent = new Intent(this, DictionaryService.class);
			newIntent.putExtra(COMMAND, COMMAND_DOWNLOAD_DICTIONARIES);
			newIntent.setData(uri);
			startService(newIntent);
			break;
		case COMMAND_DOWNLOAD_DICTIONARIES:
			Uri wordBankUri = intent.getData();
			long wordId = ContentUris.parseId(wordBankUri);
			new Thread(new GoogleSuggestCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId))).start();
			new Thread(new WikiCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId))).start();
		default:
			break;
		}
	}

}
