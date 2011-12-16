package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import roboguice.util.Ln;
import android.content.Context;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.provider.Dictionary;

public class DictionaryDictionaryCommand extends AbstractDictionaryCommand {

	protected DictionaryDictionaryCommand(Context context, String word) {
		super(context, word);
	}

	private String applyTemplate(String input) {
		try {
			InputStream is = context.getAssets().open("dictionary/template.htm");
			List<String> lines = IOUtils.readLines(is, "utf8");
			String template = Joiner.on(" ").join(lines);
			return template.replace("{1}", getQueryUrl()).replace("{0}", input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

	private String extractDefinition(String input) {
		String pattern = "<div class=\"pronunciation\">.*<div class=\"etymology\">.*?</div>";
		Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
		Matcher m = p.matcher(input);
		if (m.find()) {
			return m.group();
		} else {
			Ln.e("Not match for word : %s", getQueryUrl());
			return input;
		}
	}

	@Override
	protected String getContent() {
		return readAsOneLine(getQueryUrl(), 0);
	}

	@Override
	public int getDictionaryId() {
		return Dictionary.DICTIONARY_DICTIONARY_DICTIONARY;
	}

	@Override
	protected String getQueryUrl() {
		return " http://m.dictionary.com/?submit-result-SEARCHD=Search&q=" + word;
	}

	@Override
	protected String render(String input) {
		String extracted = extractDefinition(input).replaceAll("<a.*>(.*)</a>", "$1");
		return applyTemplate(extracted);
	}

}
