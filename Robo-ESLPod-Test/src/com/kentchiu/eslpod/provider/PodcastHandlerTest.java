package com.kentchiu.eslpod.provider;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Node;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;

import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastHandlerTest extends AndroidTestCase {


	private MockContentResolver mockResolver;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mockResolver = new MockContentResolver();
		mockResolver.addProvider("com.kentchiu.eslpod.provider.Podcast", new MockContentProvider(null) {
			@Override
			public Uri insert(Uri uri, ContentValues values) {
				return uri;
			}
		});
	}

	public void testInsert() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastHandler h = new PodcastHandler(mockResolver, inputStream);
		h.run();
	}


	public void testCreateContentValue() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastHandler h = new PodcastHandler(mockResolver, inputStream);
		List<Node> items = h.getItemNodes();
		Node node = Iterables.get(items, 5);
		ContentValues values = h.convert(node);
		assertEquals("http://www.eslpod.com/website/show_podcast.php?issue_id=10231549", values.getAsString(PodcastColumns.LINK));
		assertEquals("Slow dialogue: 0:59,Explanations: 2:32,Fast dialogue: 16:26", values.getAsString(PodcastColumns.PARAGRAPH_INDEX));
		assertEquals("Fri, 29 Apr 2011 03:00:13 -0400", values.getAsString(PodcastColumns.PUBLISHED));
		String script = values.getAsString(PodcastColumns.SCRIPT);
		assertTrue(script.startsWith("Jim: "));
		assertTrue(script.endsWith("Script by Dr. Lucy Tse"));
		assertEquals("Talking about someone’s religion can sometimes cause controversy.  Learn what not to say in this episode.", values.getAsString(PodcastColumns.SUBTITLE));
		assertEquals("681 - Disagreeing about Religion", values.getAsString(PodcastColumns.TITLE));
	}

	public void testGetItemNodes() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastHandler h = new PodcastHandler(mockResolver, inputStream);
		assertEquals(86, Iterables.size(h.getItemNodes()));
	}
}
