package com.kentchiu.eslpod.provider;

import static com.kentchiu.eslpod.provider.Podcast.PodcastColumns.MEDIA_URL_LOCAL;
import static com.kentchiu.eslpod.provider.Podcast.PodcastColumns.PODCAST_URI;
import static com.kentchiu.eslpod.provider.Podcast.PodcastColumns.TITLE;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class PodcastContentProviderTest extends ProviderTestCase2<PodcastContentProvider> {

	private SQLiteDatabase	db;

	public PodcastContentProviderTest() {
		super(PodcastContentProvider.class, PodcastContentProvider.class.getName());
	}

	public void testGetType() throws Exception {
		assertEquals("vnd.android.cursor.dir/vnd.eslpod.podcast", getProvider().getType(PODCAST_URI));
	}

	public void testInsert_episode() throws Exception {
		ContentValues values = new ContentValues();
		values.put(TITLE, "test");
		Uri r = getProvider().insert(PODCAST_URI, values);
		assertEquals("content://com.kentchiu.eslpod.provider.Podcast/podcast/4", r.toString());
	}

	public void testQuery() throws Exception {
		Cursor c = getProvider().query(PODCAST_URI, null, null, null, null);
		assertEquals(3, c.getCount());
	}

	public void testUpdate() throws Exception {
		Integer id = 1;
		Uri uri = Uri.withAppendedPath(PODCAST_URI, id.toString());
		ContentValues values = new ContentValues();
		values.put(TITLE, "test");
		values.put(MEDIA_URL_LOCAL, "bar");
		getProvider().update(uri, values, "_ID=?", new String[] { "1" });
		Cursor c = db.rawQuery("select TITLE, MEDIA_URL_LOCAL from podcast where _ID=1", null);
		c.moveToFirst();
		assertEquals("test", c.getString(0));
		assertEquals("bar", c.getString(1));
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
