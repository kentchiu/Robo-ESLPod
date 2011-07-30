package com.kentchiu.eslpod.helper;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Node;

import android.content.ContentValues;
import android.test.AndroidTestCase;

import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastCommandTest extends AndroidTestCase {

	public void testCreateContentValue() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastCommand h = new PodcastCommand(mContext, inputStream);
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
		PodcastCommand h = new PodcastCommand(mContext, inputStream);
		assertEquals(86, Iterables.size(h.getItemNodes()));
	}

	public void testInsert() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastCommand h = new PodcastCommand(mContext, inputStream);
		h.run();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
}
