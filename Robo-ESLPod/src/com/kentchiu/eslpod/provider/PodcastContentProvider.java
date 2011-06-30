package com.kentchiu.eslpod.provider;

import static com.kentchiu.eslpod.provider.Podcast.ContentType.MEDIA;
import static com.kentchiu.eslpod.provider.Podcast.ContentType.PODCAST;
import static com.kentchiu.eslpod.provider.Podcast.ContentType.PODCASTS;
import static com.kentchiu.eslpod.provider.Podcast.ContentType.getById;

import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.ContentType;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastContentProvider extends ContentProvider {
	private static final String	FILE_CACHE_DIR	= "/data/data/com.kentchiu.eslpod/file_cache";
	private static final String	QUERY_URI		= "http://feeds.feedburner.com/EnglishAsASecondLanguagePodcast";

	private UriMatcher			uriMatcher;
	private DatabaseHelper		openHelper;
	private SQLiteDatabase		db;

	//@Override
	//public int delete(Uri uri, String where, String[] whereArgs) {
	//		int match = uriMatcher.match(uri);
	//		int affected;
	//
	//		SQLiteDatabase db = openHelper.getWritableDatabase();
	//		switch (match) {
	//		case VIDEOS:
	//			affected = db.delete(DatabaseHelper., (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	//			break;
	//		case VIDEO_ID:
	//			long videoId = ContentUris.parseId(uri);
	//			affected = db.delete(VIDEOS_TABLE_NAME, BaseColumns._ID + "=" + videoId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	//			getContext().getContentResolver().notifyChange(uri, null);
	//
	//			break;
	//		default:
	//			throw new IllegalArgumentException("unknown video element: " + uri);
	//		}
	//
	//		return affected;
	//	return 0;
	//}

	public PodcastContentProvider() {
		super();
		init();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		int match = getUriMatcher().match(uri);
		return Podcast.ContentType.getById(match).getIdentifier();
	}

	public UriMatcher getUriMatcher() {
		return uriMatcher;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = openHelper.getWritableDatabase();
		int m = getUriMatcher().match(uri);
		if (m != PODCASTS.getId()) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		long rowId = db.insert(DatabaseHelper.PODCAST_TABLE_NAME, null, values);
		Log.d(EslPodApplication.LOG_TAG, "insert data : " + values.toString());
		return ContentUris.withAppendedId(Podcast.PODCAST_URI, rowId);

	}

	@Override
	public boolean onCreate() {
		init();
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
		db = openHelper.getWritableDatabase();
		int match = getUriMatcher().match(uri);
		ContentType type = getById(match);
		Cursor queryCursor;
		switch (type) {
		case PODCASTS:
			// the query is passed out of band of other information passed
			// to this method -- its not an argument.
			String queryText = uri.getQueryParameter("last");

			if (queryText == null) {
				queryText = "10";
			}

			// quickly return already matching data
			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, where, whereArgs, null, null, sortOrder);

			Log.i(EslPodApplication.LOG_TAG, "send uri" + uri);
			// make the cursor observe the requested query
			queryCursor.setNotificationUri(getContext().getContentResolver(), uri);

			/**
			 * Always try to update results with the latest data from the
			 * network.
			 *
			 * Spawning an asynchronous load task thread, guarantees that
			 * the load has no chance to block any content provider method,
			 * and therefore no chance to block the UI thread.
			 *
			 * While the request loads, we return the cursor with existing
			 * data to the client.
			 *
			 * If the existing cursor is empty, the UI will render no
			 * content until it receives URI notification.
			 * Content updates that arrive when the asynchronous network
			 * request completes will appear in the already returned cursor,
			 * since that cursor query will match that of
			 * newly arrived items.
			 */
			//asyncQueryRequest(Podcast.PODCAST_URI.toString(), QUERY_URI);
			//Thread t = new Thread(requestTask);
			break;
		case PODCAST:
			long podcastId = ContentUris.parseId(uri);
			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, BaseColumns._ID + " = " + podcastId, whereArgs, null, null, null);
			queryCursor.moveToFirst();
			queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
			String link = queryCursor.getString(PodcastColumns.INDEX_OF_LINK);
			Log.i(EslPodApplication.LOG_TAG, "Retrive rich script content from :" + link);
			Uri requestTag = ContentUris.withAppendedId(Podcast.PODCAST_URI, podcastId);
			Log.d(EslPodApplication.LOG_TAG, "requestTag:" + requestTag.toString());
			//asyncQueryRequest(requestTag.toString(), link);
			break;
		case MEDIA:
			String uriString = uri.toString();
			int lastSlash = uriString.lastIndexOf("/");
			String mediaID = uriString.substring(lastSlash + 1);
			queryCursor = db.query(DatabaseHelper.PODCAST_TABLE_NAME, projection, Podcast.PodcastColumns.MEDIA_ID + " = " + mediaID, whereArgs, null, null, null);
			queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
			break;

		default:
			throw new IllegalArgumentException("unsupported uri: " + QUERY_URI);
		}

		return queryCursor;
	}

	public void setUriMatcher(UriMatcher uriMatcher) {
		this.uriMatcher = uriMatcher;
	}

	//	protected ResponseHandler newResponseHandler(String requestTag) {
	//		int match = getUriMatcher().match(Uri.parse(requestTag));
	//		ContentType type = getById(match);
	//		Log.d(EslPodApplication.TAG_NAME, "type:" + type);
	//		switch (type) {
	//		case PODCASTS:
	//			return new PodcastHandler(this);
	//		case PODCAST:
	//			return new RichScriptHandler(this, Uri.parse(requestTag));
	//		default:
	//			return null;
	//		}
	//
	//	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		getContext().getContentResolver().notifyChange(uri, null);
		//
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int match = getUriMatcher().match(uri);
		ContentType type = getById(match);
		Log.d(EslPodApplication.LOG_TAG, "type:" + type);
		switch (type) {

		case PODCAST:
			return db.update(DatabaseHelper.PODCAST_TABLE_NAME, values, where, whereArgs);
		default:

		}
		//		case VIDEOS:
		//			count = db.update(VIDEOS_TABLE_NAME, values, where, whereArgs);
		//			break;
		//
		//		case VIDEO_ID:
		//			String videoId = uri.getPathSegments().get(1);
		//			count = db.update(VIDEOS_TABLE_NAME, values, BaseColumns._ID + "=" + videoId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
		//			break;
		//
		//		default:
		//			throw new IllegalArgumentException("Unknown URI " + uri);
		//		}
		//
		//		getContext().getContentResolver().notifyChange(uri, null);
		//		return count;
		return 0;
	}

	private void init() {
		openHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		//fileHandlerFactory = new FileHandlerFactory(FILE_CACHE_DIR);
		setUriMatcher(new UriMatcher(UriMatcher.NO_MATCH));
		getUriMatcher().addURI(Podcast.AUTHORITY, "podcast", PODCASTS.getId());
		getUriMatcher().addURI(Podcast.AUTHORITY, "podcast" + "/#", PODCAST.getId());
		getUriMatcher().addURI(Podcast.AUTHORITY, "media" + "/#", MEDIA.getId());
	}

	private Long mediaExists(SQLiteDatabase db, String mediaID) {
		Cursor cursor = null;
		Long rowID = null;
		try {
			cursor = db.query(DatabaseHelper.DATABASE_NAME, null, PodcastColumns.MEDIA_ID + " = '" + mediaID + "'", null, null, null, null);
			if (cursor.moveToFirst()) {
				rowID = cursor.getLong(PodcastColumns.INDEX_OF_ID);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return rowID;
	}

	//	private void verifyValues(ContentValues values) {
	//		if (!values.containsKey(FinchVideo.Videos.TITLE)) {
	//			Resources r = Resources.getSystem();
	//			values.put(FinchVideo.Videos.TITLE, r.getString(android.R.string.untitled));
	//		}
	//
	//		if (!values.containsKey(FinchVideo.Videos.DESCRIPTION)) {
	//			Resources r = Resources.getSystem();
	//			values.put(FinchVideo.Videos.DESCRIPTION, r.getString(android.R.string.untitled));
	//		}
	//
	//		if (!values.containsKey(FinchVideo.Videos.THUMB_URI_NAME)) {
	//			throw new IllegalArgumentException("Thumb uri not specified: " + values);
	//		}
	//
	//		if (!values.containsKey(FinchVideo.Videos.THUMB_WIDTH_NAME)) {
	//			throw new IllegalArgumentException("Thumb width not specified: " + values);
	//		}
	//
	//		if (!values.containsKey(FinchVideo.Videos.THUMB_HEIGHT_NAME)) {
	//			throw new IllegalArgumentException("Thumb height not specified: " + values);
	//		}
	//
	//		// Make sure that the fields are all set
	//		if (!values.containsKey(FinchVideo.Videos.TIMESTAMP)) {
	//			Long now = System.currentTimeMillis();
	//			values.put(FinchVideo.Videos.TIMESTAMP, now);
	//		}
	//
	//		if (!values.containsKey(FinchVideo.Videos.QUERY_TEXT_NAME)) {
	//			throw new IllegalArgumentException("Query Text not specified: " + values);
	//		}
	//
	//		if (!values.containsKey(FinchVideo.Videos.MEDIA_ID_NAME)) {
	//			throw new IllegalArgumentException("Media ID not specified: " + values);
	//		}
	//	}
}

class DatabaseHelper extends SQLiteOpenHelper {
	public static int			DATABASE_VERSION	= 9;
	public static String		PODCAST_TABLE_NAME	= "podcast";
	public static final String	DATABASE_NAME		= "elspod.db";

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
		super(context, name, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createTable(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PODCAST_TABLE_NAME + ";");
		createTable(sqLiteDatabase);
	}

	private void createTable(SQLiteDatabase sqLiteDatabase) {
		// @formatter:off
		String createPodcastTable = "CREATE TABLE " + PODCAST_TABLE_NAME + " (" +
		BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		PodcastColumns.TITLE + " TEXT UNIQUE, " +
		PodcastColumns.SUBTITLE + " TEXT, " +
		PodcastColumns.MEDIA_URI                   + " TEXT UNIQUE, " +
		PodcastColumns.MEDIA_ID                  + " INTEGER, " +
		PodcastColumns.MEDIA_LENGTH                  + " INTEGER, " +
		PodcastColumns._DATA               + " TEXT UNIQUE, "	+
		PodcastColumns.PUBLISHED               + " TEXT, " +
		PodcastColumns.LINK              + " TEXT UNIQUE," +
		PodcastColumns.DURATION           + " TEXT, " +
		PodcastColumns.SCRIPT + " TEXT, " +
		PodcastColumns.RICH_SCRIPT + " TEXT, " +
		PodcastColumns.TAGS + " TEXT, " +
		PodcastColumns.PARAGRAPH_INDEX + " TEXT " +
		");";
		// @formatter:on
		Log.i(EslPodApplication.LOG_TAG, createPodcastTable);
		sqLiteDatabase.execSQL(createPodcastTable);
	}
}
