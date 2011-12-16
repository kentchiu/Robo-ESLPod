package com.kentchiu.eslpod.provider;

import roboguice.content.RoboContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.google.inject.Inject;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class WordFetchContentProvider extends RoboContentProvider {
	@Inject
	private DatabaseHelper	databaseHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	@Override
	public String getType(Uri uri) {

		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		long rowId = db.insertWithOnConflict(DatabaseHelper.WORD_FETCH_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		Uri result = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, rowId);
		getContext().getContentResolver().notifyChange(result, null);
		return result;
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		return db.update(DatabaseHelper.WORD_FETCH_TABLE_NAME, values, selection, selectionArgs);
	}

}
