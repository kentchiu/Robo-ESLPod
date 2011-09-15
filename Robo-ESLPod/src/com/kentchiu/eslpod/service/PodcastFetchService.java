package com.kentchiu.eslpod.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.PodcastCommand;

public class PodcastFetchService extends Service {

	public static int	LOCAL_PODCAST_COUNT	= 5;

	public void fetchNew(Handler handler) throws MalformedURLException, IOException {
		InputStream is = new URL(PodcastCommand.RSS_URI).openStream();
		PodcastCommand podcastCommand = new PodcastCommand(PodcastFetchService.this, is, handler);
		// doInBackground is already in work thread, no need to using a new one
		Thread t = new Thread(podcastCommand);
		t.start();
	}

	public void importLocal(Handler handler) {
		InputStream is = getResources().openRawResource(R.raw.podcast_680_685);
		PodcastCommand podcastCommand = new PodcastCommand(PodcastFetchService.this, is, handler);
		// doInBackground is already in work thread, no need to using a new one
		Thread t = new Thread(podcastCommand);
		t.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder<PodcastFetchService>(this);
	}

}
