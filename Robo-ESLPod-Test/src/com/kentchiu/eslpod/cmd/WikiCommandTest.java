package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.provider.Dictionary;

public class WikiCommandTest extends AndroidTestCase {
	private WiktionaryCommand	command;

	public void testGetContent() throws Exception {
		assertThat(command.getContent(), containsString("test"));
	}

	public void testGetDictionId() throws Exception {
		assertThat(command.getDictionaryId(), is(Dictionary.DICTIONARY_WIKITIONARY));
	}

	public void testQueryUri() throws Exception {
		assertThat(command.getQueryUrl(), is("http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&rvexpandtemplates=true&alllinks=true&titles=" + "test"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		command = new WiktionaryCommand(null, "test");
	}

}
