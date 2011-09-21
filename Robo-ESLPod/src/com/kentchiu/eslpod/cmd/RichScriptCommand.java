package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.EslPodApplication;

public class RichScriptCommand implements Runnable {
	static final class Trim implements Function<String, String> {
		@Override
		public String apply(String input) {
			String input2 = StringUtils.trim(input);
			char last = input2.charAt(input2.length() - 1);
			char first = input2.charAt(0);
			String result;
			if (!StringUtils.isAlpha(String.valueOf(last))) {
				result = input2.substring(0, input2.length() - 1);
			} else {
				result = input2;
			}
			if (!StringUtils.isAlpha(String.valueOf(first))) {
				result = result.substring(1, result.length());
			}
			return result;
		}
	}

	private static class ContainPredicate implements Predicate<String> {

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

	public static Iterable<String> extractWord(String richScript) {
		if (StringUtils.isBlank(richScript)) {
			return ImmutableList.of();
		}
		String[] words = StringUtils.substringsBetween(richScript, "<b>", "</b>");
		if (ArrayUtils.isEmpty(words)) {
			return ImmutableList.of();
		} else {
			Iterable<String> results = Iterables.transform(Arrays.asList(words), new Trim());
			return ImmutableList.copyOf(results);
		}
	}

	public static HashSet<String> getBaseWords(Context context) {
		Resources res = context.getResources();
		String[] baseWords = res.getStringArray(R.array.base_words);
		// Using set to remove duplication
		final HashSet<String> baseWordSet = Sets.newHashSet();
		for (String each : baseWords) {
			baseWordSet.add(each.toLowerCase());
		}
		return baseWordSet;
	}

	public static Iterable<String> headword(Context context, Iterable<String> filter) {
		Set<String> result = Sets.newLinkedHashSet();
		for (String each : filter) {
			for (String word : splitPhaseVerbToWords(each)) {
				String w = word.replace('?', ' ').replace('.', ' ').replace(',', ' ').trim();
				if (!isBaseWord(getBaseWords(context), w) && !StringUtils.containsAny(w, "’")) {
					result.add(w);
				}
			}
		}
		return result;
	}

	protected static boolean isBaseWord(Set<String> baseWords, String word) {
		for (String each : baseWords) {
			if (StringUtils.equalsIgnoreCase(each, word)) {
				return true;
			}
		}
		return false;
	}

	protected static Iterable<String> splitPhaseVerbToWords(String words) {
		Iterable<String> results = Splitter.on(' ').trimResults().trimResults(CharMatcher.is(',')).split(words);
		return results;
	}

	private Context	context;
	private Uri		podcastUri;
	private String	scriptUrl;

	public RichScriptCommand(Context context, Uri podcastUri, String scriptUrl) {
		super();
		setContext(context);
		this.podcastUri = podcastUri;
		this.scriptUrl = scriptUrl;
	}

	public synchronized List<String> extractScript(List<String> lines) {
		int index1 = Iterables.indexOf(lines, new ContainPredicate("Audio Index:"));
		int index2 = Iterables.indexOf(lines, new ContainPredicate("Script by Dr. Lucy Tse"));
		// start from line "Audio Index:" to line "Script by Dr. Lucy Tse"
		List<String> subLines;
		try {
			subLines = lines.subList(index1, index2);
			String wholeLine = Joiner.on("\n").join(subLines);
			String lines3 = StringUtils.substringBetween(wholeLine, "<span class=\"pod_body\">", "</span>");
			Iterable<String> result = Splitter.on("\n").trimResults().split(lines3.replaceAll("<br>", ""));
			return ImmutableList.copyOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ImmutableList.of();
		}
	}

	public Context getContext() {
		return context;
	}

	@Override
	public void run() {
		Cursor c = context.getContentResolver().query(PodcastColumns.PODCAST_URI, null, PodcastColumns.LINK + "=?", new String[] { scriptUrl.toString() }, null);
		String richScript = "";
		String link = "";
		String title = "";
		if (c.moveToFirst()) {
			richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			link = c.getString(c.getColumnIndex(PodcastColumns.LINK));
			title = c.getString(c.getColumnIndex(PodcastColumns.TITLE));
		}
		if (StringUtils.isBlank(richScript)) {
			if (StringUtils.isNotBlank(link)) {
				String script = fetchScript(title);
				if (StringUtils.isNotBlank(script)) {
					updateDatabase(script);
				}
			}
		} else {
			Log.v(EslPodApplication.TAG, "rich script for url [" + scriptUrl + "] already exists");
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	protected String fetchScript(String title) {
		List<String> lines = ImmutableList.of();

		try {
			Log.d(EslPodApplication.TAG, "Start fetching script of " + title + " form : " + scriptUrl);
			// ===== The ascii 146 issues ====
			// Ascii 146 (or 0x92) is render as ’ (Right single quotation mark) ref : http://www.ascii-code.com/
			// But ascii 146 is not exists in any encoding which I can found
			// iso-8859-1 and utf-8 not includes ascii 146, but it can be render by any browser and render as ’ (Right single quotation mark)
			// I have no idea which encoding can be used to render 146 to right single quotation mark
			// So, I hacking it, if I got asc146, I will force it to ’
			InputStream is = new URL(scriptUrl).openStream();
			int c;
			StringBuilder sb = new StringBuilder();
			while ((c = is.read()) != -1) {
				if (c == 146) {
					sb.append('’');
				} else {
					sb.append((char) c);
				}
			}
			Log.d(EslPodApplication.TAG, "End fetching script of " + title + " form : " + scriptUrl);
			lines = ImmutableList.copyOf(Splitter.on("\n").split(sb.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!lines.isEmpty()) {
			Iterable<String> filter = Iterables.filter(extractScript(lines), new Predicate<String>() {
				@Override
				public boolean apply(String input) {
					return StringUtils.isNotEmpty(input);
				}
			});

			return Joiner.on("\n").join(filter);
		} else {
			return "";
		}

	}

	protected void updateDatabase(String richScript) {
		ContentValues values = new ContentValues();
		values.put(PodcastColumns.RICH_SCRIPT, richScript);
		Log.i(EslPodApplication.TAG, "update rich script");
		getContext().getContentResolver().update(podcastUri, values, null, null);
	}

}