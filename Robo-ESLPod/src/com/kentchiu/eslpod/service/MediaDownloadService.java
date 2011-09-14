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
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.EslPodApplication;

public class MediaDownloadService extends Service {
	private static final int				MAX_TASK	= 5;
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;
	private Handler							downloadHandler;

	public Handler createDownloadHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String from = msg.getData().getString("from");
				String to = msg.getData().getString("to");
				if (from == null) {
					Log.w(EslPodApplication.TAG, "Download fail with illegal message " + msg);
					return;
				}
				switch (msg.what) {
				case MediaCommand.DOWNLOAD_START:
					break;
				case MediaCommand.DOWNLOAD_PROCESSING:
					break;
				case MediaCommand.DOWNLOAD_COMPLETED:
					ContentValues cv = new ContentValues();
					cv.put(PodcastColumns.MEDIA_URL_LOCAL, to);
					int count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
					if (count != 1) {
						Log.w(EslPodApplication.TAG, "exception row updated but " + count);
					}
					break;
				default:
					Log.w(EslPodApplication.TAG, "Unknow message " + msg);
				}

			}
		};
	}

	public void download(Uri uri) throws MalformedURLException {
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		if (c.moveToFirst()) {
			String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
			Preconditions.checkNotNull(url);
			URL from = new URL(url);
			String name = StringUtils.substringAfterLast(from.getFile(), "/");
			File to = new File(getDownloadFolder(), name);
			MediaCommand cmd = new MediaCommand(from, to, downloadHandler);
			executorService.execute(cmd);
		}
	}

	public ArrayBlockingQueue<Runnable> getCommandQueue() {
		return commandQueue;
	}

	public Handler getDownloadHandler() {
		return downloadHandler;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder<MediaDownloadService>(this);
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
		downloadHandler = createDownloadHandler();
	}

	public void setDownloadHandler(Handler downloadHandler) {
		this.downloadHandler = downloadHandler;
	}

	private File getDownloadFolder() {
		String state = Environment.getExternalStorageState();
		if (StringUtils.equals(Environment.MEDIA_MOUNTED, state)) {
			return getExternalCacheDir();
		} else {
			Log.w(EslPodApplication.TAG, "SD card no found, save to internl storage");
			return getCacheDir();
		}
	}

}
