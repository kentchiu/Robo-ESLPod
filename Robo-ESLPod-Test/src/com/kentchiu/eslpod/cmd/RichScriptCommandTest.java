package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.DatabaseHelper;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class RichScriptCommandTest extends AndroidTestCase {

	private RichScriptCommand	command;
	private SQLiteDatabase		database;

	public void testExtractScript() throws Exception {
		InputStream is = getClass().getResourceAsStream("/script.html");
		List<String> script = command.extractScript(IOUtils.readLines(is));
		assertThat(script, hasSize(56));
	}

	public void testGetScript() throws Exception {
		String script = command.getScript();
		assertThat(script, startsWith("Cherise:  <b>Rise and shine</b>!"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		URL resource = getClass().getResource("/script.html");
		DatabaseHelper databaseHelper = new DatabaseHelper(mContext, DatabaseHelper.DATABASE_NAME, null);
		database = databaseHelper.getWritableDatabase();
		database.execSQL("delete from podcast");
		database.execSQL("insert into podcast(_id, link) values(1, '" + resource.toString() + "')");
		command = new RichScriptCommand(mContext, ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, 1), null);
	}

}
