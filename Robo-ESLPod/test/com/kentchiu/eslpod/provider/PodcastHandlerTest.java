package com.kentchiu.eslpod.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

import android.content.ContentValues;

import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

@RunWith(MyRobolectricTestRunner.class)
public class PodcastHandlerTest {

	@Test
	public void createContentValue() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastHandler h = new PodcastHandler(null);
		List<Node> items = h.getItemNodes(inputStream);
		Node node = Iterables.get(items, 5);
		ContentValues values = h.convert(node);

		//assertEquals("", values.getAsString(PodcastColumns.DURATION));
		assertEquals("http://www.eslpod.com/website/show_podcast.php?issue_id=10231549", values.getAsString(PodcastColumns.LINK));
		assertEquals("Slow dialogue: 0:59,Explanations: 2:32,Fast dialogue: 16:26", values.getAsString(PodcastColumns.PARAGRAPH_INDEX));
		//assertEquals("", values.getAsString(PodcastColumns.MEDIA_ID));
		//assertEquals("", values.getAsString(PodcastColumns.MEDIA_LENGTH));
		//assertEquals("", values.getAsString(PodcastColumns.MEDIA_URI));
		//assertEquals("", values.getAsString(PodcastColumns.PARAGRAPH_INDEX));
		assertEquals("Fri, 29 Apr 2011 03:00:13 -0400", values.getAsString(PodcastColumns.PUBLISHED));
		String script = values.getAsString(PodcastColumns.SCRIPT);
		System.out.println(script);
		assertThat(script, startsWith("Jim: "));
		assertThat(script, endsWith("Script by Dr. Lucy Tse"));
		assertEquals("Talking about someone’s religion can sometimes cause controversy.  Learn what not to say in this episode.", values.getAsString(PodcastColumns.SUBTITLE));
		assertEquals("681 - Disagreeing about Religion", values.getAsString(PodcastColumns.TITLE));

		// remote
		//assertEquals("", values.getAsString(PodcastColumns.RICH_SCRIPT));
		//assertEquals("", values.getAsString(PodcastColumns.TAGS));
	}

	@Test
	public void getItemNodes() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/podcast.xml");
		PodcastHandler h = new PodcastHandler(null);
		assertEquals(86, Iterables.size(h.getItemNodes(inputStream)));
	}
}
