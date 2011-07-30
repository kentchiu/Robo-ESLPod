package com.kentchiu.eslpod.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import android.content.ContentValues;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class GoogleSuggestCommandTest extends AndroidTestCase {
	private GoogleSuggestCommand	command;

	public void testGetContent() throws Exception {
		assertThat(command.getContent("test"), containsString("test"));
	}

	public void testGetDictionId() throws Exception {
		assertThat(command.getDictionaryId(), is(Dictionary.DICTIONARY_GOOGLE_SUGGESTION));
	}

	public void testQueryUri() throws Exception {
		assertThat(command.getQueryUrl("test"), is("http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=" + "test"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ContentValues cv = new ContentValues();
		cv.put(WordBankColumns.WORD, "test");
		Uri uri = mContext.getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
		command = new GoogleSuggestCommand(mContext, uri);
	}

}
