package com.kentchiu.eslpod;

import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RadioGroup;
import android.widget.ViewFlipper;

public class DictFlipActivity extends Activity implements OnGestureListener, OnDoubleTapListener, OnTouchListener {

	private ViewFlipper		flipper;
	private GestureDetector	gestureDetector;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dict_flip_activity);
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		gestureDetector = new GestureDetector(this, this);
		SimpleWikiHelper.prepareUserAgent(this);
		String query = getIntent().getStringExtra(SearchManager.QUERY);
		new WikiDictTask(this).execute(query);
		new GoogleDictTask(this).execute(query);
		new DictionaryDictTask(this).execute(query);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (flipper.isFlipping()) {
			flipper.stopFlipping();
		} else {
			flipper.startFlipping();
		}
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return gestureDetector.onTouchEvent(e);
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
	public boolean onSingleTapConfirmed(MotionEvent e) {

		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {

		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
}