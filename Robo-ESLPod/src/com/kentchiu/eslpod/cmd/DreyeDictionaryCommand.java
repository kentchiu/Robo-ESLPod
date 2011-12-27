package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import roboguice.util.Ln;
import android.content.Context;
import android.content.Intent;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.provider.Dictionary;

public class DreyeDictionaryCommand extends AbstractDictionaryCommand {

	public DreyeDictionaryCommand(Context context, Intent intent) {
		super(context, intent);
	}

	private String applyTemplate(String input) {
		try {
			InputStream is = context.getAssets().open("dreye/template.htm");
			List<String> lines = IOUtils.readLines(is, "utf8");
			String template = Joiner.on(" ").join(lines);
			String replace = template.replace("{1}", getQueryUrl()).replace("{0}", input);
			return replace;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

	private String extractDefinition(String input) {
		String pattern = "<div id=\"KK\" class=\"yinbiao\">.*?</table>";
		Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
		Matcher m = p.matcher(input);
		if (m.find()) {
			String group = m.group();
			return group;
		} else {
			Ln.e("Not match for word : %s", getQueryUrl());
			return input;
		}
	}

	@Override
	protected String getContent() {
		String url = getQueryUrl();
		return readAsOneLine(url, 0);
	}

	@Override
	public int getDictionaryId() {
		return Dictionary.DICTIONARY_DREYE_DICTIONARY;
	}

	@Override
	protected String getQueryUrl() {
		return "http://www.dreye.com/mws/dict.php?ua=dc_cont&hidden_codepage=01&w=" + intent.getExtras().getString(WORD);
	}

	@Override
	protected String render(String input) {
		String extracted = extractDefinition(input).replaceAll("images/DIC/PN", "http://www.dreye.com/mws/images/DIC/PN");
		String applyTemplate = applyTemplate(extracted);
		return applyTemplate;
	}

}
