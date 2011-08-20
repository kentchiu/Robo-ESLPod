package com.kentchiu.eslpod.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import android.R;
import android.app.IntentService;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;
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

public class FetchService extends IntentService {

	private ExecutorService	es;
	private ExecutorService	es2;

	public FetchService() {
		super(FetchService.class.getName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.MIN_PRIORITY);
		builder.setDaemon(true);
		es = Executors.newSingleThreadExecutor(builder.build());
		es2 = Executors.newFixedThreadPool(3, builder.build());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		es.shutdown();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final List<AbstractDictionaryCommand> fetchCmds = Lists.newArrayList();
		Cursor c = getContentResolver().query(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		while (c.moveToNext()) {
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			String link = c.getString(c.getColumnIndex(PodcastColumns.LINK));
			if (StringUtils.isBlank(richScript)) {
				if (StringUtils.isNotBlank(link)) {
					fetchScript(c, link);
				}
			} else {
				Iterable<String> words = RichScriptCommand.extractWord(richScript);
				List<AbstractDictionaryCommand> cmds = fetchWods(words);
				fetchCmds.addAll(cmds);
				Log.v(EslPodApplication.TAG, "Add " + Iterables.size(cmds) + " new command, total command is" + Iterables.size(fetchCmds));
			}
			if (Iterables.size(fetchCmds) > 1000) {
				break;
			}
		}
		executeWordCommands(fetchCmds);
	}

	private void executeWordCommands(final List<AbstractDictionaryCommand> fetchCmds) {
		final int total = Iterables.size(fetchCmds);
		Log.i(EslPodApplication.TAG, "End of fetch command collecting, there are new command need " + total + " be executed");
		count  = 1;
		for (final AbstractDictionaryCommand each : fetchCmds) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					each.run();
					Log.i(EslPodApplication.TAG, "Execute command " + count++ + " / " + total  );
				}
			});
		}
	}
	private static int count;

	private void fetchScript(Cursor c, String link) {
		try {
			RichScriptCommand richScriptCmd = new RichScriptCommand(this, ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, c.getInt(c.getColumnIndex(BaseColumns._ID))), new URL(link));
			es.execute(richScriptCmd);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private List<AbstractDictionaryCommand> fetchWods(Iterable<String> words) {
		List<AbstractDictionaryCommand> results = Lists.newArrayList();
		Iterable<String> headword = RichScriptCommand.headword(this, words);
		for (String phase : headword) {
			Iterable<String> ws = Splitter.onPattern("(\n| )").trimResults().split(phase);
			for (String w : ws) {
				String input = StringUtils.trim(w.replaceAll(",", ""));
				List<AbstractDictionaryCommand> cmds = AbstractDictionaryCommand.newDictionaryCommands(this, input);
				for (AbstractDictionaryCommand each : cmds) {
					results.add(each);
					//es2.execute(each);
				}
			}
		}
		return results;
	}

}
