package com.kentchiu.eslpod.service;

import roboguice.service.RoboIntentService;
import android.app.AlarmManager;
import android.content.Intent;

import com.google.inject.Inject;

public class AutoFetchService extends RoboIntentService {

	@Inject
	private AlarmManager	alarmManager;

	public AutoFetchService() {
		super("AutoFetch");
	}

	public AutoFetchService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//		Intent service = new Intent(this, WordFetchService.class);
		//		service.setData(PodcastColumns.PODCAST_URI);
		//		PendingIntent pi = PendingIntent.getService(this, 0, service, 0);
		//		alarmManager.setInexactRepeating(AlarmManager.RTC, 60000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
	}

}
