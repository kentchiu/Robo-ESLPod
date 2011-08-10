package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class RichScriptCommand implements Runnable {
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
			return ImmutableList.copyOf(words);
		}
	}

	public static Iterable<String> headword(Context context, Iterable<String> filter) {
		Set<String> menuStr = Sets.newLinkedHashSet();
		for (String each : filter) {
			Iterable<String> words2 = splitPhaseVerbToWords(each);
			for (String word : words2) {
				if (!isBaseWord(context, word)) {
					menuStr.add(word);
				}
			}
		}
		return menuStr;
	}

	protected static boolean isBaseWord(Context context, String word) {
		//ImmutableList<String> baseWords = ImmutableList.of("I", "you", "me", "of", "on", "of", "off", "it", "a", "an");
		Resources res = context.getResources();
		String[] baseWords = res.getStringArray(R.array.base_words);
		// Using set to remove duplication
		final HashSet<String> baseWordSet = Sets.newHashSet();
		for (String each : baseWords) {
			baseWordSet.add(each.toLowerCase());
		}
		for (String each : baseWordSet) {
			if (StringUtils.equalsIgnoreCase(each, word)) {
				return true;
			}
		}
		return false;
	}

	protected static Iterable<String> splitPhaseVerbToWords(String words) {
		Iterable<String> results = Splitter.on(' ').trimResults().split(words);
		return results;
	}

	private Context	context;

	private Uri		podcastUri;

	private URL		scriptUrl;

	private String	richScriptCache;

	public RichScriptCommand(Context context, Uri podcastUri, URL scriptUrl) {
		super();
		setContext(context);
		this.podcastUri = podcastUri;
		this.scriptUrl = scriptUrl;
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

	public Context getContext() {
		return context;
	}

	/**
	 * Get RichScript which fetch from web.
	 * This method only available after {@link #run()} method completed.
	 * @return Rich Script in Html format.
	 */
	public String getRichScriptCache() {
		return richScriptCache;
	}

	@Override
	public void run() {
		richScriptCache = fetchScript();
		if (StringUtils.isNotBlank(richScriptCache)) {
			updateDatabase(richScriptCache);
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	protected String fetchScript() {
		List<String> lines = ImmutableList.of();
		Log.d(EslPodApplication.TAG, "Start fetching script form : " + scriptUrl);
		try {
			InputStream is = scriptUrl.openStream();
			lines = IOUtils.readLines(is, "iso-8859-1");
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