package com.kentchiu.eslpod.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Podcast {

	public static final class PodcastColumns implements BaseColumns {
		public static final String	DEFAULT_SORT_ORDER	= "modified DESC";

		/*
		 * column name
		 */
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	TITLE				= "title";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	SUBTITLE			= "subtitle";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	MEDIA_URI			= "media_uri";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	MEDIA_ID			= "media_id";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	MEDIA_LENGTH		= "media_length";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	_DATA				= "_data";
		/**
		 * <P>Type: TEXT (Date String)</P>
		 */
		public static final String	PUBLISHED			= "published";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	LINK				= "link";
		/**
		 * <P>Type: INTEGER</P>
		 */
		public static final String	DURATION			= "duration";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	SCRIPT				= "script";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	RICH_SCRIPT			= "rich_script";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	TAGS				= "tags";
		/**
		 * <P>Type: TEXT</P>
		 */
		public static final String	PARAGRAPH_INDEX		= "paragraph_index";

		private PodcastColumns() {
		}
	}

	enum ContentType {
		PODCASTS {
			@Override
			public int getId() {
				return 1;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.dir/vnd.eslpod.podcast";
			}
		},
		PODCAST {
			@Override
			public int getId() {
				return 2;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.item/vnd.eslpod.podcast";
			}
		},
		MEDIA {
			@Override
			public int getId() {
				return 3;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.item/vnd.eslpod.media";
			}
		};
		public static ContentType getById(int id) {
			switch (id) {
			case 1:
				return PODCASTS;
			case 2:
				return PODCAST;
			case 3:
				return MEDIA;
			default:
				throw new IllegalArgumentException("Unknow id : " + id);
			}
		}

		public abstract int getId();

		public abstract String getIdentifier();
	}

	public static final String	AUTHORITY	= Podcast.class.getName();

	public static final Uri		PODCAST_URI	= Uri.parse("content://" + AUTHORITY + "/podcast");

}
