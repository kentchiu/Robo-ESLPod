package com.kentchiu.eslpod.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;
import com.kentchiu.eslpod.service.DictionaryService;

public class DictionaryContentProvider extends ContentProvider {

	private static final int	WORDS			= 1;
	private static final int	WORD			= 2;
	private static final int	DICTIONARIES	= 3;
	private static final int	DICTIONARY		= 4;
	private UriMatcher			uriMatcher;
	private DatabaseHelper		databaseHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case WORDS:
			return WordBankColumns.CONTENT_TYPE_WORDS;
		case WORD:
			return WordBankColumns.CONTENT_TYPE_WORD;
		case DICTIONARIES:
			return DictionaryColumns.CONTENT_TYPE_DICTIONARIES;
		case DICTIONARY:
			return DictionaryColumns.CONTENT_TYPE_DICTIONARY;
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
			id = db.insert(DatabaseHelper.WORD_BANK_TABLE_NAME, null, values);
			//			Intent intent = new Intent(this.getContext(), DictionaryService.class);
			//			intent.putExtra(DictionaryService.COMMAND, DictionaryService.COMMAND_DOWNLOAD_DICTIONARIES);
			//			intent.setData(ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, id));
			//			getContext().startService(intent);
			getContext().getContentResolver().notifyChange(WordBankColumns.WORDBANK_URI, null);
			break;
		case DICTIONARIES:
			id = db.insert(DatabaseHelper.DICTIONARY_TABLE_NAME, null, values);
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
		uriMatcher.addURI(Dictionary.AUTHORITY, "word", WORDS);
		uriMatcher.addURI(Dictionary.AUTHORITY, "word" + "/#", WORD);
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict", DICTIONARIES);
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict" + "/#", DICTIONARY);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case WORDS:
			Cursor c = db.query(DatabaseHelper.WORD_BANK_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			if (c.getCount() == 0) {
				Intent intent = new Intent(getContext(), DictionaryService.class);
				intent.putExtra(DictionaryService.COMMAND, DictionaryService.COMMAND_DOWNLOAD_WORD);
				intent.putExtra(SearchManager.QUERY, selectionArgs[0]);
				getContext().startService(intent);
			}
			return c;
		case WORD:
			long id = ContentUris.parseId(uri);
			return db.query(DatabaseHelper.WORD_BANK_TABLE_NAME, projection, BaseColumns._ID + "=?", new String[] { Long.toString(id) }, null, null, sortOrder);
		case DICTIONARIES:
			return db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		case DICTIONARY:
			id = ContentUris.parseId(uri);
			return db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, BaseColumns._ID + "=?", new String[] { Long.toString(id) }, null, null, sortOrder);
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
