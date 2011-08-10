package com.kentchiu.eslpod.cmd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.net.Uri;

import com.kentchiu.eslpod.provider.Dictionary;

public class GoogleSuggestCommand extends AbstractDictionaryCommand {

	public GoogleSuggestCommand(Context context, Uri wordBankUri) {
		super(context, wordBankUri);
	}

	@Override
	protected String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		String join = readAsOneLine(url, "BIG5");
		String str1 = StringUtils.substringAfter(join, "window.google.ac.hr(");
		return StringUtils.substringBeforeLast(str1, ")");
	}

	@Override
	protected int getDictionaryId() {
		return Dictionary.DICTIONARY_GOOGLE_SUGGESTION;
	}

	@Override
	protected String getQueryUrl(String word) {
		return "http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=" + word;

	}

}
