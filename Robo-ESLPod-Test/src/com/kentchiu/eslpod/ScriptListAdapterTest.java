package com.kentchiu.eslpod;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;

import android.test.AndroidTestCase;

import com.google.common.collect.ImmutableList;

public class ScriptListAdapterTest extends AndroidTestCase {

	public void testExtractWords_none_words() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.setRichScript("foo bar");
		assertThat(adapter.extractWord(), Matchers.<String> emptyIterable());
	}

	public void testExtractWords_one_word() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.setRichScript("foo <b>bar</b> baz");
		Iterable<String> words = adapter.extractWord();
		assertThat(words, hasItem("bar"));
	}

	public void testExtractWords_two_words() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.setRichScript("<b>foo</b> bar <b>baz</b>");
		Iterable<String> words = adapter.extractWord();
		assertThat(words, hasItems("foo", "baz"));
		assertThat(words, not(hasItem("bar")));
	}

	public void testRichText() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		CharSequence text = adapter.richText("this is a foo bar string", ImmutableList.of("foo", "string"));
		System.out.println(text);

	}

}
