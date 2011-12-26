package com.kentchiu.eslpod.cmd;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;

import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.DatabaseHelper;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class RichScriptCommandTest extends AndroidTestCase {

	private RichScriptCommand	command;
	private SQLiteDatabase		database;

	public void isBaseWord() throws Exception {
		ImmutableSet<String> baseWords = ImmutableSet.of("I", "you", "me", "of", "on", "of", "off", "it", "a", "an");
		assertTrue(RichScriptCommand.isBaseWord(baseWords, "I"));
		assertTrue(RichScriptCommand.isBaseWord(baseWords, "You"));
		assertTrue(RichScriptCommand.isBaseWord(baseWords, "you"));
		assertTrue(RichScriptCommand.isBaseWord(baseWords, "on"));
		assertTrue(RichScriptCommand.isBaseWord(baseWords, "On"));
		assertFalse(RichScriptCommand.isBaseWord(baseWords, "foo"));
		assertFalse(RichScriptCommand.isBaseWord(baseWords, "Foo"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		URL resource = getClass().getResource("/script.html");
		DatabaseHelper databaseHelper = new DatabaseHelper(mContext, DatabaseHelper.DATABASE_NAME, null);
		database = databaseHelper.getWritableDatabase();
		database.execSQL("delete from podcast");
		database.execSQL("insert into podcast(_id, link) values(1, '" + resource.toString() + "')");
		command = new RichScriptCommand(mContext, ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, 1));
	}

	public void test7_722_issues() throws Exception {
		//String script722 = "http://www.eslpod.com/website/show_podcast.php?issue_id=10848015";
		String script722 = "http://feedproxy.google.com/~r/EnglishAsASecondLanguagePodcast/~3/8YqeQY3nJ2U/show_podcast.php";

		RichScriptCommand cmd = new RichScriptCommand(mContext, null);
		String script = cmd.fetchScript(script722, "722");
		System.out.println(script);
		//System.out.println(script);
		// line 2 <u>Madwomen</u>  -> not mark as headword
		// line 4 cancelled -> not show in  headword list
		// line 5 cliffhanger! -> not show in headword list  (cause by '!'?)
		// line 6 episodes,  -> not show in headword list  (cause by '!'?)
		// line <u>Reviver</u>   -> not mark as headword
		// last line <img src=..... with="1"/> -> this line should not in script

	}

	public void testExtractScript() throws Exception {
		InputStream is = getClass().getResourceAsStream("/script.html");
		List<String> script = command.extractScript(IOUtils.readLines(is));
		assertThat(script, hasSize(56));
	}

	public void testExtractWords_none_words() throws Exception {
		assertThat(RichScriptCommand.extractWord("foo bar"), Matchers.<String> emptyIterable());
	}

	public void testExtractWords_one_word() throws Exception {
		Iterable<String> words = RichScriptCommand.extractWord("foo <b>bar</b> baz");
		assertThat(words, hasItem("bar"));
	}

	public void testExtractWords_two_words() throws Exception {
		Iterable<String> words = RichScriptCommand.extractWord("<b>foo</b> bar <b>baz</b>");
		assertThat(words, hasItems("foo", "baz"));
		assertThat(words, not(hasItem("bar")));
	}

	public void testExtractWords_whole_script() throws Exception {
		InputStream is = getClass().getResourceAsStream("/script.html");
		List<String> script = command.extractScript(IOUtils.readLines(is));
		Iterable<String> words = RichScriptCommand.extractWord(Joiner.on('\n').join(script));
		assertThat(Iterables.size(words), is(17));
	}

	public void testFetchScriptWithNonAsciiCode() throws Exception {
		String scriptUrl = "http://www.eslpod.com/website/show_podcast.php?issue_id=10718756";
		RichScriptCommand cmd = new RichScriptCommand(mContext, null);
		String script = cmd.fetchScript(scriptUrl, "test");
		assertThat(script, containsString("<b>There’s no way around it</b>"));
	}

	public void testGetScript() throws Exception {
		String script = command.fetchScript(null, "test");
		assertThat(script, startsWith("Cherise:  <b>Rise and shine</b>!"));
	}

	public void testSplitPhaseVerbToWords() throws Exception {
		Iterable<String> foo = RichScriptCommand.splitPhaseVerbToWords("foo");
		assertThat(Iterables.size(foo), is(1));
		Iterable<String> foobar = RichScriptCommand.splitPhaseVerbToWords("foo bar");
		assertThat(Iterables.size(foobar), is(2));
		Iterable<String> foobarbaz = RichScriptCommand.splitPhaseVerbToWords("foo bar baz");
		assertThat(Iterables.size(foobarbaz), is(3));
		assertThat(foobarbaz, hasItems("foo", "bar", "baz"));
	}

}
