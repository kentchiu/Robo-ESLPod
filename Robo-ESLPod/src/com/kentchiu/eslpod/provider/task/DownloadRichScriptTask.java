package com.kentchiu.eslpod.provider.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class DownloadRichScriptTask extends AsyncTask<String, Void, Iterable<String>> {
	private class ContainPredicate implements Predicate<String> {

		private String	token;

		public ContainPredicate(String token) {
			super();
			this.token = token;
		}

		@Override
		public boolean apply(String input) {
			return StringUtils.contains(input, token);
		}
	}

	private final Context	context;
	private final long		podcastId;

	private final Uri		uri2;

	public DownloadRichScriptTask(Context context, long podcastId, Uri uri2) {
		this.context = context;
		this.podcastId = podcastId;
		this.uri2 = uri2;
	}

	public synchronized List<String> extractScript(List<String> lines) {
		int index1 = Iterables.indexOf(lines, new ContainPredicate("Audio Index:"));
		int index2 = Iterables.indexOf(lines, new ContainPredicate("Script by Dr. Lucy Tse"));
		// start from line "Audio Index:" to line "Script by Dr. Lucy Tse"
		List<String> subLines = lines.subList(index1, index2);
		String wholeLine = Joiner.on("\n").join(subLines);
		String lines3 = StringUtils.substringBetween(wholeLine, "<span class=\"pod_body\">", "</span>");
		Iterable<String> result = Splitter.on("\n").trimResults().split(lines3.replaceAll("<br>", ""));
		return ImmutableList.copyOf(result);
	}

	@Override
	protected Iterable<String> doInBackground(String... params) {
		try {
			URL url = new URL(params[0]);
			List<String> lines;
			try {
				InputStream is = url.openStream();
				lines = IOUtils.readLines(is, "iso-8859-1");
			} catch (MalformedURLException e) {
				lines = ImmutableList.of();
				e.printStackTrace();
			} catch (IOException e) {
				lines = ImmutableList.of();
				e.printStackTrace();
			}
			Iterable<String> filter = Iterables.filter(extractScript(lines), new Predicate<String>() {
				@Override
				public boolean apply(String input) {
					return StringUtils.isNotEmpty(input);
				}
			});
			return filter;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ImmutableList.of();
	}

	@Override
	protected void onPostExecute(Iterable<String> result) {
		ContentValues values = new ContentValues();
		String richScript = Joiner.on("\n").join(result);
		values.put(PodcastColumns.RICH_SCRIPT, richScript);
		Log.i(EslPodApplication.LOG_TAG, "update rich script");
		context.getContentResolver().update(uri2, values, "_ID=?", new String[] { Long.toString(podcastId) });
	}
}