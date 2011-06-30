package com.kentchiu.eslpod.provider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.kentchiu.eslpod.provider.Podcast.ContentType;

public class PodcastContentTypeTest {

	@Test
	public void getCode() {
		assertEquals(1, ContentType.PODCASTS.getId());
		assertEquals(2, ContentType.PODCAST.getId());
		assertEquals(3, ContentType.MEDIA.getId());
	}

	@Test
	public void getIdentifier() throws Exception {
		assertEquals("vnd.android.cursor.dir/vnd.eslpod.podcast", ContentType.PODCASTS.getIdentifier());
		assertEquals("vnd.android.cursor.item/vnd.eslpod.podcast", ContentType.PODCAST.getIdentifier());
		assertEquals("vnd.android.cursor.item/vnd.eslpod.media", ContentType.MEDIA.getIdentifier());
	}

}
