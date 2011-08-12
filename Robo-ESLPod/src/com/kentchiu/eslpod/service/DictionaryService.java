package com.kentchiu.eslpod.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public class DictionaryService extends IntentService {

	public static final String		COMMAND					= "command";
	public static final String		NO_WAIT					= "no wait";
	public static final int			COMMAND_DOWNLOAD_WORD	= 1;
	// Using static field to ensure one and only one SingleThreadExecutor created.
	private static ExecutorService	es						= Executors.newFixedThreadPool(3);
	private static ExecutorService	es2						= Executors.newCachedThreadPool();
	private MyHandler				handler;

	public DictionaryService() {
		super(DictionaryService.class.getName());
		handler = new MyHandler();
		handler.setContext(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int cmd = intent.getIntExtra(COMMAND, -1);
		String query = intent.getStringExtra(SearchManager.QUERY);
		int dictId = intent.getIntExtra(DictionaryColumns.DICTIONARY_ID, -1);
		Preconditions.checkNotNull(query);
		Preconditions.checkState(dictId != -1);
		switch (cmd) {
		//		case COMMAND_DOWNLOAD_WORD:
		//			ContentValues cv = new ContentValues();
		//			cv.put(DictionaryColumns.WORD, query);
		//			Log.d(EslPodApplication.TAG, "save word [" + query + "] to bank");
		//			Uri uri = getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
		//			Log.v(EslPodApplication.TAG, "word [" + query + "] insert as uri :" + uri);
		//			Intent newIntent = new Intent(this, DictionaryService.class);
		//			newIntent.putExtra(COMMAND, COMMAND_DOWNLOAD_WORD);
		//			newIntent.putExtra(SearchManager.QUERY, query);
		//			newIntent.setData(uri);
		//			startService(newIntent);
		//			break;
		case COMMAND_DOWNLOAD_WORD:
			es.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, dictId));
			//			if (intent.getBooleanExtra(NO_WAIT, false)) {
			//				es2.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, Dictionary.DICTIONARY_DREYE_DICTIONARY));
			//				es2.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, Dictionary.DICTIONARY_DICTIONARY_DICTIONARY));
			//				es2.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, Dictionary.DICTIONARY_WIKITIONARY));
			//			} else {
			//				es.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, Dictionary.DICTIONARY_DREYE_DICTIONARY));
			//				es.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, Dictionary.DICTIONARY_DICTIONARY_DICTIONARY));
			//				es.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, Dictionary.DICTIONARY_WIKITIONARY));
			//			}

			break;
		default:
			break;
		}
	}

}

class MyHandler extends Handler {
	private Context	context;

	public Context getContext() {
		return context;
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle b = msg.getData();
		int dictId = b.getInt(DictionaryColumns.DICTIONARY_ID);
		String query = b.getString(DictionaryColumns.WORD);
		String content = b.getString(DictionaryColumns.CONTENT);
		Cursor c = getContext().getContentResolver().query(DictionaryColumns.DICTIONARY_URI, null, DictionaryColumns.WORD + "=? and " + DictionaryColumns.DICTIONARY_ID + "=?", new String[] { query, Long.toString(dictId) }, null);
		if (c.getCount() == 0) {
			Log.v(EslPodApplication.TAG, "save word definition of [" + query + "] to dictionary " + dictId);
			ContentValues cv = new ContentValues();
			cv.put(DictionaryColumns.DICTIONARY_ID, dictId);
			cv.put(DictionaryColumns.WORD, query);
			cv.put(DictionaryColumns.CONTENT, content);
			getContext().getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
		} else {
			Log.v(EslPodApplication.TAG, "word already exists in dictionary " + dictId);
		}

	}

	public void setContext(Context context) {
		this.context = context;
	}
}
