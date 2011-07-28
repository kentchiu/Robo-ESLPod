package com.kentchiu.eslpod.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.kentchiu.eslpod.helper.DownloadMediaCommand;

public class MediaDownloadService extends IntentService {

	public MediaDownloadService() {
		super(DictionaryService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final Uri podcastUri = intent.getData();
		new Thread(new DownloadMediaCommand(this, podcastUri)).run();
	}

}
