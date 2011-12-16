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