package com.kentchiu.eslpod.service;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import roboguice.service.RoboService;
import roboguice.util.Ln;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.provider.WordFetch.WordFetchColumns;

public class WordFetchService extends RoboService {

	private final int						MAX_TASK	= 500;
	private final Handler					handler		= new Handler();
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;

	private void executeWordCommands(final long podcastId, final List<AbstractDictionaryCommand> fetchCmds) {
		final int addtion = Iterables.size(fetchCmds);
		final int total = commandQueue.size() + addtion;
		Ln.i("Queue size %d(%d)", total, addtion);
		try {
			for (final AbstractDictionaryCommand each : fetchCmds) {
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						each.run();
						markAsDownloaded(podcastId, each);
						Ln.v("queue size : %d", commandQueue.size());
					}
				});
			}
		} catch (RejectedExecutionException e) {
		}
	}

	private void markAsDownloaded(long podcastId, AbstractDictionaryCommand cmd) {
		//updateStatus(podcastId, cmd.getDictionaryId(), cmd.getWord(), WordFetchColumns.STATUS_DOWNLOADED);
	}

	private void markAsDownloading(final long podcastId, final List<AbstractDictionaryCommand> cmds) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				for (AbstractDictionaryCommand each : cmds) {
					updateStatus(podcastId, each.getDictionaryId(), each.getWord(), WordFetchColumns.STATUS_DOWNLOADING);
				}
			}
		}).start();
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
		executorService = new ThreadPoolExecutor(3, // core size
				3, // max size
				10 * 60, // idle timeout
				TimeUnit.SECONDS, commandQueue, builder.build(), new AbortPolicy()); // queue with a size

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final List<AbstractDictionaryCommand> fetchCmds = Lists.newArrayList();
		Uri podcastUri = intent.getData();
		Cursor c = getContentResolver().query(podcastUri, null, null, null, null);

		if (c.moveToFirst()) {
			long podcastId = c.getLong(c.getColumnIndex(BaseColumns._ID));
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			Iterable<String> words = RichScriptCommand.extractWord(richScript);
			List<AbstractDictionaryCommand> cmds = prepareCommands(words);
			markAsDownloading(podcastId, cmds);
			fetchCmds.addAll(cmds);
			Ln.v("Add %d new command, total command is %d", Iterables.size(cmds), Iterables.size(fetchCmds));
			executeWordCommands(podcastId, fetchCmds);
		}

		return START_REDELIVER_INTENT;
	}

	private List<AbstractDictionaryCommand> prepareCommands(Iterable<String> words) {
		List<AbstractDictionaryCommand> results = Lists.newArrayList();
		Iterable<String> headword = RichScriptCommand.headword(this, words);
		for (String phase : headword) {
			Iterable<String> ws = Splitter.onPattern("(\n| )").trimResults().split(phase);
			for (String w : ws) {
				String input = StringUtils.trim(w.replaceAll(",", ""));
				List<AbstractDictionaryCommand> cmds = AbstractDictionaryCommand.newDictionaryCommands(this, input);
				for (AbstractDictionaryCommand each : cmds) {
					results.add(each);
				}
			}
		}
		return results;
	}

	private void updateStatus(long podcastId, int dictId, String word, int status) {
		ContentValues cv = new ContentValues();
		cv.put(WordFetchColumns.STATUS, status);
		String where = String.format("%s=? and %s=? and %s=?", WordFetchColumns.WORD, WordFetchColumns.DICTIONARY_ID, WordFetchColumns.PODCAST_ID);
		Ln.v("Mark word [%s] at dictionary %d as downloaded", word, dictId);
		getContentResolver().update(WordFetchColumns.WORD_FETCH_URI, cv, where, new String[] { word, Integer.toString(dictId), Long.toString(podcastId) });
	}

	//	void showMessage(final String text, final int lengthLong) {
	//		handler.post(new Runnable() {
	//
	//			@Override
	//			public void run() {
	//				Toast.makeText(WordFetchService.this, text, lengthLong).show();
	//			}
	//		});
	//	}

}
