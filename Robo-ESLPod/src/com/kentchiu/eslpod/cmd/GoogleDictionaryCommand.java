package com.kentchiu.eslpod.cmd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.net.Uri;

import com.kentchiu.eslpod.provider.Dictionary;

public class GoogleDictionaryCommand extends DictionaryCommand {

	public GoogleDictionaryCommand(Context context, Uri wordBankUri) {
		super(context, wordBankUri);
	}

	@Override
	protected String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		String join = readAsOneLine(url);
		String str1 = StringUtils.substringAfter(join, "dict_api.callbacks.id100(");
		return StringUtils.substringBeforeLast(str1, ")");
	}

	@Override
	protected int getDictionaryId() {
		return Dictionary.DICTIONARY_GOOGLE_DICTIONARY;
	}

	@Override
	protected String getQueryUrl(String word) {
		return "http://www.google.com/dictionary/json?callback=dict_api.callbacks.id100&sl=en&tl=zh-TW&restrict=pr%2Cde&client=te&&q=" + word;

	}

}
