package com.kentchiu.eslpod.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class RichScriptHandler implements Runnable {

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

	private URL						link;
	private Iterable<String> script;

	public RichScriptHandler(URL link) {
		this.link = link;
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
	public void run() {
		List<String> lines;
		try {
			InputStream is = link.openStream();
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
		script = filter;
	}

	public Iterable<String> getScript() {
		return script;
	}


}
