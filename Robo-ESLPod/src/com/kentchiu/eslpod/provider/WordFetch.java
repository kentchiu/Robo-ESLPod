package com.kentchiu.eslpod.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class WordFetch {
	public static final class WordFetchColumns implements BaseColumns {
		public static final Uri		WORD_FETCH_URI			= Uri.parse("content://" + WordFetch.AUTHORITY + "/wordFetch");
		public static final String	CONTENT_TYPE_PODCASTS	= "vnd.android.cursor.dir/vnd.eslpod.wordFetch";
		public static final String	CONTENT_TYPE_PODCAST	= "vnd.android.cursor.item/vnd.eslpod.wordFetch";
		public static final int		STATUS_DOWNLOADABLE		= 0;															// not yet download
		public static final int		STATUS_DOWNLOADED		= 1;
		public static final int		STATUS_DOWNLOADING		= 2;
		public static final int		STATUS_DOWNLOAD_FAIL	= 3;

		/*
		 * column name
		 */
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	WORD					= "word";
		/**
		 * <P>Type: INTEGER</P>
		 * {@link #STATUS_DOWNLOADABLE} or {@link #STATUS_DOWNLOADING} or {@link #STATUS_DOWNLOADED}
		 */
		public static final String	STATUS					= "status";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	DICTIONARY_ID			= "dictionary_id";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	PODCAST_ID				= "podcast_id";

		private WordFetchColumns() {
		}
	}

	public static final String	AUTHORITY	= WordFetch.class.getName();
}
