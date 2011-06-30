package com.kentchiu.eslpod.provider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.entity.ContentProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class PodcastContentProviderTest {


	private SQLiteDatabase	mockDatabase;
	private PodcastContentProvider	contentProvider;
	private Context	mockContext;



	@Before
	public void setUp() {
		mockDatabase = mock(SQLiteDatabase.class);
		mockContext = mock(Context.class);
		contentProvider = new PodcastContentProvider();
		contentProvider.attachInfo(mockContext, null);
	}


	@Test
	public void getType() throws Exception {
		assertEquals(1, new PodcastContentProvider().getType(Podcast.PODCAST_URI));
	}

	@Test
	public void insert() throws Exception {
		ContentValues values = new ContentValues();
		long rowId = 2L;
		when(mockDatabase.insert("podcast", null, values)).thenReturn(rowId);
		Uri r = contentProvider.insert(Podcast.PODCAST_URI, values, mockDatabase);
		assertEquals("content://com.kentchiu.eslpod.provider.Podcast/podcast/2", r.toString());
	}


	@Test
	public void query() throws Exception {
		contentProvider.setDatabase(mockDatabase);
		Cursor mockCursor = mock(Cursor.class);
		ContentResolver mock = mock(ContentResolver.class);
		when(mockContext.getContentResolver()).thenReturn(mock);
		when(mockDatabase.query(DatabaseHelper.PODCAST_TABLE_NAME, null, null, null, null, null, null)).thenReturn(mockCursor );
		contentProvider.query(Podcast.PODCAST_URI, null, null, null, null);
		verify(mockCursor).setNotificationUri(isA(ContentResolver.class), isA(Uri.class));
	}

}
