package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.DatabaseHelper;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class DictionaryCommandTest extends AndroidTestCase {
	private MyDictionaryCommand	command;
	private Uri					wordBankUri;
	private SQLiteDatabase		database;

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
		Cursor c = database.rawQuery("select * from dictionary where dictionary_id =? and word_id = ?", new String[] { dictId, wordId });
		assertThat(c.getCount(), equalTo(1));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		DatabaseHelper helper = new DatabaseHelper(mContext, DatabaseHelper.DATABASE_NAME, null);
		database = helper.getWritableDatabase();
		database.execSQL("delete from word_bank");
		database.execSQL("delete from dictionary");
		database.execSQL("insert into word_bank(_id, word) values(1, 'test') ");
		wordBankUri = ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, 1);
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