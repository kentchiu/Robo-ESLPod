package com.kentchiu.eslpod.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static String	DATABASE_NAME			= "elspod.db";
	public static int		DATABASE_VERSION		= 1;
	public static String	PODCAST_TABLE_NAME		= "podcast";
	public static String	WORD_BANK_TABLE_NAME	= "word_bank";
	public static String	DICTIONARY_TABLE_NAME	= "dictionary";

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
		super(context, name, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createPodcastTable(sqLiteDatabase);
		createWordBankTable(sqLiteDatabase);
		createDictionaryTable(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PODCAST_TABLE_NAME + ";");
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WORD_BANK_TABLE_NAME + ";");
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DICTIONARY_TABLE_NAME + ";");
		createPodcastTable(sqLiteDatabase);
		createWordBankTable(sqLiteDatabase);
		createDictionaryTable(sqLiteDatabase);
	}

	private void createDictionaryTable(SQLiteDatabase sqLiteDatabase) {
		// @formatter:off
		String sql = "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
		BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		"word_id" + " INTEGER, " +
		"dictionary_id" + " INTEGER, " +
		"content" + " TEXT " +
		");";
		// @formatter:on
		Log.i(EslPodApplication.LOG_TAG, sql);
		sqLiteDatabase.execSQL(sql);
	}

	private void createPodcastTable(SQLiteDatabase sqLiteDatabase) {
		// @formatter:off
		String sql = "CREATE TABLE " + PODCAST_TABLE_NAME + " (" +
		BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		PodcastColumns.TITLE + " TEXT UNIQUE, " +
		PodcastColumns.SUBTITLE + " TEXT, " +
		PodcastColumns.MEDIA_URI + " TEXT UNIQUE, " +
		PodcastColumns.MEDIA_ID+ " INTEGER, " +
		PodcastColumns.MEDIA_LENGTH+ " INTEGER, " +
		PodcastColumns._DATA+ " TEXT UNIQUE, "	+
		PodcastColumns.PUBLISHED+ " TEXT, " +
		PodcastColumns.LINK  + " TEXT UNIQUE," +
		PodcastColumns.DURATION  + " TEXT, " +
		PodcastColumns.SCRIPT + " TEXT, " +
		PodcastColumns.RICH_SCRIPT + " TEXT, " +
		PodcastColumns.TAGS + " TEXT, " +
		PodcastColumns.PARAGRAPH_INDEX + " TEXT " +
		");";
		// @formatter:on
		Log.i(EslPodApplication.LOG_TAG, sql);
		sqLiteDatabase.execSQL(sql);
	}

	private void createWordBankTable(SQLiteDatabase sqLiteDatabase) {
		// @formatter:off
		String sql = "CREATE TABLE " + WORD_BANK_TABLE_NAME + " (" +
		BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		"word" + " TEXT UNIQUE " +
		");";
		// @formatter:on
		Log.i(EslPodApplication.LOG_TAG, sql);
		sqLiteDatabase.execSQL(sql);
	}
}