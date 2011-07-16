package com.kentchiu.eslpod.provider;

import junit.framework.TestCase;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelperTest extends TestCase {
	private DatabaseHelper	helper;
	private SQLiteDatabase	db;

	@Override
	public void tearDown() {
		helper.close();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = SQLiteDatabase.create(null);
		helper = new DatabaseHelper(null, "podcast.db", null);
		helper.onOpen(db);
		helper.onCreate(db);
	}

	public void testPodcastTable() throws Exception {
		Cursor c = db.query(DatabaseHelper.PODCAST_TABLE_NAME, null, null, null, null, null, null);
		assertEquals(14, c.getColumnCount());
	}

	public void testWordBankTable() throws Exception {
		Cursor c = db.query(DatabaseHelper.WORD_BANK_TABLE_NAME, null, null, null, null, null, null);
		assertEquals(2, c.getColumnCount());
	}
	public void testDictionaryBankTable() throws Exception {
		Cursor c = db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, null, null, null, null, null, null);
		assertEquals(4, c.getColumnCount());
	}

}
