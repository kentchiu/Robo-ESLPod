package com.kentchiu.eslpod.provider;

import android.net.Uri;

public class Dictionary {

	enum ContentType {
		DICTIONARIES {
			@Override
			public int getId() {
				return 1;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.dir/vnd.eslpod.dict";
			}
		},
		DICTIONARY {
			@Override
			public int getId() {
				return 2;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.item/vnd.eslpod.dict";
			}
		};
		public static ContentType getById(int id) {
			switch (id) {
			case 1:
				return DICTIONARIES;
			case 2:
				return DICTIONARY;
			default:
				throw new IllegalArgumentException("Unknow id : " + id);
			}
		}

		public abstract int getId();

		public abstract String getIdentifier();
	}

	public static final String	AUTHORITY		= Dictionary.class.getName();

	public static final Uri		DICTIONARY_URI	= Uri.parse("content://" + AUTHORITY + "/dict");

	public static final int	DICTIONARY_GOOGLE_SUGGESTION	= 1;
}
