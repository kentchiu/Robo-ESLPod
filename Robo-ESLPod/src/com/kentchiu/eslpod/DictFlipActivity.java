package com.kentchiu.eslpod;

import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;
import com.kentchiu.eslpod.service.DictionaryService;

public class DictFlipActivity extends Activity implements OnGestureListener, OnTouchListener {

	private ViewFlipper			flipper;
	private GestureDetector		gestureDetector;
	private Iterable<WebView>	webViews;

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
		Cursor c = managedQuery(WordBankColumns.WORDBANK_URI, null, "word=?", new String[] { getIntent().getStringExtra(SearchManager.QUERY) }, null);
		if (c.moveToFirst()) {
			long wordId = c.getLong(c.getColumnIndex(BaseColumns._ID));
			Cursor c2 = managedQuery(DictionaryColumns.DICTIONARY_URI, null, "word_id=?", new String[] { Long.toString(wordId) }, null);
			while (c2.moveToNext()) {
				int dictId = c2.getInt(c2.getColumnIndex(DictionaryColumns.DICTIONARY_ID));
				String content = c2.getString(c2.getColumnIndex(DictionaryColumns.CONTENT));
				AbstractDictionaryCommand cmd = AbstractDictionaryCommand.newDictionaryCommand(this, ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId), dictId);
				String html = cmd.toHtml(content);
				Iterables.get(webViews, dictId - 1).loadDataWithBaseURL("Dictionary", html, "text/html", "utf-8", null);
			}
		}
	}

	private void createWebViews() {
		webViews = Lists.newArrayList();
		for (int each : new int[] { R.id.dict1, R.id.dict2, R.id.dict3 }) {
			View viewGroup = findViewById(each);
			TextView textView = (TextView) viewGroup.findViewById(R.id.title);
			String query = getIntent().getStringExtra(SearchManager.QUERY);
			textView.setText(query);
			final WebView webView = (WebView) viewGroup.findViewById(R.id.webview);
			webView.loadDataWithBaseURL("Dictionary", "查詢中....", "text/html", "utf-8", null);
			webView.setOnTouchListener(this);
			webView.setLongClickable(true);
			((List<WebView>) webViews).add(webView);
		}
		Cursor c = managedQuery(WordBankColumns.WORDBANK_URI, null, "word=?", new String[] { getIntent().getStringExtra(SearchManager.QUERY) }, null);
		if (c.moveToFirst()) {
			long wordId = c.getLong(c.getColumnIndex(BaseColumns._ID));
			Uri workbankUri = ContentUris.withAppendedId(WordBankColumns.WORDBANK_URI, wordId);
			Intent newIntent = new Intent(this, DictionaryService.class);
			newIntent.putExtra(DictionaryService.COMMAND, DictionaryService.COMMAND_DOWNLOAD_DICTIONARIES);
			newIntent.putExtra(DictionaryService.NO_WAIT, true);
			newIntent.setData(workbankUri);
			startService(newIntent);
		}

	}

}