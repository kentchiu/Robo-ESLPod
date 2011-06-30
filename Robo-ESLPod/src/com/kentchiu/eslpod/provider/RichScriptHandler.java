package com.kentchiu.eslpod.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;

import android.content.ContentValues;
import android.net.Uri;

import com.finchframework.finch.rest.ResponseHandler;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class RichScriptHandler implements ResponseHandler {

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

	private PodcastContentProvider	provider;
	private Uri						uri;

	public RichScriptHandler(PodcastContentProvider provider, Uri uri) {
		this.provider = provider;
		this.uri = uri;
	}

	public List<String> extractScript(List<String> lines) {
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
	public void handleResponse(HttpResponse response, Uri uri) throws IOException {
		InputStream is = response.getEntity().getContent();
		List<String> lines = IOUtils.readLines(is, "iso-8859-1");
		Iterable<String> filter = Iterables.filter(extractScript(lines), new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return StringUtils.isNotEmpty(input);
			}
		});
		ContentValues values = new ContentValues();
		values.put(PodcastColumns.RICH_SCRIPT, Joiner.on("\n").join(filter));
		provider.update(this.uri, values, null, null);
	}

}
