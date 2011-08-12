package com.kentchiu.eslpod.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static String	DATABASE_NAME			= "elspod.db";
	public static int		DATABASE_VERSION		= 9;
	public static String	PODCAST_TABLE_NAME		= "podcast";
	public static String	DICTIONARY_TABLE_NAME	= "dictionary";

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
		super(context, name, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createPodcastTable(sqLiteDatabase);
		createDictionaryTable(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PODCAST_TABLE_NAME + ";");
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DICTIONARY_TABLE_NAME + ";");
		createPodcastTable(sqLiteDatabase);
		createDictionaryTable(sqLiteDatabase);
	}

	private void createDictionaryTable(SQLiteDatabase sqLiteDatabase) {
		// @formatter:off
		String sql = "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
		BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		DictionaryColumns.WORD + " TEXT NOT NULL, " +
		DictionaryColumns.DICTIONARY_ID + " INTEGER NOT NULL, " +
		DictionaryColumns.CONTENT + " TEXT " +
		");";
		String sql2= "CREATE UNIQUE INDEX [IDX_DICT_DICTIONID_WORD] ON [" + DICTIONARY_TABLE_NAME + "]([" + DictionaryColumns.DICTIONARY_ID + "]  DESC, [" + DictionaryColumns.WORD + "]  DESC)";

		// @formatter:on
		Log.i(EslPodApplication.TAG, sql);
		sqLiteDatabase.execSQL(sql);
		// create unique index
		sqLiteDatabase.execSQL(sql2);
	}

	private void createPodcastTable(SQLiteDatabase sqLiteDatabase) {
		// @formatter:off
		String sql = "CREATE TABLE " + PODCAST_TABLE_NAME + " (" +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		PodcastColumns.TITLE + " TEXT UNIQUE, " +
		PodcastColumns.SUBTITLE + " TEXT, " +
		PodcastColumns.MEDIA_URL + " TEXT UNIQUE, " +
		PodcastColumns.MEDIA_URL_LOCAL + " TEXT UNIQUE, " +
		PodcastColumns.MEDIA_LENGTH+ " INTEGER, " +
		PodcastColumns.PUBLISHED+ " TEXT, " +
		PodcastColumns.LINK  + " TEXT UNIQUE," +
		PodcastColumns.DURATION  + " TEXT, " +
		PodcastColumns.SCRIPT + " TEXT, " +
		PodcastColumns.RICH_SCRIPT + " TEXT, " +
		PodcastColumns.TAGS + " TEXT, " +
		PodcastColumns.PARAGRAPH_INDEX + " TEXT " +
		");";
		// @formatter:on
		Log.i(EslPodApplication.TAG, sql);
		sqLiteDatabase.execSQL(sql);
	}
}