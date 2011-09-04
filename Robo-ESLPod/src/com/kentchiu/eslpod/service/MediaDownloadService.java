package com.kentchiu.eslpod.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaDownloadService extends Service {
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;
	private Handler							handler;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.MIN_PRIORITY);
		builder.setDaemon(true);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				new ContentValues();
			}
		};

		commandQueue = new ArrayBlockingQueue<Runnable>(10);
		executorService = new ThreadPoolExecutor(2, // core size
				2, // max size
				30 * 60, // idle timeout
				TimeUnit.SECONDS, commandQueue, builder.build(), new AbortPolicy()); // queue with a size

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String url = intent.getStringExtra(PodcastColumns.MEDIA_URL);
		Preconditions.checkNotNull(url);
		try {
			URL from = new URL(url);
			String name = StringUtils.substringAfterLast(from.getFile(), "/");
			// TODO change to SD card
			File to = new File(getCacheDir(), name);
			MediaCommand cmd = new MediaCommand(from, to, handler);
			executorService.execute(cmd);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
