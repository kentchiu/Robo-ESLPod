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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.EslPodApplication;

public class MediaDownloadService extends Service {
	private static final int				MAX_TASK	= 10;
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
				ContentValues cv = new ContentValues();
				switch (msg.what) {
				case MediaCommand.DOWNLOAD_START:
					cv.put(PodcastColumns.MEDIA_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADING);
					int count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
					if (count != 1) {
						Log.w(EslPodApplication.TAG, "exception row updated but " + count);
					}
					break;
				case MediaCommand.DOWNLOAD_PROCESSING:
					if (msg.arg1 % 5 == 0) {
						cv.put(PodcastColumns.MEDIA_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADING);
						count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
						if (count != 1) {
							Log.w(EslPodApplication.TAG, "exception row updated but " + count);
						}
					}
					break;
				case MediaCommand.DOWNLOAD_COMPLETED:
					cv.put(PodcastColumns.MEDIA_URL_LOCAL, to);
					cv.put(PodcastColumns.MEDIA_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADED);
					count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
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
			// executorService.execute(cmd) will not execute command immediately, we need send download start message as soon as possible when download() invoked.
			cmd.sendMessage(MediaCommand.DOWNLOAD_START, 0, 0);
			executorService.execute(cmd);
			Log.d(EslPodApplication.TAG, "add download task, there are " + commandQueue.size()  + " in queue" );
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

	public int refreshStatus() {
		Cursor c = getContentResolver().query(PodcastColumns.PODCAST_URI, null, null, null, null);
		int count = 0;
		while (c.moveToNext()) {
			int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
			String path = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
			long length = c.getLong(c.getColumnIndex(PodcastColumns.MEDIA_LENGTH));
			int status = c.getInt(c.getColumnIndex(PodcastColumns.MEDIA_STATUS));

			int newStatus = getFileStatus(path, length);
			if (newStatus != status) {
				Log.d(EslPodApplication.TAG, "update download status of uri to " + newStatus);
				Uri uri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
				ContentValues values = new ContentValues();
				values.put(PodcastColumns.MEDIA_STATUS, newStatus);
				getContentResolver().update(uri, values, null, null);
				count++;
			}
		}
		return count++;
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

	private int getFileStatus(String path, long length) {
		int newStatus = 0;
		if (StringUtils.isBlank(path)) {
			newStatus = PodcastColumns.MEDIA_STATUS_DOWNLOADABLE;
		} else {
			File file = new File(path);
			if (file.exists() && file.length() == length) {
				newStatus = PodcastColumns.MEDIA_STATUS_DOWNLOADED;
			} else {
				newStatus = PodcastColumns.MEDIA_STATUS_DOWNLOADABLE;
			}
		}
		return newStatus;
	}

}
