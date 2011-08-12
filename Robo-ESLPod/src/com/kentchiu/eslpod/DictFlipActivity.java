package com.kentchiu.eslpod;

import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.RadioGroup;
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.go:
			v.getTag(1);
			v.getTag(2);

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

		createWebViews();
		updateContent();

		getContentResolver().registerContentObserver(DictionaryColumns.DICTIONARY_URI, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				updateContent();
			};
		});
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
		} else {
			return true;
		}
		RadioGroup indicator = (RadioGroup) findViewById(R.id.indicator);
		switch (flipper.getCurrentView().getId()) {
		case R.id.dict1:
			indicator.check(R.id.idxDict1);
			break;
		case R.id.dict2:
			indicator.check(R.id.idxDict2);
			break;
		case R.id.dict3:
			indicator.check(R.id.idxDict3);
			break;
		default:
			indicator.check(R.id.idxDict1);
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

	void updateContent() {
		String query = getIntent().getStringExtra(SearchManager.QUERY);
		Cursor c = managedQuery(DictionaryColumns.DICTIONARY_URI, null, DictionaryColumns.WORD + "=?", new String[] { query }, null);
		while (c.moveToNext()) {
			int dictId = c.getInt(c.getColumnIndex(DictionaryColumns.DICTIONARY_ID));
			String content = c.getString(c.getColumnIndex(DictionaryColumns.CONTENT));
			AbstractDictionaryCommand cmd = AbstractDictionaryCommand.newDictionaryCommand(null, query, dictId);
			String html = cmd.toHtml(content);
			Iterables.get(webViews, dictId - 1).loadDataWithBaseURL("Dictionary", html, "text/html", "utf-8", null);
		}
	}

	private void createWebViews() {
		webViews = Lists.newArrayList();
		String word = getIntent().getStringExtra(SearchManager.QUERY);
		for (int each : new int[] { R.id.dict1, R.id.dict2, R.id.dict3 }) {
			View viewGroup = findViewById(each);
			TextView textView = (TextView) viewGroup.findViewById(R.id.titleTxt);
			String query = word;
			textView.setText(query);
			//			Button go = (Button) viewGroup.findViewById(R.id.go);
			//			go.setTag(1, each);
			//			go.setTag(2, query);
			final WebView webView = (WebView) viewGroup.findViewById(R.id.webview);
			webView.loadDataWithBaseURL("Dictionary", "查詢中....", "text/html", "utf-8", null);
			webView.setOnTouchListener(this);
			webView.setLongClickable(true);
			((List<WebView>) webViews).add(webView);
		}
	}

}