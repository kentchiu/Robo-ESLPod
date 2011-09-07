package com.kentchiu.eslpod.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaDownloadService extends Service {
	private static final int	MAX_TASK	= 5;
	private ExecutorService		executorService;

	public ArrayBlockingQueue<Runnable> getCommandQueue() {
		return commandQueue;
	}

	public Handler getDownloadHandler() {
		return downloadHandler;
	}

	private ArrayBlockingQueue<Runnable>	commandQueue;
	private Handler							downloadHandler;

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
		internalHandler = new Handler();
	}

	public void download(Uri uri) {
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		if (c.moveToFirst()) {
			String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
			Preconditions.checkNotNull(url);
			try {
				URL from = new URL(url);
				String name = StringUtils.substringAfterLast(from.getFile(), "/");
				File to = new File(getDownloadFolder(), name);
				MediaCommand cmd = new MediaCommand(from, to, getDownloadHandler());
				executorService.execute(cmd);
			} catch (RejectedExecutionException e) {
				final String text = "Only " + MAX_TASK + " tasks allow, tyr it later";
				showMessage(text, Toast.LENGTH_SHORT);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	Handler	internalHandler;

	void showMessage(final String text, final int lengthLong) {
		internalHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MediaDownloadService.this, text, lengthLong).show();
			}
		});
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

	public void setDownloadHandler(Handler downloadHandler) {
		this.downloadHandler = downloadHandler;
	}

}
