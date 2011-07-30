package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.DatabaseHelper;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class GoogleSuggestCommandTest extends AndroidTestCase {
	private GoogleSuggestCommand	command;
	private SQLiteDatabase			database;

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
		DatabaseHelper helper = new DatabaseHelper(mContext, DatabaseHelper.DATABASE_NAME, null);
		database = helper.getWritableDatabase();
		database.execSQL("delete from word_bank");
		database.execSQL("delete from dictionary");
		database.execSQL("insert into word_bank(_id, word) values(1, 'test') ");
		Uri wordBankUri = ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, 1);
		command = new GoogleSuggestCommand(mContext, wordBankUri);
		//		ContentValues cv = new ContentValues();
		//		cv.put(WordBankColumns.WORD, "test");
		//		Uri uri = mContext.getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
	}

}
