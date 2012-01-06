package com.kentchiu.eslpod.service;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;
import android.widget.RemoteViews.ActionException;

public class MusicServiceTest extends ServiceTestCase<MusicService> {

	public MusicServiceTest() {
		super(MusicService.class);
	}
	
	@Override
	public Context getSystemContext() {
		return super.getSystemContext();
	}
	
	@Override
	public Context getContext() {
		return super.getContext();
	}
	
	public void testPlayMusic() throws Exception {
		Intent intent = new Intent(MusicService.ACTION_PLAY);
		intent.setData(ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, 1));
		intent.putExtra("foo", "bar");
		startService(intent);
		
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
