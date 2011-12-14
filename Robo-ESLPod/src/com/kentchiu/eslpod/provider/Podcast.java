package com.kentchiu.eslpod.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Podcast {

	public static final class PodcastColumns implements BaseColumns {
		public static final Uri		PODCAST_URI					= Uri.parse("content://" + Podcast.AUTHORITY + "/podcast");
		public static final String	CONTENT_TYPE_PODCASTS		= "vnd.android.cursor.dir/vnd.eslpod.podcast";
		public static final String	CONTENT_TYPE_PODCAST		= "vnd.android.cursor.item/vnd.eslpod.podcast";
		public static final int		STATUS_DOWNLOADABLE			= 0;														// not yet download
		public static final int		STATUS_DOWNLOADED			= 1;
		public static final int		STATUS_DOWNLOADING			= 2;

		/*
		 * column name
		 */
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	TITLE						= "title";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	SUBTITLE					= "subtitle";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	MEDIA_URL					= "media_url";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	MEDIA_URL_LOCAL				= "media_url_local";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	MEDIA_LENGTH				= "media_length";
		/**
		 * <P>Type: INTEGER</P>
		 * {@link #STATUS_DOWNLOADABLE} or {@link #STATUS_DOWNLOADING} or {@link #STATUS_DOWNLOADED}
		 */
		public static final String	MEDIA_DOWNLOAD_STATUS		= "media_download_status";
		/**
		 * <P>Type: INTEGER</P>
		 * {@link #STATUS_DOWNLOADABLE} or {@link #STATUS_DOWNLOADING} or {@link #STATUS_DOWNLOADED}
		 */
		public static final String	DICTIONARY_DOWNLOAD_STATUS	= "dict_download_status";
		/**
		 * <P>Type: TEXT (Date String)</P>
		 */
		public static final String	PUBLISHED					= "published";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	LINK						= "link";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	DURATION					= "duration";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	SCRIPT						= "script";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	RICH_SCRIPT					= "rich_script";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	TAGS						= "tags";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	PARAGRAPH_INDEX				= "paragraph_index";

		private PodcastColumns() {
		}
	}

	public static final String	AUTHORITY	= Podcast.class.getName();
}
