package com.kentchiu.eslpod.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastContentProviderTest extends ProviderTestCase2<PodcastContentProvider> {

	public PodcastContentProviderTest() {
		super(PodcastContentProvider.class, PodcastContentProvider.class.getName());
	}

	public void testGetType() throws Exception {
		assertEquals("vnd.android.cursor.dir/vnd.eslpod.podcast", new PodcastContentProvider().getType(Podcast.PODCAST_URI));
	}

	public void testInsert() throws Exception {
		ContentValues values = new ContentValues();
		values.put(PodcastColumns.TITLE, "test");
		Uri r = getProvider().insert(Podcast.PODCAST_URI, values);
		assertEquals("content://com.kentchiu.eslpod.provider.Podcast/podcast/1", r.toString());
	}

	public void testQuery() throws Exception {
		Cursor c = getProvider().query(Podcast.PODCAST_URI, null, null, null, null);
		assertNotNull(c);
	}

	public void testUpdate() throws Exception {
		Integer id = 1;
		Uri uri = Uri.withAppendedPath(Podcast.PODCAST_URI, id.toString());
		ContentValues values = new ContentValues();
		getProvider().update(uri, values, BaseColumns._ID + "=" + id, null);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

}
