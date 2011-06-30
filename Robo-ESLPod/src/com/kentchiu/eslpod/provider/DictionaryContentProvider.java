package com.kentchiu.eslpod.provider;

import static com.kentchiu.eslpod.provider.Dictionary.ContentType.DICTIONARIES;
import static com.kentchiu.eslpod.provider.Dictionary.ContentType.DICTIONARY;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.kentchiu.eslpod.provider.Dictionary.ContentType;

public class DictionaryContentProvider extends ContentProvider {

	private UriMatcher	uriMatcher;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		int id = uriMatcher.match(uri);
		return ContentType.getById(id).getIdentifier();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict", DICTIONARIES.getId());
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict" + "/#", DICTIONARY.getId());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
