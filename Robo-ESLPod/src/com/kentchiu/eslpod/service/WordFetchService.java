package com.kentchiu.eslpod.service;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import roboguice.util.Ln;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.BaseColumns;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class WordFetchService extends Service {

	private class BatchDownloadCommand implements Runnable {

		private int					podcastId;

		private Iterable<String>	words;

		private boolean				isDone;

		private String				title;

		public BatchDownloadCommand(int podcastId) {
			super();
			this.podcastId = podcastId;
			Cursor c = getContentResolver().query(ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, podcastId), null, null, null, null);
			if (c.moveToFirst()) {
				String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
				title = c.getString(c.getColumnIndex(PodcastColumns.TITLE));
				words = RichScriptCommand.preareForDownload(WordFetchService.this, richScript);
				isDone = PodcastColumns.STATUS_DOWNLOADED == c.getInt(c.getColumnIndex(PodcastColumns.DICTIONARY_DOWNLOAD_STATUS));
			} else {
				title = "";
				words = ImmutableList.of();
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			BatchDownloadCommand other = (BatchDownloadCommand) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (podcastId != other.podcastId) {
				return false;
			}
			return true;
		}

		private WordFetchService getOuterType() {
			return WordFetchService.this;
		}

		public int getPodcastId() {
			return podcastId;

		}

		public String getTitle() {
			return title;
		}

		public Iterable<String> getWords() {
			return words;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + podcastId;
			return result;
		}

		public boolean isDone() {
			return isDone;
		}

		@Override
		public void run() {
			updateStatus(PodcastColumns.STATUS_DOWNLOADING);
			Ln.d("Starting download dictionary for episode  %s , %d words need be downloaded ", title, Iterables.size(words));
			int count = 0;
			for (String word : words) {
				List<AbstractDictionaryCommand> cmds = AbstractDictionaryCommand.newDictionaryCommands(WordFetchService.this, word);
				for (AbstractDictionaryCommand cmd : cmds) {
					cmd.run();
					count++;
				}
			}
			updateStatus(PodcastColumns.STATUS_DOWNLOADED);
			Ln.d("Dictionary download completed for episode  %s, %d entity for %d words entity downloaded", title, count, Iterables.size(words));

		}

		@Override
		public String toString() {
			return "BatchDownloadCommand [podcastId=" + podcastId + ", words=" + words + ", title=" + title + "]";
		}

		private void updateStatus(int status) {
			ContentValues cv = new ContentValues();
			cv.put(PodcastColumns.DICTIONARY_DOWNLOAD_STATUS, status);
			getContentResolver().update(PodcastColumns.PODCAST_URI, cv, BaseColumns._ID + "=?", new String[] { Integer.toString(podcastId) });
		}
	}

	private final int						MAX_TASK	= 5;
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.MIN_PRIORITY);

		commandQueue = new ArrayBlockingQueue<Runnable>(MAX_TASK);
		executorService = new ThreadPoolExecutor(1, // core size
				1, // max size
				10 * 60, // idle timeout
				TimeUnit.SECONDS, commandQueue, builder.build(), new AbortPolicy()); // queue with a size

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//final List<AbstractDictionaryCommand> fetchCmds = Lists.newArrayList();
		Uri podcastUri = intent.getData();
		Ln.d("intent data: %s", podcastUri);
		Preconditions.checkNotNull(podcastUri);
		long id = ContentUris.parseId(podcastUri);
		BatchDownloadCommand cmd = new BatchDownloadCommand((int) id);
		try {
			if (commandQueue.contains(cmd)) {
				Ln.d("The episode [%s] is already in download queue", cmd.getTitle());
			} else if (cmd.isDone) {
				Ln.d("The episode [%s] is already in download completed", cmd.getTitle());
			} else {
				Ln.d("Put episode [%s]  in download queue", cmd.getTitle());
				if (Iterables.size(cmd.getWords()) > 0) {
					commandQueue.put(cmd);
				}
				executorService.execute(cmd);
			}
		} catch (InterruptedException e) {
		}
		return Service.START_NOT_STICKY;
	}
}
