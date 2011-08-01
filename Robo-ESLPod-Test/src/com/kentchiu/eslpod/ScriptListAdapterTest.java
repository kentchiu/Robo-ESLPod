package com.kentchiu.eslpod;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hamcrest.Matchers;

import android.test.AndroidTestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ScriptListAdapterTest extends AndroidTestCase {

	public void isBaseWord() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		assertTrue(adapter.isBaseWord("I"));
		assertTrue(adapter.isBaseWord("You"));
		assertTrue(adapter.isBaseWord("you"));
		assertTrue(adapter.isBaseWord("on"));
		assertTrue(adapter.isBaseWord("On"));
		assertFalse(adapter.isBaseWord("foo"));
		assertFalse(adapter.isBaseWord("Foo"));
	}

	public void testExtractWords_none_words() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.setRichScript("foo bar");
		assertThat(adapter.extractWord(richScript), Matchers.<String> emptyIterable());
	}

	public void testExtractWords_one_word() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.setRichScript("foo <b>bar</b> baz");
		Iterable<String> words = adapter.extractWord(richScript);
		assertThat(words, hasItem("bar"));
	}

	public void testExtractWords_two_words() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.setRichScript("<b>foo</b> bar <b>baz</b>");
		Iterable<String> words = adapter.extractWord(richScript);
		assertThat(words, hasItems("foo", "baz"));
		assertThat(words, not(hasItem("bar")));
	}

	public void testRichText() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.richText("this is a foo bar string", ImmutableList.of("foo", "string"));
	}

	public void testSplitPhaseVerbToWords() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		Iterable<String> foo = adapter.splitPhaseVerbToWords("foo");
		assertThat(Iterables.size(foo), is(1));
		Iterable<String> foobar = adapter.splitPhaseVerbToWords("foo bar");
		assertThat(Iterables.size(foobar), is(2));
		Iterable<String> foobarbaz = adapter.splitPhaseVerbToWords("foo bar baz");
		assertThat(Iterables.size(foobarbaz), is(3));
		assertThat(foobarbaz, hasItems("foo", "bar", "baz"));
	}

}
