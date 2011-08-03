package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URL;

import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.DatabaseHelper;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaCommandTest extends AndroidTestCase {

	private MediaCommand	command;
	private SQLiteDatabase	database;

	public void testDownloadFrom() throws Exception {
		URL resource = getClass().getResource("/ESLPod700.mp3");
		File f = command.downloadFrom(resource.toString(), 12345);
		assertThat(f.exists(), is(true));
		assertThat(f.isFile(), is(true));
		assertThat(f.getAbsolutePath(), is("/data/data/com.kentchiu.eslpod/cache/ESLPod700.mp3"));
	}

	public void testUpdateDatabase() throws Exception {
		//URL resource = getClass().getResource("/ESLPod700.mp3");
		command.updateDatabase(new File("/foo/bar.mp3"));
		Cursor c = database.rawQuery("select * from podcast where _id=?", new String[] { Long.toString(1) });
		c.moveToFirst();
		assertThat(c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL)), is("/foo/bar.mp3"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		DatabaseHelper helper = new DatabaseHelper(mContext, DatabaseHelper.DATABASE_NAME, null);
		database = helper.getWritableDatabase();
		database.execSQL("delete from podcast");
		database.execSQL("insert into podcast(_id) values(1)");
		Uri wordBankUri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, 1);
		command = new MediaCommand(mContext, wordBankUri);
	}

}
