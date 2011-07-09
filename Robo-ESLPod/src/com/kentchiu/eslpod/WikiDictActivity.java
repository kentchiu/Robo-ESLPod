/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kentchiu.eslpod;

import java.util.Stack;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kentchiu.eslpod.SimpleWikiHelper.ApiException;
import com.kentchiu.eslpod.SimpleWikiHelper.ParseException;

/**
 * Activity that lets users browse through Wiktionary content. This is just the
 * user interface, and all API communication and parsing is handled in
 * {@link ExtendedWikiHelper}.
 */
public class WikiDictActivity extends Activity implements AnimationListener {
	/**
	 * Background task to handle Wiktionary lookups. This correctly shows and
	 * hides the loading animation from the GUI thread before starting a
	 * background query to the Wiktionary API. When finished, it transitions
	 * back to the GUI thread where it updates with the newly-found entry.
	 */
	private class LookupTask extends AsyncTask<String, String, String> {
		/**
		 * Perform the background query using {@link ExtendedWikiHelper}, which
		 * may return an error message as the result.
		 */
		@Override
		protected String doInBackground(String... args) {
			String query = args[0];
			String parsedText = null;

			try {
				// If query word is null, assume request for random word
				if (query == null) {
					query = ExtendedWikiHelper.getRandomWord();
				}

				if (query != null) {
					// Push our requested word to the title bar
					publishProgress(query);
					String wikiText = SimpleWikiHelper.getPageContent(query, true);
					parsedText = ExtendedWikiHelper.formatWikiText(wikiText);
				}
			} catch (ApiException e) {
				Log.e(TAG, "Problem making wiktionary request", e);
			} catch (ParseException e) {
				Log.e(TAG, "Problem making wiktionary request", e);
			}

			if (parsedText == null) {
				parsedText = getString(R.string.empty_result);
			}

			return parsedText;
		}

		/**
		 * When finished, push the newly-found entry content into our
		 * {@link WebView} and hide the {@link ProgressBar}.
		 */
		@Override
		protected void onPostExecute(String parsedText) {
			mTitleBar.startAnimation(mSlideOut);
			mProgress.setVisibility(View.INVISIBLE);

			setEntryContent(parsedText);
		}

		/**
		 * Before jumping into background thread, start sliding in the
		 * {@link ProgressBar}. We'll only show it once the animation finishes.
		 */
		@Override
		protected void onPreExecute() {
			mTitleBar.startAnimation(mSlideIn);
		}

		/**
		 * Our progress update pushes a title bar update.
		 */
		@Override
		protected void onProgressUpdate(String... args) {
			String searchWord = args[0];
			setEntryTitle(searchWord);
		}
	}

	private static final String	TAG				= "WikiDictActivity";
	private View				mTitleBar;
	private TextView			mTitle;
	private ProgressBar			mProgress;

	private WebView				mWebView;
	private Animation			mSlideIn;

	private Animation			mSlideOut;

	/**
	 * History stack of previous words browsed in this session. This is
	 * referenced when the user taps the "back" key, to possibly intercept and
	 * show the last-visited entry, instead of closing the activity.
	 */
	private Stack<String>		mHistory		= new Stack<String>();

	private String				mEntryTitle;

	/**
	 * Keep track of last time user tapped "back" hard key. When pressed more
	 * than once within {@link #BACK_THRESHOLD}, we treat let the back key fall
	 * through and close the app.
	 */
	private long				mLastPress		= -1;

	private static final long	BACK_THRESHOLD	= DateUtils.SECOND_IN_MILLIS / 2;

	/**
	 * Make the {@link ProgressBar} visible when our in-animation finishes.
	 */
	@Override
	public void onAnimationEnd(Animation animation) {
		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// Not interested if the animation repeats
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// Not interested when the animation starts
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.wiki_dict_activity);

		// Load animations used to show/hide progress bar
		mSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
		mSlideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);

		// Listen for the "in" animation so we make the progress bar visible
		// only after the sliding has finished.
		mSlideIn.setAnimationListener(this);

		mTitleBar = findViewById(R.id.title_bar);
		mTitle = (TextView) findViewById(R.id.title);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mWebView = (WebView) findViewById(R.id.webview);

		// Make the view transparent to show background
		mWebView.setBackgroundColor(0);

		// Prepare User-Agent string for wiki actions
		SimpleWikiHelper.prepareUserAgent(this);

		// Handle incoming intents as possible searches or links
		onNewIntent(getIntent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.lookup, menu);
		return true;
	}

	/**
	 * Intercept the back-key to try walking backwards along our word history
	 * stack. If we don't have any remaining history, the key behaves normally
	 * and closes this activity.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle back key as long we have a history stack
		if (keyCode == KeyEvent.KEYCODE_BACK && !mHistory.empty()) {

			// Compare against last pressed time, and if user hit multiple times
			// in quick succession, we should consider bailing out early.
			long currentPress = SystemClock.uptimeMillis();
			if (currentPress - mLastPress < BACK_THRESHOLD) {
				return super.onKeyDown(keyCode, event);
			}
			mLastPress = currentPress;

			// Pop last entry off stack and start loading
			String lastEntry = mHistory.pop();
			startNavigating(lastEntry, false);

			return true;
		}

		// Otherwise fall through to parent
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Because we're singleTop, we handle our own new intents. These usually
	 * come from the {@link SearchManager} when a search is requested, or from
	 * internal links the user clicks on.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			// Start query for incoming search request
			String query = intent.getStringExtra(SearchManager.QUERY);
			startNavigating(query, true);

		} else if (Intent.ACTION_VIEW.equals(action)) {
			String query =  intent.getStringExtra(SearchManager.QUERY);
			startNavigating(query, true);
		} else {
			// If not recognized, then start showing random word
			startNavigating("Hello", true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.lookup_search: {
			onSearchRequested();
			return true;
		}
		}
		return false;
	}

	/**
	 * Set the content for the current entry. This will update our
	 * {@link WebView} to show the requested content.
	 */
	protected void setEntryContent(String entryContent) {
		mWebView.loadDataWithBaseURL(ExtendedWikiHelper.WIKI_AUTHORITY, entryContent, ExtendedWikiHelper.MIME_TYPE, ExtendedWikiHelper.ENCODING, null);
	}

	/**
	 * Set the title for the current entry.
	 */
	protected void setEntryTitle(String entryText) {
		mEntryTitle = entryText;
		mTitle.setText(mEntryTitle);
	}

	/**
	 * Start navigating to the given word, pushing any current word onto the
	 * history stack if requested. The navigation happens on a background thread
	 * and updates the GUI when finished.
	 *
	 * @param word The dictionary word to navigate to.
	 * @param pushHistory If true, push the current word onto history stack.
	 */
	private void startNavigating(String word, boolean pushHistory) {
		// Push any current word onto the history stack
		if (!TextUtils.isEmpty(mEntryTitle) && pushHistory) {
			mHistory.add(mEntryTitle);
		}

		// Start lookup for new word in background
		new LookupTask().execute(word);
	}
}
