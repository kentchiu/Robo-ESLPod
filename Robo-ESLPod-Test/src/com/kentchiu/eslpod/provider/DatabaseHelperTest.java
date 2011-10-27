package com.kentchiu.eslpod.provider;

import junit.framework.TestCase;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelperTest extends TestCase {
	private DatabaseHelper	helper;
	private SQLiteDatabase	db;

	@Override
	public void tearDown() {
		helper.close();
	}

	public void testDictionaryBankTable() throws Exception {
		Cursor c = db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, null, null, null, null, null, null);
		assertEquals(4, c.getColumnCount());
	}

	public void testPodcastTable() throws Exception {
		Cursor c = db.query(DatabaseHelper.PODCAST_TABLE_NAME, null, null, null, null, null, null);
		assertEquals(15, c.getColumnCount());
	}

	public void testUniquIndex() throws Exception {
		try {
			db.execSQL("insert into Dictionary(dictionary_id, word) values(1, 'test')");
			db.execSQL("insert into Dictionary(dictionary_id, word) values(1, 'test')");
			fail("Shoud throw uniqu constrain exception");
		} catch (SQLiteConstraintException e) {
			// do nothing
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = SQLiteDatabase.create(null);
		helper = new DatabaseHelper(null, "podcast.db", null);
		helper.onOpen(db);
		helper.onCreate(db);
	}

}
