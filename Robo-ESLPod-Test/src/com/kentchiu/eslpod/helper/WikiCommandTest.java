package com.kentchiu.eslpod.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import android.content.ContentValues;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class WikiCommandTest extends AndroidTestCase {
	private WikiCommand	command;

	public void testGetContent() throws Exception {
		assertThat(command.getContent("test"), containsString("test"));
	}

	public void testGetDictionId() throws Exception {
		assertThat(command.getDictionaryId(), is(Dictionary.DICTIONARY_WIKI_DICTIONARY));
	}

	public void testQueryUri() throws Exception {
		assertThat(command.getQueryUrl("test"), is("http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&rvexpandtemplates=true&titles=" + "test"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ContentValues cv = new ContentValues();
		cv.put(WordBankColumns.WORD, "test");
		Uri uri = mContext.getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
		command = new WikiCommand(mContext, uri);
	}

}
