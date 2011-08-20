package com.kentchiu.eslpod.cmd;

import java.io.IOException;

import android.content.Context;

import com.kentchiu.eslpod.provider.Dictionary;

public class DictionaryDictionaryCommand extends AbstractDictionaryCommand {

	protected DictionaryDictionaryCommand(Context context, String query) {
		super(context, query);
	}

	@Override
	protected String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		return readAsOneLine(url, 0);
	}

	@Override
	protected int getDictionaryId() {
		return Dictionary.DICTIONARY_DICTIONARY_DICTIONARY;
	}

	@Override
	protected String getQueryUrl(String word) {
		return " http://m.dictionary.com/?submit-result-SEARCHD=Search&q=" + word;
	}

}
