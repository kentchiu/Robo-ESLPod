package com.kentchiu.eslpod;

import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AbstractDictTask extends AsyncTask<String, String, String> {

	private final DictFlipActivity	activity;
	private String	query;


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

		if (query != null) {
			// Push our requested word to the title bar
			publishProgress(query);
		}
		//String url = "http://www.google.com.tw/dictionary?q=book&hl=zh-TW&aq=f";
		parsedText = getWebContent();
		//parsedText = HttpUtils.getContent(DictFlipActivity.this, url);

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
		textView.setText(query);
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

}