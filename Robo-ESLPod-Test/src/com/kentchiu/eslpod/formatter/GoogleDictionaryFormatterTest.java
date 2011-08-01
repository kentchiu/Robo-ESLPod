package com.kentchiu.eslpod.formatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

public class GoogleDictionaryFormatterTest extends TestCase {

	public void testFormat() throws Exception {
		InputStream inputSteam = this.getClass().getResourceAsStream("help.json");
		String json = IOUtils.toString(inputSteam);
		assertThat(GoogleSuggestFormatter.formatText(json), is("/hɛlp/<br/>verb<br/>幫助；協助；援助</br>"));
	}

}
