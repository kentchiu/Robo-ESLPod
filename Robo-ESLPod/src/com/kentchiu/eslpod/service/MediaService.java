package com.kentchiu.eslpod.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaService extends Service {

	private MediaPlayer	player;


	public MediaPlayer getPlayer() {
		return player;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder<MediaService>(this);
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

	public void prepare(Uri uri,  OnPreparedListener listener) {
		Log.i(EslPodApplication.TAG, "working uri:" + uri);
		Preconditions.checkNotNull(uri);
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
		prepare(url, listener);
	}

	private void prepare(String url, OnPreparedListener listener) {
		if (url == null) return ;
		try {
			File file = new File(url);
			if (file.exists()) {
				player.setDataSource(new FileInputStream(file).getFD());
				player.prepareAsync();
				player.setOnPreparedListener(listener);
			}
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}