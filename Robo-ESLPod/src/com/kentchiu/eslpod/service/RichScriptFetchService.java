package com.kentchiu.eslpod.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import roboguice.util.Ln;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.BaseColumns;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.provider.WordFetch.WordFetchColumns;

public class RichScriptFetchService extends Service {

	private ExecutorService	executorService;

	private void fetchScript(Cursor c, String link) {
		RichScriptCommand richScriptCmd = new RichScriptCommand(this, ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, c.getInt(c.getColumnIndex(BaseColumns._ID))), link);
		executorService.execute(richScriptCmd);
	}

	
	private List<AbstractDictionaryCommand> prepareCommands(int podcastId,Iterable<String> words) {
		List<AbstractDictionaryCommand> results = Lists.newArrayList();
		Iterable<String> headword = RichScriptCommand.headword(this, words);
		for (String phase : headword) {
			Iterable<String> ws = Splitter.onPattern("(\n| )").trimResults().split(phase);
			for (String w : ws) {
				String word = StringUtils.trim(w.replaceAll(",", ""));
				for (Integer dictId : AbstractDictionaryCommand.allDictionaryId()) {
					ContentValues cv = new ContentValues();
					cv.put(WordFetchColumns.STATUS, WordFetchColumns.STATUS_DOWNLOADABLE);
					String where = String.format("%s=? and %s=? and %s=?", WordFetchColumns.WORD, WordFetchColumns.DICTIONARY_ID, WordFetchColumns.PODCAST_ID);
					Ln.v("Mark word [%s] at dictionary %d as downloaded", word, dictId);
					getContentResolver().update(WordFetchColumns.WORD_FETCH_URI, cv, where, new String[] { word, Integer.toString(dictId), Long.toString(podcastId) });
				}
			}
		}
		return results;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.MIN_PRIORITY);
		builder.setDaemon(true);
		executorService = Executors.newSingleThreadExecutor(builder.build());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		executorService.shutdown();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Lists.newArrayList();
		Cursor c = getContentResolver().query(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		while (c.moveToNext()) {
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			String link = c.getString(c.getColumnIndex(PodcastColumns.LINK));
			if (StringUtils.isBlank(richScript)) {
				if (StringUtils.isNotBlank(link)) {
					fetchScript(c, link);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
