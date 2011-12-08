package com.kentchiu.eslpod.service;

import roboguice.service.RoboService;
import android.content.Intent;
import android.os.IBinder;

public class PodcastFetchService extends RoboService {

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder<PodcastFetchService>(this);
	}

}
