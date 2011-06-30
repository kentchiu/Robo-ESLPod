package com.kentchiu.eslpod.provider;

import junit.framework.TestCase;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelperTest extends TestCase {
	private DatabaseHelper	helper;

	@Override
	public void tearDown() {
		helper.close();
	}

	public void testOnCreate() throws Exception {
		SQLiteDatabase db = SQLiteDatabase.create(null);
		helper = new DatabaseHelper(null, "podcast.db", null);
		helper.onOpen(db);
		helper.onCreate(db);
		Cursor c = db.query("podcast", new String[] { "_id" }, null, null, null, null, null);
		helper.close();
		assertNotNull(c);
		assertEquals(1, c.getColumnCount());
	}
}
