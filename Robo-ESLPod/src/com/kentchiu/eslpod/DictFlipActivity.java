package com.kentchiu.eslpod;

import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public class DictFlipActivity extends Activity implements OnGestureListener, OnTouchListener, OnClickListener {

	private ViewFlipper			flipper;
	private GestureDetector		gestureDetector;
	private Iterable<WebView>	webViews;
	private TextView			input;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.go:
			updateContent(input.getText().toString());
			break;

		default:
			break;
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_flip_activity);

		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		gestureDetector = new GestureDetector(this, this);

		String word = getIntent().getStringExtra(SearchManager.QUERY);
		input = (TextView) findViewById(R.id.titleTxt);
		String query = word;
		input.setText(query);

		initWebView();
		updateContent(word);
		setTitle("● ○ ○          Dr.eye");
		getApplicationContext();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > 200) {//move to left
			flipper.setInAnimation(getApplicationContext(), R.anim.push_left_in);
			flipper.setOutAnimation(getApplicationContext(), R.anim.push_left_out);
			flipper.showNext();
			flipper.setInAnimation(getApplicationContext(), R.anim.push_right_in);
			flipper.setOutAnimation(getApplicationContext(), R.anim.push_right_out);
		} else if (e2.getX() - e1.getX() > 200) {
			flipper.setInAnimation(getApplicationContext(), R.anim.push_right_in);
			flipper.setOutAnimation(getApplicationContext(), R.anim.push_right_out);
			flipper.showPrevious();
			flipper.setInAnimation(getApplicationContext(), R.anim.push_left_in);
			flipper.setOutAnimation(getApplicationContext(), R.anim.push_left_out);
		}

		View currentView = flipper.getCurrentView();
		switch (currentView.getId()) {
		case R.id.dict1:
			setTitle("● ○ ○          Dr.eye");
			break;
		case R.id.dict2:
			setTitle("○ ● ○          Dictionary");
			break;
		case R.id.dict3:
			setTitle("○ ○ ●          Witionary");
			break;

		default:
			break;
		}

		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {

		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	void updateContent(final String query) {
		for (int dictId = 1; dictId < 4; dictId++) {
			final Cursor c = managedQuery(DictionaryColumns.DICTIONARY_URI, null, DictionaryColumns.WORD + "=? and " + DictionaryColumns.DICTIONARY_ID + "=?", new String[] { query, Integer.toString(dictId) }, null);
			loadContentToWebView(c, dictId);
			if (c.getCount() == 0) {
				fetchContent(query, dictId, c);
			}
		}

	}

	private void fetchContent(final String query, int dictId, final Cursor c) {
		Log.d(EslPodApplication.TAG, "Fetching content [" + query + "] from dict id = " + dictId);
		final int dictId2 = dictId;
		final AbstractDictionaryCommand cmd = AbstractDictionaryCommand.newDictionaryCommand(this, query, dictId);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				cmd.run();
				DictFlipActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						c.requery();
						loadContentToWebView(c, dictId2);
					}
				});
			};
		});
		t.setDaemon(true);
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}

	private void initWebView() {
		webViews = Lists.newArrayList();
		for (int each : new int[] { R.id.dict1, R.id.dict2, R.id.dict3 }) {
			View viewGroup = findViewById(each);
			final WebView webView = (WebView) viewGroup.findViewById(R.id.webview);
			webView.loadDataWithBaseURL("Dictionary", "查詢中....", "text/html", "utf-8", null);
			webView.setOnTouchListener(this);
			webView.setLongClickable(true);
			((List<WebView>) webViews).add(webView);
		}
	}

	private void loadContentToWebView(Cursor c, int dictId) {
		if (c.moveToFirst()) {
			String content = c.getString(c.getColumnIndex(DictionaryColumns.CONTENT));
			String html = AbstractDictionaryCommand.toHtml(content, dictId);
			Iterables.get(webViews, dictId - 1).loadDataWithBaseURL("Dictionary", html, "text/html", "utf-8", null);
			Log.v(EslPodApplication.TAG, "webviews [" + (dictId - 1) + "] loaded content from db");
		}
	}

}