package com.kentchiu.eslpod.service;

import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

public class PodcastServiceTest extends ServiceTestCase<PodcastService> {

	public PodcastServiceTest() {
		super(PodcastService.class);
	}

	public void testFetchNewPodcast() throws Exception {
		Context context = getContext();
		Intent intent = new Intent(context, PodcastServiceTest.class);
		intent.setAction(PodcastService.FETCH_NEW_PODCAST);
		startService(intent);
		getService();

	}

}
