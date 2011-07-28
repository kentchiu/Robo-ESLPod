package com.kentchiu.eslpod.provider;

import org.apache.commons.lang.StringUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.provider.task.DownloadRichScriptTask;

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
		switch (getUriMatcher().match(uri)) {
		case PODCASTS:
			return PodcastColumns.CONTENT_TYPE_PODCASTS;
		case PODCAST:
			return PodcastColumns.CONTENT_TYPE_PODCAST;
		default:
			throw new IllegalArgumentException("Unknow uri : " + uri);
		}
	}

	public UriMatcher getUriMatcher() {
		return uriMatcher;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (getUriMatcher().match(uri)) {
		case PODCASTS:
			long rowId = db.insert(DatabaseHelper.PODCAST_TABLE_NAME, null, values);
			Log.d(EslPodApplication.LOG_TAG, "insert pocast data");
			Uri url = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return url;
			//		case MEDIA:
			//			ContentValues mediaValues = new ContentValues();
			//			mediaValues.put("_data", values.getAsString("_data"));
			//			rowId = db.insert(DatabaseHelper.MEDIA_TABLE_NAME, null, mediaValues);
			//			url = ContentUris.withAppendedId(MediaColumns.MEDIA_URI, rowId);
			//			getContext().getContentResolver().notifyChange(uri, null);
			//			if (rowId != -1) {
			//				ContentValues podcastValues = new ContentValues();
			//				podcastValues.put(PodcastColumns.MEDIA_ID, rowId);
			//				update(Uri.parse(values.getAsString("podcastUri")), podcastValues, null, null);
			//			}
			//			return url;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		getUriMatcher().addURI(Podcast.AUTHORITY, "podcast", PODCASTS);
		getUriMatcher().addURI(Podcast.AUTHORITY, "podcast" + "/#", PODCAST);
		getUriMatcher().addURI(Podcast.AUTHORITY, "media", MEDIA);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final Cursor queryCursor;
		switch (getUriMatcher().match(uri)) {
		case PODCASTS:
			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, where, whereArgs, null, null, sortOrder);
			Log.i(EslPodApplication.LOG_TAG, "send uri" + uri);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case PODCAST:
			final long podcastId = ContentUris.parseId(uri);
			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, BaseColumns._ID + " = " + podcastId, whereArgs, null, null, null);
			queryCursor.moveToFirst();
			int linkIdx = queryCursor.getColumnIndex(PodcastColumns.LINK);
			int richScriptIdx = queryCursor.getColumnIndex(PodcastColumns.RICH_SCRIPT);
			queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
			String link = queryCursor.getString(linkIdx);
			Log.i(EslPodApplication.LOG_TAG, "Retrive rich script content from :" + link);

			if (StringUtils.isBlank(queryCursor.getString(richScriptIdx))) {
				final Uri uri2 = uri;
				new DownloadRichScriptTask(getContext(), podcastId, uri2).execute(link);
			}
			break;
		//		case MEDIA:
		//			mediaID =
		//			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, Podcast.PodcastColumns.MEDIA_ID + " = " + mediaID, whereArgs, null, null, null);
		//			break;

		default:
			throw new IllegalArgumentException("unsupported uri: " + uri);
		}
		return queryCursor;
	}

	public void setUriMatcher(UriMatcher uriMatcher) {
		this.uriMatcher = uriMatcher;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (getUriMatcher().match(uri)) {
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
