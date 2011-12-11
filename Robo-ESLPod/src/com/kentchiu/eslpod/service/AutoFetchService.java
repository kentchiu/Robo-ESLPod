package com.kentchiu.eslpod.service;

import com.google.inject.Inject;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.HomeActivity;

import roboguice.service.RoboIntentService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;

public class AutoFetchService extends RoboIntentService {
	
	@Inject
	private AlarmManager alarmManager;
	public AutoFetchService() {
		super("AutoFetch");
	}
	
	public AutoFetchService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
//		Cursor c = getContentResolver().query(PodcastColumns.PODCAST_URI, null, null, null, null);
//		while(c.moveToNext()) {
//			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
//			getContentResolver().query(WordFetchService, null, richScript, null, richScript)
//		}
		Intent service = new Intent(this, WordFetchService.class);
		service.setData(null);
		PendingIntent pi = PendingIntent.getService(this, 0,service, 0); 
		alarmManager.setInexactRepeating(AlarmManager.RTC, 60000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
	}

}
