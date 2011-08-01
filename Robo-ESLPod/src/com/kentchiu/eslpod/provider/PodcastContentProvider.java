package com.kentchiu.eslpod.provider;

import org.apache.commons.lang.StringUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.PodcastService;

public class PodcastContentProvider extends ContentProvider {

	private static final int	PODCASTS	= 1;
	private static final int	PODCAST		= 2;
	private static final int	MEDIA		= 3;

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
		case PODCASTS:
			return PodcastColumns.CONTENT_TYPE_PODCASTS;
		case PODCAST:
			return PodcastColumns.CONTENT_TYPE_PODCAST;
		default:
			throw new IllegalArgumentException("Unknow uri : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case PODCASTS:
			long rowId = db.insert(DatabaseHelper.PODCAST_TABLE_NAME, null, values);
			Log.d(EslPodApplication.TAG, "insert pocast data as id " + rowId);
			Uri url = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return url;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(Podcast.AUTHORITY, "podcast", PODCASTS);
		uriMatcher.addURI(Podcast.AUTHORITY, "podcast" + "/#", PODCAST);
		uriMatcher.addURI(Podcast.AUTHORITY, "media", MEDIA);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
		Log.d(EslPodApplication.TAG, "query uri :" + uri);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final Cursor c;
		switch (uriMatcher.match(uri)) {
		case PODCASTS:
			c = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, where, whereArgs, null, null, sortOrder);
			break;
		case PODCAST:
			long id = ContentUris.parseId(uri);
			c = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, BaseColumns._ID + "=?", new String[] { Long.toString(id) }, null, null, null);
			break;
		default:
			throw new IllegalArgumentException("unsupported uri: " + uri);
		}
		return c;
	}


	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case PODCAST:
			Long id = ContentUris.parseId(uri);
			int update;
			update = db.update(DatabaseHelper.PODCAST_TABLE_NAME, values, BaseColumns._ID + "=?", new String[] { id.toString() });
			getContext().getContentResolver().notifyChange(uri, null);
			return update;
		default:

		}
		return 0;
	}

}
