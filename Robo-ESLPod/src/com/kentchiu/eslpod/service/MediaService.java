package com.kentchiu.eslpod.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaService extends Service {
	public static String	ACTION_PREPARE	= "com.kentchiu.eslpod.intent.action.PREPARE";
	public static String	ACTION_PLAY		= "com.kentchiu.eslpod.intent.action.PLAY";
	public static String	ACTION_PAUSE	= "com.kentchiu.eslpod.intent.action.PAUSE";
	public static String	ACTION_STOP		= "com.kentchiu.eslpod.intent.action.STOP";

	private MediaPlayer		player;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		player = new MediaPlayer();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		player.stop();
		player.release();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (StringUtils.equals(ACTION_PREPARE, intent.getAction())) {
			final Uri uri = intent.getData();
			Log.i(EslPodApplication.TAG, "working uri:" + uri);
			Preconditions.checkNotNull(uri);
			final Cursor c = getContentResolver().query(uri, null, null, null, null);
			c.moveToFirst();
			String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
			String path = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
			initPlayer(url, path);
		} else if (StringUtils.equals(ACTION_PLAY, intent.getAction())) {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.start();
			}
		} else {
			throw new IllegalArgumentException("Unknow action : " + intent.getAction());
		}
		return super.onStartCommand(intent, flags, startId);

	}

	private void initPlayer(String url, String path) {
		try {
			if (StringUtils.isNotBlank(url)) {
				File file = new File(url);
				if (file.exists()) {
					player.setDataSource(new FileInputStream(file).getFD());
					player.prepareAsync();
				} else {
					player.setDataSource(path);
					player.prepareAsync();
				}
			} else {
				player.setDataSource(path);
				player.prepareAsync();
			}
			Log.d(EslPodApplication.TAG, "media url : " + path);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
