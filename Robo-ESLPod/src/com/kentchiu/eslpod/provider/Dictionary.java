package com.kentchiu.eslpod.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Dictionary {

	public static final class DictionaryColumns implements BaseColumns {
		public static final Uri		DICTIONARY_URI				= Uri.parse("content://" + Dictionary.AUTHORITY + "/dict");
		public static final String	CONTENT_TYPE_DICTIONARIES	= "vnd.android.cursor.dir/vnd.eslpod.dict";
		public static final String	CONTENT_TYPE_DICTIONARY		= "vnd.android.cursor.item/vnd.eslpod.dict";
		public static final String	WORD_ID						= "word_id";
		public static final String	DICTIONARY_ID				= "dictionary_id";
		public static final String	CONTENT						= "content";

		private DictionaryColumns() {
		};
	}

	public static final class WordBankColumns implements BaseColumns {
		public static final String	WORD				= "word";
		public static final Uri		WORDBANK_URI		= Uri.parse("content://" + Dictionary.AUTHORITY + "/word");
		public static final String	CONTENT_TYPE_WORDS	= "vnd.android.cursor.dir/vnd.eslpod.word";
		public static final String	CONTENT_TYPE_WORD	= "vnd.android.cursor.item/vnd.eslpod.word";

		private WordBankColumns() {
		};
	}

	public static final String	AUTHORITY						= Dictionary.class.getName();
	public static final int		DICTIONARY_GOOGLE_SUGGESTION	= 1;
	public static final int		DICTIONARY_GOOGLE_DICTIONARY	= 2;
	public static final int		DICTIONARY_WIKI_DICTIONARY		= 3;


}
