package com.kentchiu.eslpod.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.kentchiu.eslpod.provider.Dictionary.ContentType;

public class DictionaryContentProvider extends ContentProvider {

	private UriMatcher		uriMatcher;
	private DatabaseHelper	databaseHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	@Override
	public String getType(Uri uri) {
		int id = uriMatcher.match(uri);
		return ContentType.getByCode(id).getIdentifier();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		ContentType type = ContentType.getByCode(uriMatcher.match(uri));
		long id;
		switch (type) {
		case DICTIONARIES:
			id = db.insert(DatabaseHelper.DICTIONARY_TABLE_NAME, null, values);
			break;
		case WORDS:
			id = db.insert(DatabaseHelper.WORD_BANK_TABLE_NAME, null, values);
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
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict", ContentType.DICTIONARIES.getCode());
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict" + "/#", ContentType.DICTIONARY.getCode());
		uriMatcher.addURI(Dictionary.AUTHORITY, "word", ContentType.WORDS.getCode());
		uriMatcher.addURI(Dictionary.AUTHORITY, "word" + "/#", ContentType.WORD.getCode());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		ContentType type = ContentType.getByCode(uriMatcher.match(uri));
		switch (type) {
		case WORDS:
			return db.query(DatabaseHelper.WORD_BANK_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		case DICTIONARIES:
			return db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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
