package com.kentchiu.eslpod.cmd;

import java.io.IOException;

import android.os.Handler;

import com.kentchiu.eslpod.provider.Dictionary;

public class DictionaryDictionaryCommand extends AbstractDictionaryCommand {

	public DictionaryDictionaryCommand(Handler handler, String query) {
		super(handler, query);
	}

	@Override
	protected String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		String join = readAsOneLine(url);
		return join;
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
