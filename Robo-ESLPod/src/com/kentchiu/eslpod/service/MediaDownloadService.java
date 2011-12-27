package com.kentchiu.eslpod.service;

import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import roboguice.util.Ln;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.MediaDownloadCommand;

public class MediaDownloadService extends Service {
	private static final int				MAX_TASK	= 10;
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;
	private Handler							downloadHandler;

	public void download(Intent intent) throws MalformedURLException {
		MediaDownloadCommand cmd = new MediaDownloadCommand(MediaDownloadService.this, intent, downloadHandler);
		executorService.execute(cmd);
		Ln.d("add download task, there are %d in queue", commandQueue.size());
	}

	public ArrayBlockingQueue<Runnable> getCommandQueue() {
		return commandQueue;
	}

	public Handler getDownloadHandler() {
		return downloadHandler;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.MIN_PRIORITY);
		builder.setDaemon(true);
		commandQueue = new ArrayBlockingQueue<Runnable>(MAX_TASK);
		executorService = new ThreadPoolExecutor(2, // core size
				2, // max size
				30 * 60, // idle timeout
				TimeUnit.SECONDS, commandQueue, builder.build(), new AbortPolicy()); // queue with a size
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			download(intent);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void setDownloadHandler(Handler downloadHandler) {
		this.downloadHandler = downloadHandler;
	}

}
