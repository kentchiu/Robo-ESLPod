package com.kentchiu.eslpod.provider;

import roboguice.util.Ln;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

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
			long rowId = db.insertWithOnConflict(DatabaseHelper.PODCAST_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			Ln.v("insert pocast data as id %d", rowId);
			Uri result = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, rowId);
			getContext().getContentResolver().notifyChange(result, null);
			return result;
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
		Ln.v("query uri : %s", uri);
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
		Ln.v("update uri : %s", uri);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int result;
		switch (uriMatcher.match(uri)) {
		case PODCASTS:
			result = db.update(DatabaseHelper.PODCAST_TABLE_NAME, values, where, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case PODCAST:
			Long id = ContentUris.parseId(uri);
			result = db.update(DatabaseHelper.PODCAST_TABLE_NAME, values, BaseColumns._ID + "=?", new String[] { id.toString() });
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		default:
			result = 0;
		}
		return result;
	}

}
