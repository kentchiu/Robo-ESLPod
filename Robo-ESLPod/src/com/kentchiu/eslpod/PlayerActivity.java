package com.kentchiu.eslpod;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PlayerActivity extends ListActivity implements OnTouchListener, OnGestureListener, OnClickListener {
	private static final int	DICT_GOOGLE			= 1;
	private static final int	FLING_MIN_DISTANCE	= 100;
	private static final int	FLING_MIN_VELOCITY	= 200;
	private GestureDetector		gd;
	private MediaPlayer			player;
	private SeekBar				seekBar;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playButton:
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.start();
			}
			break;
		case R.id.prevButton:
			// action for myButton2 click
			break;
		case R.id.nextButton:
			// action for myButton2 click
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Toast.makeText(this, "您選擇的是" + item.getTitle(), Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Intent.ACTION_VIEW, Dictionary.DICTIONARY_URI);
		intent.putExtra("query", item.getTitle());
		startActivity(intent);
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//		super.onCreateContextMenu(menu, v, menuInfo);
		//		MenuItem item = menu.getItem(0);
		//		MenuInflater inflater = getMenuInflater();
		//		inflater.inflate(R.menu.context_menu, menu);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		System.out.println(info.targetView);
		menu.setHeaderTitle("Action");
		menu.add(0, 0, 0, "單字1");
		menu.add(0, 1, 0, "單字2");
		menu.add(0, 2, 0, "單字3");
		menu.add(0, 3, 0, "單字4");

	}

	@Override
	public boolean onDown(MotionEvent e) {
		System.out.println("onDown...........");
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		System.out.println("onFling..............");
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling left
			Uri.withAppendedPath(Dictionary.DICTIONARY_URI, Long.toString(DICT_GOOGLE));
			Intent intent = new Intent(Intent.ACTION_VIEW, Dictionary.DICTIONARY_URI);
			System.out.println(getIntent().getData());
			System.out.println(getIntent().toUri(0));
			intent.putExtra("PODCAST_URI", getIntent().getDataString());
			startActivity(intent);
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);

		Uri uri = getIntent().getData();
		Log.i(EslPodApplication.LOG_TAG, "working uri:" + uri);
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		String title = c.getString(PodcastColumns.INDEX_OF_TITLE);
		setTitle(title);
		String script;
		String richScript = c.getString(PodcastColumns.INDEX_OF_RICH_SCRIPT);
		if (StringUtils.isNotBlank(richScript)) {
			script = richScript;
		} else {
			script = c.getString(PodcastColumns.INDEX_OF_SCRIPT);
		}
		Log.d(EslPodApplication.LOG_TAG, "script:" + script);
		Iterable<String> lines = Splitter.on("\n").trimResults().split(script);
		setListAdapter(new ScriptListAdapter(this, R.layout.listitem, R.id.scriptLine, ImmutableList.copyOf(lines)));
		registerForContextMenu(getListView());

		player = new MediaPlayer();
		//player.setDataSource(this, c.getString(PodcastColumns.INDEX_OF_MEDIA_ID));
		String url = c.getString(PodcastColumns.INDEX_OF_MEDIA_URI);
		try {
			player.setDataSource(url);
			player.prepareAsync();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//controller = new MediaController(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);

		OnSeekBarChangeListener sbcl = new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.d(EslPodApplication.LOG_TAG, "progress:" + progress + ", from User:" + fromUser);
				if (fromUser) {
					player.seekTo(progress);
				} else {
					// the event was fired from code and you shouldn't call player.seekTo()
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d(EslPodApplication.LOG_TAG, "onStartTrackingTouch");

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(EslPodApplication.LOG_TAG, "onStopTrackingTouch");

			}
		};
		seekBar.setOnSeekBarChangeListener(sbcl);

		gd = new GestureDetector(this);
		getListView().setLongClickable(true);
		getListView().setOnTouchListener(this);

		Thread syncSeekBarThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int currentPosition = 0;
				int total = player.getDuration();
				seekBar.setMax(total);
				while (player != null && currentPosition < total) {
					try {
						Thread.sleep(1000);
						currentPosition = player.getCurrentPosition();
					} catch (InterruptedException e) {
						return;
					} catch (Exception e) {
						return;
					}
					seekBar.setProgress(currentPosition);
				}
			}
		});
		syncSeekBarThread.start();
	}

	@Override
	protected void onDestroy() {
		player.release();
		super.onDestroy();
	}

}
