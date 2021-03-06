package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import roboguice.util.Ln;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Handler;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class RichScriptCommand extends AbstractCommand {
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

	public static Iterable<String> excludeBaseWord(Context context, Iterable<String> filter) {
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

	public static Iterable<String> extractWord(String richScript) {
		if (StringUtils.isBlank(richScript)) {
			return ImmutableList.of();
		}
		String richScript2 = richScript.replaceAll("\n", "");
		String[] words = StringUtils.substringsBetween(richScript2, "<b>", "</b>");
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

	protected static boolean isBaseWord(Set<String> baseWords, String word) {
		for (String each : baseWords) {
			if (StringUtils.equalsIgnoreCase(each, word)) {
				return true;
			}
		}
		return false;
	}

	public static Iterable<String> preareForDownload(Context context, String richScript) {
		Iterable<String> wordsOrPhaseVerbs = extractWord(richScript);
		Iterable<String> headwords = excludeBaseWord(context, wordsOrPhaseVerbs);
		List<String> results = Lists.newArrayList();
		for (String headword : headwords) {
			Ln.v("headword: [%s]", headword);
			results.add(headword);
		}

		return results;
	}

	protected static Iterable<String> splitPhaseVerbToWords(String words) {
		Iterable<String> results = Splitter.on(' ').trimResults().trimResults(CharMatcher.is(',')).split(words);
		return results;
	}

	public RichScriptCommand(Context context, Intent intent) {
		super(context, intent);
	}

	public RichScriptCommand(Context context, Intent intent, Handler handler) {
		super(context, intent, handler);
	}

	@Override
	public boolean execute() {
		Cursor c = context.getContentResolver().query(intent.getData(), null, null, null, null);
		String richScript = "";
		String scriptUrl;
		String title;
		if (c.moveToFirst()) {
			richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			scriptUrl = c.getString(c.getColumnIndex(PodcastColumns.LINK));
			title = c.getString(c.getColumnIndex(PodcastColumns.TITLE));
		} else {
			return false;
		}
		if (StringUtils.isBlank(richScript)) {
			if (StringUtils.isNotBlank(scriptUrl)) {
				String script = fetchScript(scriptUrl, title);
				if (StringUtils.isNotBlank(script)) {
					updateDatabase(script);
				}
			}
		} else {
			Ln.v("rich script for url [%s] already exists", scriptUrl);
		}
		return true;
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

	protected String fetchScript(String scriptUrl, String title) {
		List<String> lines = ImmutableList.of();
		try {
			Ln.d("Start fetching script of %s form : ", scriptUrl);
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
			Ln.d("End fetching script of %s form : %s", title, scriptUrl);
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

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	protected void updateDatabase(String richScript) {
		ContentValues values = new ContentValues();
		values.put(PodcastColumns.RICH_SCRIPT, richScript);
		Ln.i("update rich script");
		getContext().getContentResolver().update(intent.getData(), values, null, null);
	}

}