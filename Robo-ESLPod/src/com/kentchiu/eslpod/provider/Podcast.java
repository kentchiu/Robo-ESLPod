package com.kentchiu.eslpod.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Podcast {

	public static final class PodcastColumns implements BaseColumns {
		public static final String	DEFAULT_SORT_ORDER			= "modified DESC";

		/*
		 * column name
		 */
		public static final String	TITLE						= "title";
		public static final String	SUBTITLE					= "subtitle";
		public static final String	MEDIA_URI					= "media_uri";
		public static final String	MEDIA_ID					= "media_id";
		public static final String	MEDIA_LENGTH				= "media_length";
		public static final String	_DATA						= "_data";
		public static final String	PUBLISHED					= "published";
		public static final String	LINK						= "link";
		public static final String	DURATION					= "duration";
		public static final String	SCRIPT						= "script";
		public static final String	RICH_SCRIPT					= "rich_script";
		public static final String	TAGS						= "tags";
		public static final String	PARAGRAPH_INDEX				= "paragraph_index";
		/*
		 * column index
		 */
//		public static final int		INDEX_OF_ID					= 0;
//		public static final int		INDEX_OF_TITLE				= 1;
//		public static final int		INDEX_OF_SUBTITLE			= 2;
//		public static final int		INDEX_OF_MEDIA_URI			= 3;
//		public static final int		INDEX_OF_MEDIA_ID			= 4;
//		public static final int		INDEX_OF_MEDIA_LENGTH		= 5;
//		public static final int		INDEX_OF_DATA				= 6;
//		public static final int		INDEX_OF_PUBLISHED			= 7;
//		public static final int		INDEX_OF_LINK				= 8;
//		public static final int		INDEX_OF_DURATION			= 9;
//		public static final int		INDEX_OF_SCRIPT				= 10;
//		public static final int		INDEX_OF_RICH_SCRIPT		= 11;
//		public static final int		INDEX_OF_TAGS				= 12;
//		public static final int		INDEX_OF_PARAGRAPH_INDEX	= 13;

		// This class cannot be instantiated
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
