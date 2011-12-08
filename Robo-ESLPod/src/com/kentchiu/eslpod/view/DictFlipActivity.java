package com.kentchiu.eslpod.view;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.AbstractDictionaryCommand;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public class DictFlipActivity extends RoboActivity {

	class MyOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return detector.onTouchEvent(event);
		}
	}

	class MySimpleOnGestureListener extends SimpleOnGestureListener {
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

			updateTitle();
			return true;
		}

	}

	@InjectView(R.id.viewFlipper)
	private ViewFlipper		flipper;
	private GestureDetector	detector;

	@InjectView(R.id.titleTxt)
	private TextView		input;

	@InjectView(R.id.go)
	private Button			go;

	private void createDictView(final int dictId) {
		final DictWebView dictView = new DictWebView(DictFlipActivity.this, dictId);
		dictView.setOnTouchListener(new MyOnTouchListener());
		dictView.query(input.getText().toString().trim());
		flipper.addView(dictView);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_flip_activity);
		input.setText(getIntent().getStringExtra(SearchManager.QUERY));
		createDictView(Dictionary.DICTIONARY_DICTIONARY_DICTIONARY);
		createDictView(Dictionary.DICTIONARY_WIKITIONARY);
		createDictView(Dictionary.DICTIONARY_DREYE_DICTIONARY);
		updateTitle();

		detector = new GestureDetector(new MySimpleOnGestureListener());
		go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String word = input.getText().toString();
				Ln.i("query for :%s", word);
				flipper.removeAllViews();
				createDictView(Dictionary.DICTIONARY_DICTIONARY_DICTIONARY);
				createDictView(Dictionary.DICTIONARY_WIKITIONARY);
				createDictView(Dictionary.DICTIONARY_DREYE_DICTIONARY);

			}
		});
	}

	private void updateTitle() {
		DictWebView currentView = (DictWebView) flipper.getCurrentView();
		switch (currentView.getDictId()) {
		case Dictionary.DICTIONARY_DICTIONARY_DICTIONARY:
			setTitle("● ○ ○          Dictionary");
			break;
		case Dictionary.DICTIONARY_WIKITIONARY:
			setTitle("○ ● ○          Witionary");
			break;
		case Dictionary.DICTIONARY_DREYE_DICTIONARY:
			setTitle("○ ○ ●          Dr.eye");
			break;
		default:
			break;
		}
	}
	/*
	class MyOnGestureListener implements OnGestureListener {

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

	}

	class MyOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return gestureDetector.onTouchEvent(event);
		}
	}

	@InjectView(R.id.viewFlipper)
	private ViewFlipper			flipper;
	private GestureDetector		gestureDetector;
	private Iterable<WebView>	webViews;

	@InjectView(R.id.titleTxt)
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_flip_activity);
		gestureDetector = new GestureDetector(this, new MyOnGestureListener());
		String query = getIntent().getStringExtra(SearchManager.QUERY);
		input.setText(query);
		initWebView();
		updateContent(query);
		setTitle("● ○ ○          Dr.eye");
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
		Ln.d("Fetching content [%s] from dict id = %d", query, dictId);
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
			webView.loadDataWithBaseURL("Dictionary", "Searching....", "text/html", "utf-8", null);
			webView.setOnTouchListener(new MyOnTouchListener());
			webView.setLongClickable(true);
			((List<WebView>) webViews).add(webView);
		}
	}

	private void loadContentToWebView(Cursor c, int dictId) {
		if (c.moveToFirst()) {
			String content = c.getString(c.getColumnIndex(DictionaryColumns.CONTENT));
			String word = c.getString(c.getColumnIndex(DictionaryColumns.WORD));
			String html = AbstractDictionaryCommand.toHtml(this, word, content, dictId);
			Iterables.get(webViews, dictId - 1).loadDataWithBaseURL("Dictionary", html, "text/html", "utf-8", null);
			Ln.v("webviews [%d] loaded content from db", dictId - 1);
		}
	}
	*/
}

class DictWebView extends WebView {

	private Context	context;
	private int		dictId;

	public DictWebView(Context context, int dictId) {
		super(context);
		this.context = context;
		this.dictId = dictId;
	}

	public int getDictId() {
		return dictId;
	}

	public void query(final String word) {
		loadDataWithBaseURL(null, "Searching for [" + word + "]....", "text/html", "utf-8", null);
		final Cursor c = context.getContentResolver().query(DictionaryColumns.DICTIONARY_URI, null, DictionaryColumns.WORD + "=? and " + DictionaryColumns.DICTIONARY_ID + "=?", new String[] { word, Integer.toString(dictId) }, null);
		if (c.moveToFirst()) {
			String content = c.getString(c.getColumnIndex(DictionaryColumns.CONTENT));
			c.getString(c.getColumnIndex(DictionaryColumns.WORD));
			String html = AbstractDictionaryCommand.toHtml(context, word, content, dictId);
			loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
			Ln.v("webviews [%d] loaded content from db", dictId - 1);
		} else {
			new AsyncTask<String, Void, Void>() {

				@Override
				protected Void doInBackground(String... params) {
					final AbstractDictionaryCommand cmd = AbstractDictionaryCommand.newDictionaryCommand(context, params[0], dictId);
					cmd.run();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					c.requery();
					if (c.moveToFirst()) {
						String content = c.getString(c.getColumnIndex(DictionaryColumns.CONTENT));
						c.getString(c.getColumnIndex(DictionaryColumns.WORD));
						String html = AbstractDictionaryCommand.toHtml(context, word, content, dictId);
						loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
						Ln.v("webviews [%d] loaded content from db", dictId - 1);
					}
				};

			}.execute(word);
		}
	}

}