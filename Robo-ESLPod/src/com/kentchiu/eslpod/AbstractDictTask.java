package com.kentchiu.eslpod;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.provider.Dictionary;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AbstractDictTask extends AsyncTask<String, String, String> {

	private final DictFlipActivity	activity;
	private String					query;

	public AbstractDictTask(DictFlipActivity activity) {
		this.activity = activity;
	}

	public DictFlipActivity getActivity() {
		return activity;
	}

	/**
	 * Perform the background query using {@link ExtendedWikiHelper}, which
	 * may return an error message as the result.
	 */
	@Override
	protected String doInBackground(String... args) {
		query = args[0];
		String parsedText = null;

		if (getQuery() != null) {
			// Push our requested word to the title bar
			publishProgress(getQuery());
		}
		//String url = "http://www.google.com.tw/dictionary?q=book&hl=zh-TW&aq=f";
		Cursor c = getActivity().getContentResolver().query(Dictionary.WORDBANK_URI, null, "word=?", new String[] {getQuery()}, null);
		long word_id;
		c.moveToFirst();
		if (c.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(Dictionary.WORD, query);
			Uri uri = getActivity().getContentResolver().insert(Dictionary.WORDBANK_URI , values);
			word_id = ContentUris.parseId(uri);
		} else {
			word_id = c.getLong(c.getColumnIndex(Dictionary.WORD));
		}
		Cursor c2 = getActivity().getContentResolver().query(Dictionary.DICTIONARY_URI, null, "word_id=?", new String[] {Long.toString(word_id)}, null);
		if (c2.getCount() == 0) {
			parsedText = getWebContent();
			ContentValues vs = new ContentValues();
			vs.put(Dictionary.DICTIONARY_ID, Dictionary.DICTIONARY_GOOGLE_SUGGESTION);
			vs.put(Dictionary.CONTENT, parsedText);
			getActivity().getContentResolver().insert(Dictionary.DICTIONARY_URI, vs );
		} else {
			parsedText = c.getString(c.getColumnIndex(Dictionary.CONTENT));
		}

		if (parsedText == null) {
			//parsedText = getString(R.string.empty_result);
			parsedText = "";
		}

		return parsedText;
	}

	protected abstract String getWebContent();

	/**
	 * When finished, push the newly-found entry content into our
	 * {@link WebView} and hide the {@link ProgressBar}.
	 */
	@Override
	protected void onPostExecute(String parsedText) {
		//mTitleBar.startAnimation(mSlideOut);
		//mProgress.setVisibility(View.INVISIBLE);

		View viewGroup = getActivity().findViewById(topViewId());
		TextView textView = (TextView) viewGroup.findViewById(R.id.title);
		textView.setText(getQuery());
		WebView webView = (WebView) viewGroup.findViewById(R.id.webview);
		webView.loadDataWithBaseURL(ExtendedWikiHelper.WIKI_AUTHORITY, parsedText, ExtendedWikiHelper.MIME_TYPE, ExtendedWikiHelper.ENCODING, null);
		webView.setOnTouchListener(getActivity());
		webView.setLongClickable(true);

	}

	/**
	 * Before jumping into background thread, start sliding in the
	 * {@link ProgressBar}. We'll only show it once the animation finishes.
	 */
	@Override
	protected void onPreExecute() {
		//mTitleBar.startAnimation(mSlideIn);
	}

	/**
	 * Our progress update pushes a title bar update.
	 */
	@Override
	protected void onProgressUpdate(String... args) {
	}

	protected abstract int topViewId();

	public String getQuery() {
		return query;
	}


}