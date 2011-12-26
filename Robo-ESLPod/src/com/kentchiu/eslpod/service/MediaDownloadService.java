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
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.MediaDownloadCommand;

public class MediaDownloadService extends Service {
	private static final int				MAX_TASK	= 10;
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;
	private Handler							downloadHandler;

	public void download(Uri uri) throws MalformedURLException {
		//		Cursor c = getContentResolver().query(uri, null, null, null, null);
		//		if (c.moveToFirst()) {
		//			String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
		//			Preconditions.checkNotNull(url);
		//			URL from = new URL(url);
		//			String name = StringUtils.substringAfterLast(from.getFile(), "/");
		//			File to = new File(MediaDownloadCommand.getDownloadFolder(MediaDownloadService.this), name);
		//			MediaDownloadCommand cmd = new MediaDownloadCommand(from, to, downloadHandler);
		//			// executorService.execute(cmd) will not execute command immediately, we need send download start message as soon as possible when download() invoked.
		//			cmd.sendMessage(MediaDownloadCommand.DOWNLOAD_START, 0, 0);
		//			executorService.execute(cmd);
		//			Ln.d("add download task, there are %d in queue", commandQueue.size());
		//		}
		MediaDownloadCommand cmd = new MediaDownloadCommand(uri, MediaDownloadService.this, downloadHandler);
		cmd.sendMessage(MediaDownloadCommand.DOWNLOAD_START, 0, 0);
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

	//	private int getFileStatus(String path, long length) {
	//		int newStatus = 0;
	//		if (StringUtils.isBlank(path)) {
	//			newStatus = PodcastColumns.STATUS_DOWNLOADABLE;
	//		} else {
	//			File file = new File(path);
	//			if (file.exists() && file.length() == length) {
	//				newStatus = PodcastColumns.STATUS_DOWNLOADED;
	//			} else {
	//				newStatus = PodcastColumns.STATUS_DOWNLOADABLE;
	//			}
	//		}
	//		return newStatus;
	//	}

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
			download(intent.getData());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	//	public int refreshStatus() {
	//		Cursor c = getContentResolver().query(PodcastColumns.PODCAST_URI, null, null, null, null);
	//		int count = 0;
	//		while (c.moveToNext()) {
	//			int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
	//			String path = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
	//			long length = c.getLong(c.getColumnIndex(PodcastColumns.MEDIA_LENGTH));
	//			int status = c.getInt(c.getColumnIndex(PodcastColumns.MEDIA_DOWNLOAD_STATUS));
	//
	//			int newStatus = getFileStatus(path, length);
	//			if (newStatus != status) {
	//				Ln.d("update download status of uri to %s", newStatus);
	//				Uri uri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
	//				ContentValues values = new ContentValues();
	//				values.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, newStatus);
	//				getContentResolver().update(uri, values, null, null);
	//				count++;
	//			}
	//		}
	//		return count++;
	//	}

	public void setDownloadHandler(Handler downloadHandler) {
		this.downloadHandler = downloadHandler;
	}

}
