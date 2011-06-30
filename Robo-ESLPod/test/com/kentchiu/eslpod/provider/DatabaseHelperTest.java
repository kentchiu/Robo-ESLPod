package com.kentchiu.eslpod.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xtremelabs.robolectric.shadows.ShadowSQLiteDatabase;

@RunWith(MyRobolectricTestRunner.class)
public class DatabaseHelperTest {
	private DatabaseHelper	helper;

	@Test
	public void onCreate() throws Exception {
		SQLiteDatabase db = ShadowSQLiteDatabase.openDatabase("podcast.db", null, 0);
		helper = new DatabaseHelper(null, "podcast.db", null);
		helper.onOpen(db);
		helper.onCreate(db);
		Cursor c = db.query("podcast", new String[] { "_id" }, null, null, null, null, null);
		helper.close();
		assertNotNull(c);
		assertEquals(1, c.getColumnCount());
	}

	public void tearDown() {
		helper.close();

	}
}
