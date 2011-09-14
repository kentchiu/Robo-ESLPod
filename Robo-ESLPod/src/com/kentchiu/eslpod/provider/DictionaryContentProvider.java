package com.kentchiu.eslpod.provider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.view.EslPodApplication;

public class DictionaryContentProvider extends ContentProvider {

	private static final int		WORDS	= 1;
	private static final int		WORD	= 2;
	private UriMatcher				uriMatcher;
	private DatabaseHelper			databaseHelper;
	private static ExecutorService	es		= Executors.newFixedThreadPool(3);
	private Handler					handler;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (uriMatcher.match(uri)) {
		case WORDS:
		default:
			return 0;
		}
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case WORDS:
			return DictionaryColumns.CONTENT_TYPE_WORDS;
		case WORD:
			return DictionaryColumns.CONTENT_TYPE_WORD;
		default:
			throw new IllegalArgumentException("Unknow URI : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final long id;
		switch (uriMatcher.match(uri)) {
		case WORDS:
			id = db.insert(DatabaseHelper.DICTIONARY_TABLE_NAME, null, values);
			Log.v(EslPodApplication.TAG, "save word [" + values.getAsString(DictionaryColumns.WORD) + "] to dictionary " + values.getAsLong(DictionaryColumns.DICTIONARY_ID));
			getContext().getContentResolver().notifyChange(DictionaryColumns.DICTIONARY_URI, null);
			break;
		default:
			throw new IllegalArgumentException("url  not matched");
		}
		return ContentUris.withAppendedId(uri, id);

	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(Dictionary.AUTHORITY, "word/", WORDS);
		uriMatcher.addURI(Dictionary.AUTHORITY, "word/#", WORD);
		handler = new MyHandler(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case WORDS:
			Cursor c = db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			/*
			Set<Integer> dictIds = Sets.newHashSet();
			while (c.moveToNext()) {
				dictIds.add(c.getInt(c.getColumnIndex(DictionaryColumns.DICTIONARY_ID)));
			}
			HashSet<Integer> allDictIds = listAllDictIds();
			Iterables.removeAll(allDictIds, dictIds);
			String query = StringUtils.trim(selectionArgs[0]);
			Log.v(EslPodApplication.TAG, "There are "+ allDictIds.size() +  " dictionary need to be update for word [" + query + "]");
			for (Integer each : allDictIds) {
				es.execute(AbstractDictionaryCommand.newDictionaryCommand(handler, query, each));
			}
			c.requery();
			*/
			return c;
		case WORD:
			long id;
			id = ContentUris.parseId(uri);
			c = db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, BaseColumns._ID + "=?", new String[] { Long.toString(id) }, null, null, sortOrder);
			return c;
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}

class MyHandler extends Handler {

	private Context	context;

	public MyHandler(Context context) {
		super();
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle b = msg.getData();
		int dictId = b.getInt(DictionaryColumns.DICTIONARY_ID);
		String query = b.getString(DictionaryColumns.WORD);
		String content = b.getString(DictionaryColumns.CONTENT);
		Log.v(EslPodApplication.TAG, "save word definition of [" + query + "] to dictionary " + dictId);
		ContentValues cv = new ContentValues();
		cv.put(DictionaryColumns.DICTIONARY_ID, dictId);
		cv.put(DictionaryColumns.WORD, query);
		cv.put(DictionaryColumns.CONTENT, content);
		getContext().getContentResolver().insert(DictionaryColumns.DICTIONARY_URI, cv);
	}

	public void setContext(Context context) {
		this.context = context;
	}
}