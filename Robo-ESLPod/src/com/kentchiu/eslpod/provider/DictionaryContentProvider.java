package com.kentchiu.eslpod.provider;

import roboguice.util.Ln;
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

import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public class DictionaryContentProvider extends ContentProvider {

	private static final int	WORDS	= 1;
	private static final int	WORD	= 2;
	private UriMatcher			uriMatcher;
	private DatabaseHelper		databaseHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// do nothing
		return 0;
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
			Ln.v("save word [%s] to dictionary %d ", values.getAsString(DictionaryColumns.WORD), values.getAsLong(DictionaryColumns.DICTIONARY_ID));
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
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case WORDS:
			Cursor c = db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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
		Ln.v("save word definition of [%s] to dictionary %d", query, dictId);
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