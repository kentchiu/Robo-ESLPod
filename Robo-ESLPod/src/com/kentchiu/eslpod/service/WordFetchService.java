package com.kentchiu.eslpod.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class WordFetchService extends IntentService {

	private ExecutorService	es2;

	private static int		count;

	public WordFetchService() {
		super(WordFetchService.class.getName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.MIN_PRIORITY);
		builder.setDaemon(true);
		es2 = Executors.newFixedThreadPool(3, builder.build());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		es2.shutdown();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
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
	}

	private void executeWordCommands(final List<AbstractDictionaryCommand> fetchCmds) {
		final int total = Iterables.size(fetchCmds);
		Log.i(EslPodApplication.TAG, "End of fetch command collecting, there are new command need " + total + " be executed");
		count = 1;
		for (final AbstractDictionaryCommand each : fetchCmds) {
			es2.execute(new Runnable() {
				@Override
				public void run() {
					each.run();
					Log.i(EslPodApplication.TAG, "Execute command " + count++ + " / " + total);
				}
			});
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
