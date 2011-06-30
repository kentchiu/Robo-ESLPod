package com.kentchiu.eslpod;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.kentchiu.eslpod.provider.Dictionary;

public class GoogleDictActivity extends Activity implements OnTouchListener, OnGestureListener {
	private GestureDetector		gd;
	private static final int	FLING_MIN_DISTANCE	= 100;
	private static final int	FLING_MIN_VELOCITY	= 200;

	@Override
	public boolean onDown(MotionEvent e) {
		System.out.println("onDown...........");
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		System.out.println("onFling..............");
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling left
			String uri = getIntent().getStringExtra("PODCAST_URI");
			Uri podcastUri = Uri.parse(uri);
			Intent intent = new Intent(Intent.ACTION_VIEW, podcastUri);
			startActivity(intent);
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		System.out.println("onLongPress............");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		System.out.println("onScroll...............");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		System.out.println("onShowPress.............");

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		System.out.println("onSingleTapUp...............");
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		System.out.println("onTouch..............");
		return gd.onTouchEvent(event);
	}

	// http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=china
	// http://www.google.com/dictionary/json?callback=dict_api.callbacks.id100&sl=en&tl=zh-TW&restrict=pr%2Cde&client=te&q=book
	// http://msdn.microsoft.com/en-us/library/ff512419.aspx
	// wiki
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gd = new GestureDetector(this);
		//setLongClickable(true);
		//setOnTouchListener(this);

		TextView view = new TextView(this);
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		view.setLayoutParams(p);
		view.setLongClickable(true);
		view.setOnTouchListener(this);
		setContentView(view);

		String query = getIntent().getStringExtra("query");
		setTitle("Google Dictionary -" + query);

		Cursor cursor = managedQuery(Dictionary.DICTIONARY_URI, null, "query=book", null, null);
		cursor.moveToFirst();
		cursor.getString(0);

		view.setText(query);
	}

}
