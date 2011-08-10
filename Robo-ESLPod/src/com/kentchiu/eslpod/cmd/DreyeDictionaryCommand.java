package com.kentchiu.eslpod.cmd;

import java.io.IOException;

import android.content.Context;
import android.net.Uri;

import com.kentchiu.eslpod.provider.Dictionary;

public class DreyeDictionaryCommand extends AbstractDictionaryCommand {

	public DreyeDictionaryCommand(Context context, Uri wordBankUri) {
		super(context, wordBankUri);
	}

	@Override
	public String toHtml(String input) {
		System.out.println(input);
		input = input.replaceAll("images/", "http://www.dreye.com/ews/images/");
		input = input.replaceAll("\\<form.*?form\\>", "");
		return input;
	}

	@Override
	protected String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		String join = readAsOneLine(url);
		return join;
	}

	@Override
	protected int getDictionaryId() {
		return Dictionary.DICTIONARY_DREYE_DICTIONARY;
	}

	@Override
	protected String getQueryUrl(String word) {
		return "http://www.dreye.com/mws/dict.php?ua=dc_cont&hidden_codepage=01&w=" + word;
	}

}
