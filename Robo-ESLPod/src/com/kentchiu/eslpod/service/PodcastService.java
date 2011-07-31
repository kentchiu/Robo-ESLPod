package com.kentchiu.eslpod.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastService extends IntentService {

	public static final String	COMMAND						= "command";
	public static final int		COMMAND_FETCH_NEW_PODCAST	= 1;
	public static final int		COMMAND_RICH_SCRIPT			= 2;
	public static final int		COMMAND_DOWNLOAD_MEDIA		= 3;

	public PodcastService() {
		super(PodcastService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int cmd = intent.getIntExtra(COMMAND, -1);
		Uri podcastUri = intent.getData();
		switch (cmd) {
		case COMMAND_FETCH_NEW_PODCAST:
			InputStream is;
			try {
				is = new URL(PodcastCommand.RSS_URI).openStream();
				PodcastCommand command = new PodcastCommand(this, is);
				new Thread(command).start();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case COMMAND_RICH_SCRIPT:
			try {
				new Thread(new RichScriptCommand(this, podcastUri, new URL(intent.getStringExtra(PodcastColumns.LINK)))).start();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		case COMMAND_DOWNLOAD_MEDIA:
			new Thread(new MediaCommand(this, podcastUri)).start();

		default:
			break;
		}
	}

}
