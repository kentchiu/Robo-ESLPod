package com.kentchiu.eslpod.provider;

import android.net.Uri;

public class Dictionary {

	enum ContentType {
		DICTIONARIES {
			@Override
			public int getCode() {
				return 1;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.dir/vnd.eslpod.dict";
			}
		},
		DICTIONARY {
			@Override
			public int getCode() {
				return 2;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.item/vnd.eslpod.dict";
			}
		},
		WORDS {
			@Override
			public int getCode() {
				return 3;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.dir/vnd.eslpod.word";
			}

		},
		WORD {
			@Override
			public int getCode() {
				return 4;
			}

			@Override
			public String getIdentifier() {
				return "vnd.android.cursor.item/vnd.eslpod.word";
			}
		};
		public static ContentType getByCode(int id) {
			switch (id) {
			case 1:
				return DICTIONARIES;
			case 2:
				return DICTIONARY;
			case 3:
				return WORDS;
			case 4:
				return WORD;
			default:
				throw new IllegalArgumentException("Unknow id : " + id);
			}
		}

		public abstract int getCode();

		public abstract String getIdentifier();
	}

	public static final String	AUTHORITY						= Dictionary.class.getName();
	public static final Uri		DICTIONARY_URI					= Uri.parse("content://" + AUTHORITY + "/dict");
	public static final Uri		WORDBANK_URI					= Uri.parse("content://" + AUTHORITY + "/word");
	public static final String	WORD							= "word";
	public static final String	WORD_ID							= "word_id";
	public static final String	ID								= "_id";
	public static final String	DICTIONARY_ID					= "dictionary_id";
	public static final String	CONTENT							= "content";
	public static final int		DICTIONARY_GOOGLE_SUGGESTION	= 1;
	public static final int		DICTIONARY_WIKI_DICTIONARY		= 2;

}
