package com.kentchiu.eslpod.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastContentProviderTest extends ProviderTestCase2<PodcastContentProvider> {

	private SQLiteDatabase	db;

	public PodcastContentProviderTest() {
		super(PodcastContentProvider.class, PodcastContentProvider.class.getName());
	}

	public void testGetType() throws Exception {
		assertEquals("vnd.android.cursor.dir/vnd.eslpod.podcast", getProvider().getType(Podcast.PODCAST_URI));
	}

	public void testInsert() throws Exception {
		ContentValues values = new ContentValues();
		values.put(PodcastColumns.TITLE, "test");
		Uri r = getProvider().insert(Podcast.PODCAST_URI, values);
		assertEquals("content://com.kentchiu.eslpod.provider.Podcast/podcast/4", r.toString());
	}

	public void testQuery() throws Exception {
		Cursor c = getProvider().query(Podcast.PODCAST_URI, null, null, null, null);
		assertEquals(3, c.getCount());
	}

	public void testUpdate() throws Exception {
		Integer id = 1;
		Uri uri = Uri.withAppendedPath(Podcast.PODCAST_URI, id.toString());
		ContentValues values = new ContentValues();
		values.put(PodcastColumns.TITLE, "test");
		getProvider().update(uri, values, "_ID=?", new String[] { "1" });
		Cursor c = db.rawQuery("select TITLE from podcast where _ID=1", null);
		c.moveToFirst();
		assertEquals("test", c.getString(0));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PodcastContentProvider provider = getProvider();
		db = provider.getDatabaseHelper().getWritableDatabase();
		db.execSQL("insert into podcast(TITLE) values('title1')");
		db.execSQL("insert into podcast(TITLE) values('title2')");
		db.execSQL("insert into podcast(TITLE) values('title3')");
	}

}
