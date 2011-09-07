package com.kentchiu.eslpod.service;

import java.util.List;
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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class WordFetchService extends Service {

	private final int						MAX_TASK	= 5;
	private final Handler					handler		= new Handler();
	private ExecutorService					executorService;
	private ArrayBlockingQueue<Runnable>	commandQueue;

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
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			Iterable<String> words = RichScriptCommand.extractWord(richScript);
			List<AbstractDictionaryCommand> cmds = prepareCommands(words);
			fetchCmds.addAll(cmds);
			Log.v(EslPodApplication.TAG, "Add " + Iterables.size(cmds) + " new command, total command is" + Iterables.size(fetchCmds));
		}
		executeWordCommands(fetchCmds);
		return super.onStartCommand(intent, flags, startId);
	}

	void showMessage(final String text, final int lengthLong) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(WordFetchService.this, text, lengthLong).show();
			}
		});
	}

	private void executeWordCommands(final List<AbstractDictionaryCommand> fetchCmds) {
		final int addtion = Iterables.size(fetchCmds);
		final int total = commandQueue.size() + addtion;
		Log.i(EslPodApplication.TAG, "Queue size " + total + "(+" + addtion + ")");
		String msg = "There are " + total + "(+" + addtion + ")" + " entries be downloaded";
		if (total != 0) {
			showMessage(msg, Toast.LENGTH_SHORT);
		}
		try {
			for (final AbstractDictionaryCommand each : fetchCmds) {
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						each.run();
						Log.v(EslPodApplication.TAG, "queue size : " + commandQueue.size());
						if (commandQueue.isEmpty()) {
							showMessage("Dictionary entry download completed.", Toast.LENGTH_LONG);
						}
					}
				});
			}
		} catch (RejectedExecutionException e) {
			final String text = "Only " + MAX_TASK + " tasks allow, tyr it later";
			showMessage(text, Toast.LENGTH_LONG);
		}
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

}
