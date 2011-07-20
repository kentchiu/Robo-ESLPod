package com.kentchiu.eslpod.provider;

import static com.kentchiu.eslpod.provider.Podcast.ContentType.MEDIA;
import static com.kentchiu.eslpod.provider.Podcast.ContentType.PODCAST;
import static com.kentchiu.eslpod.provider.Podcast.ContentType.PODCASTS;
import static com.kentchiu.eslpod.provider.Podcast.ContentType.getByCode;

import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.ContentType;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.provider.task.DownloadRichScriptTask;

public class PodcastContentProvider extends ContentProvider {
	private UriMatcher		uriMatcher;
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
		int match = getUriMatcher().match(uri);
		return Podcast.ContentType.getByCode(match).getIdentifier();
	}

	public UriMatcher getUriMatcher() {
		return uriMatcher;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int m = getUriMatcher().match(uri);
		if (m != PODCASTS.getCode()) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		long rowId = db.insert(DatabaseHelper.PODCAST_TABLE_NAME, null, values);
		Log.d(EslPodApplication.LOG_TAG, "insert pocast data");
		Uri url = ContentUris.withAppendedId(Podcast.PODCAST_URI, rowId);
		getContext().getContentResolver().notifyChange(uri, null);
		return url;
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		getUriMatcher().addURI(Podcast.AUTHORITY, "podcast", PODCASTS.getCode());
		getUriMatcher().addURI(Podcast.AUTHORITY, "podcast" + "/#", PODCAST.getCode());
		getUriMatcher().addURI(Podcast.AUTHORITY, "media" + "/#", MEDIA.getCode());
		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		// only support read only files
		if (!"r".equals(mode.toLowerCase())) {
			throw new FileNotFoundException("Unsupported mode, " + mode + ", for uri: " + uri);
		}

		return openFileHelper(uri, mode);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int match = getUriMatcher().match(uri);
		ContentType type = getByCode(match);
		final Cursor queryCursor;
		switch (type) {
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

			Uri requestTag = ContentUris.withAppendedId(Podcast.PODCAST_URI, podcastId);
			Log.d(EslPodApplication.LOG_TAG, "requestTag:" + requestTag.toString());
			//asyncQueryRequest(requestTag.toString(), link);
			break;
		case MEDIA:
			String uriString = uri.toString();
			int lastSlash = uriString.lastIndexOf("/");
			String mediaID = uriString.substring(lastSlash + 1);
			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, Podcast.PodcastColumns.MEDIA_ID + " = " + mediaID, whereArgs, null, null, null);
			break;

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
		int match = getUriMatcher().match(uri);
		ContentType type = getByCode(match);
		Log.d(EslPodApplication.LOG_TAG, "type:" + type);
		switch (type) {
		case PODCAST:
			int update = db.update(DatabaseHelper.PODCAST_TABLE_NAME, values, where, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return update;
		default:

		}
		return 0;
	}

}
