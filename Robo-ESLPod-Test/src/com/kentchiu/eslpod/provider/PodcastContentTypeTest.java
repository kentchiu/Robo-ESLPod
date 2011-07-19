package com.kentchiu.eslpod.provider;

import junit.framework.TestCase;

import com.kentchiu.eslpod.provider.Podcast.ContentType;

public class PodcastContentTypeTest extends TestCase {

	public void testGetCode() {
		assertEquals(1, ContentType.PODCASTS.getCode());
		assertEquals(2, ContentType.PODCAST.getCode());
		assertEquals(3, ContentType.MEDIA.getCode());
	}

	public void testGetIdentifier() throws Exception {
		assertEquals("vnd.android.cursor.dir/vnd.eslpod.podcast", ContentType.PODCASTS.getIdentifier());
		assertEquals("vnd.android.cursor.item/vnd.eslpod.podcast", ContentType.PODCAST.getIdentifier());
		assertEquals("vnd.android.cursor.item/vnd.eslpod.media", ContentType.MEDIA.getIdentifier());
	}

}
