package com.kentchiu.eslpod.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Dictionary;

public class WiktionaryCommand extends AbstractDictionaryCommand {

	/**
	 * Internal class to hold a wiki formatting rule. It's mostly a wrapper to
	 * simplify {@link Matcher#replaceAll(String)}.
	 */
	private static class FormatRule {
		private Pattern	mPattern;
		private String	mReplaceWith;

		/**
		 * Create a wiki formatting rule.
		 *
		 * @param pattern Search string to be compiled into a {@link Pattern}.
		 * @param replaceWith String to replace any found occurances with. This
		 *            string can also include back-references into the given
		 *            pattern.
		 */
		public FormatRule(String pattern, String replaceWith) {
			this(pattern, replaceWith, 0);
		}

		/**
		 * Create a wiki formatting rule.
		 *
		 * @param pattern Search string to be compiled into a {@link Pattern}.
		 * @param replaceWith String to replace any found occurances with. This
		 *            string can also include back-references into the given
		 *            pattern.
		 * @param flags Any flags to compile the {@link Pattern} with.
		 */
		public FormatRule(String pattern, String replaceWith, int flags) {
			mPattern = Pattern.compile(pattern, flags);
			mReplaceWith = replaceWith;
		}

		/**
		 * Apply this formatting rule to the given input string, and return the
		 * resulting new string.
		 */
		public String apply(String input) {
			Matcher m = mPattern.matcher(input);
			String output = m.replaceAll(mReplaceWith);
			return output;
		}

	}

	/**
	 * HTML style sheet to include with any {@link #formatWikiText(String)} HTML
	 * results. It formats nicely for a mobile screen, and hides some content
	 * boxes to keep things tidy.
	 */
	/**
	 * Pattern of section titles we're interested in showing. This trims out
	 * extra sections that can clutter things up on a mobile screen.
	 */
	private static final Pattern			sValidSections	= Pattern.compile("(verb|noun|adjective|pronoun|interjection)", Pattern.CASE_INSENSITIVE);

	/**
	 * Pattern that can be used to split a returned wiki page into its various
	 * sections. Doesn't treat children sections differently.
	 */
	private static final Pattern			sSectionSplit	= Pattern.compile("^=+(.+?)=+.+?(?=^=)", Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * List of internal formatting rules to apply when parsing wiki text. These
	 * include indenting various bullets, apply italic and bold styles, and
	 * adding internal linking.
	 */
	private static final List<FormatRule>	sFormatRules	= new ArrayList<FormatRule>();

	static {
		// Format header blocks and wrap outside content in ordered list
		sFormatRules.add(new FormatRule("^=+(.+?)=+", "</ol><h2>$1</h2><ol>", Pattern.MULTILINE));

		// Indent quoted blocks, handle ordered and bullet lists
		sFormatRules.add(new FormatRule("^#+\\*?:(.+?)$", "<blockquote>$1</blockquote>", Pattern.MULTILINE));
		sFormatRules.add(new FormatRule("^#+:?\\*(.+?)$", "<ul><li>$1</li></ul>", Pattern.MULTILINE));
		sFormatRules.add(new FormatRule("^#+(.+?)$", "<li>$1</li>", Pattern.MULTILINE));

		// Add internal links
		//		sFormatRules.add(new FormatRule("\\[\\[([^:\\|\\]]+)\\]\\]", String.format("<a href=\"%s://%s/$1\">$1</a>", WIKI_AUTHORITY, WIKI_LOOKUP_HOST)));
		//		sFormatRules.add(new FormatRule("\\[\\[([^:\\|\\]]+)\\|([^\\]]+)\\]\\]", String.format("<a href=\"%s://%s/$1\">$2</a>", WIKI_AUTHORITY, WIKI_LOOKUP_HOST)));
		sFormatRules.add(new FormatRule("\\[\\[([^:\\|\\]]+)\\]\\]", String.format("$1", "wiktionary", "lookup")));
		sFormatRules.add(new FormatRule("\\[\\[([^:\\|\\]]+)\\|([^\\]]+)\\]\\]", String.format("$2", "wiktionary", "lookup")));

		// Add bold and italic formatting
		sFormatRules.add(new FormatRule("'''(.+?)'''", "<b>$1</b>"));
		sFormatRules.add(new FormatRule("([^'])''([^'].*?[^'])''([^'])", "$1<i>$2</i>$3"));

		// Remove odd category links and convert remaining links into flat text
		sFormatRules.add(new FormatRule("(\\{+.+?\\}+|\\[\\[[^:]+:[^\\\\|\\]]+\\]\\]|" + "\\[http.+?\\]|\\[\\[Category:.+?\\]\\])", "", Pattern.MULTILINE | Pattern.DOTALL));
		sFormatRules.add(new FormatRule("\\[\\[([^\\|\\]]+\\|)?(.+?)\\]\\]", "$2", Pattern.MULTILINE));

	}

	protected WiktionaryCommand(Handler handler, String query) {
		super(handler, query);
	}

	@Override
	public String getContent(String word) throws IOException {
		String url = getQueryUrl(word);
		String join = readAsOneLine(url);
		return extractContent(join);
	}

	@Override
	public String toHtml(String input) {
		if (input == null) {
			return null;
		}

		// Insert a fake last section into the document so our section splitter
		// can correctly catch the last section.
		input = input.concat("\n=Stub section=");

		// Read through all sections, keeping only those matching our filter,
		// and only including the first entry for each title.
		HashSet<String> foundSections = new HashSet<String>();
		StringBuilder builder = new StringBuilder();

		Matcher sectionMatcher = sSectionSplit.matcher(input);
		while (sectionMatcher.find()) {
			String title = sectionMatcher.group(1);
			// part of speech
			if (!foundSections.contains(title) && sValidSections.matcher(title).matches()) {
				String sectionContent = sectionMatcher.group();
				foundSections.add(title);
				builder.append(sectionContent);
			}
		}

		// Our new wiki text is the selected sections only
		input = builder.toString();

		// Apply all formatting rules, in order, to the wiki text
		for (FormatRule rule : sFormatRules) {
			input = rule.apply(input);
		}

		// Return the resulting HTML with style sheet, if we have content left
		if (!TextUtils.isEmpty(input)) {
			String STYLE_SHEET = "<style>h2 {font-size:1.2em;font-weight:normal;} " + "a {color:#6688cc;} ol {padding-left:1.5em;} blockquote {margin-left:0em;} " + ".interProject, .noprint {display:none;} "
					+ "li, blockquote {margin-top:0.5em;margin-bottom:0.5em;}</style>";

			return STYLE_SHEET + input;
		} else {
			return null;
		}
	}

	@Override
	protected int getDictionaryId() {
		return Dictionary.DICTIONARY_WIKITIONARY;
	}

	@Override
	protected String getQueryUrl(String word) {
		return "http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&rvexpandtemplates=true&alllinks=true&titles=" + word;
	}

	private synchronized String extractContent(String content) {
		try {
			// Drill into the JSON response to find the content body
			JSONObject response = new JSONObject(content);
			JSONObject query = response.getJSONObject("query");
			JSONObject pages = query.getJSONObject("pages");
			JSONObject page = pages.getJSONObject((String) pages.keys().next());
			JSONArray revisions = page.getJSONArray("revisions");
			JSONObject revision = revisions.getJSONObject(0);
			return revision.getString("*");
		} catch (JSONException e) {
			Log.w(EslPodApplication.TAG, "Extract json content fiil", e);
			return "";
		}
	}

}
