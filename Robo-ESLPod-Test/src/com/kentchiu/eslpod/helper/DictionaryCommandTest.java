package com.kentchiu.eslpod.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class DictionaryCommandTest extends AndroidTestCase {
	private MyDictionaryCommand	command;
	private Uri					wordBankUri;

	public void testGetContent() throws Exception {
		String content = command.getContent("test");
		assertThat(content, is("**test**"));
	}

	public void testReadAsOneLine() throws Exception {
		String line = command.readAsOneLine("http://www.google.com");
		assertThat(line, containsString("<input"));
	}

	public void testUpdateDatabase() throws Exception {
		command.updateDatabase("test");
		String dictId = Long.toString(command.getDictionaryId());
		String wordId = Long.toString(ContentUris.parseId(wordBankUri));
		String selection = DictionaryColumns.DICTIONARY_ID + "=? and " + DictionaryColumns.WORD_ID + "=?";
		Cursor c2 = mContext.getContentResolver().query(DictionaryColumns.DICTIONARY_URI, null, selection, new String[] { dictId, wordId }, null);
		assertThat(c2.getCount(), equalTo(1));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mContext.getContentResolver().delete(WordBankColumns.WORDBANK_URI, null, null);
		ContentValues cv = new ContentValues();
		cv.put(WordBankColumns.WORD, "test");
		wordBankUri = mContext.getContentResolver().insert(WordBankColumns.WORDBANK_URI, cv);
		command = new MyDictionaryCommand(mContext, wordBankUri);
	}

}

class MyDictionaryCommand extends DictionaryCommand {

	public MyDictionaryCommand(Context context, Uri wordBankUri) {
		super(context, wordBankUri);
	}

	@Override
	protected String getContent(String word) throws IOException {
		return "**" + word + "**";
	}

	@Override
	protected int getDictionaryId() {
		return 99;
	}

	@Override
	protected String getQueryUrl(String word) {
		return null;
	}

}