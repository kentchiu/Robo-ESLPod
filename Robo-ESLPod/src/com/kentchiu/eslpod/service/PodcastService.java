package com.kentchiu.eslpod.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastService extends IntentService {

	public static final String	COMMAND						= "command";
	public static final int		COMMAND_FETCH_NEW_PODCAST	= 1;
	public static final int		COMMAND_RICH_SCRIPT			= 2;
	public static final int		COMMAND_DOWNLOAD_MEDIA		= 3;

	public PodcastService() {
		super(PodcastService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int cmd = intent.getIntExtra(COMMAND, -1);
		final Uri podcastUri = intent.getData();
		switch (cmd) {
		case COMMAND_FETCH_NEW_PODCAST:
			InputStream is;
			try {
				is = new URL(PodcastCommand.RSS_URI).openStream();
				PodcastCommand command = new PodcastCommand(this, is);
				new Thread(command).start();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case COMMAND_RICH_SCRIPT:
			try {
				Cursor c = getContentResolver().query(podcastUri, null, COMMAND, null, null);
				if (c.moveToFirst()) {
					String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
					String link = c.getString(c.getColumnIndex(PodcastColumns.LINK));
					if (StringUtils.isBlank(richScript) && StringUtils.isNotBlank(link)) {
						URL scriptUrl = new URL(link);
						//new Thread(new RichScriptCommand(this, podcastUri, scriptUrl)).start();
						new AsyncTask<URL, Void, Iterable<String>>() {

							@Override
							protected Iterable<String> doInBackground(URL... params) {
								RichScriptCommand cmd = new RichScriptCommand(PodcastService.this, podcastUri, params[0]);
								// doInBackground is already running in work thread, no need to execute command in another thread
								cmd.run();
								String richScriptCache = cmd.getRichScriptCache();
								Iterable<String> result = RichScriptCommand.headword2(PodcastService.this, RichScriptCommand.extractWord(richScriptCache));
								Log.v(EslPodApplication.TAG, "headword2 :" + Iterables.toString(result));
								Intent intent = new Intent(PodcastService.this, DictionaryService.class);
								intent.putExtra(DictionaryService.COMMAND, DictionaryService.COMMAND_DOWNLOAD_WORD);
								for (String each : result) {
									intent.putExtra(SearchManager.QUERY, each);
									startService(intent);
								}
								return result;
							};

							@Override
							protected void onPostExecute(Iterable<String> result) {
							}

							@Override
							protected void onPreExecute() {

							};
						}.execute(scriptUrl);

					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		case COMMAND_DOWNLOAD_MEDIA:
			new Thread(new MediaCommand(this, podcastUri)).start();
			break;
		default:
			break;
		}
	}

}
