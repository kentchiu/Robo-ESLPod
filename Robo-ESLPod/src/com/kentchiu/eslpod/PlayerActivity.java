package com.kentchiu.eslpod;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.provider.Dictionary;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PlayerActivity extends ListActivity implements OnTouchListener, OnGestureListener, OnClickListener {
	private static final int	DICT_GOOGLE			= 1;
	private static final int	FLING_MIN_DISTANCE	= 100;
	private static final int	FLING_MIN_VELOCITY	= 200;
	private GestureDetector		gd;
	private MediaPlayer			player;
	private SeekBar				seekBar;
	private Handler				handler				= new Handler();

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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		ScriptListAdapter adapter = (ScriptListAdapter) getListAdapter();
		final String item = (String) adapter.getItem(info.position);
		Iterable<String> words = adapter.extractWord();
		Iterable<String> filter = Iterables.filter(words, new Predicate<String>() {

			@Override
			public boolean apply(String input) {
				return StringUtils.indexOfIgnoreCase(item, input) != -1;
			}
		});
		menu.setHeaderTitle("字典搜尋");
		int i = 1;
		for (String each : filter) {
			menu.add(0, i++, 0, each);
		}
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

	ScriptListAdapter createAdapter(final Uri uri) {
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		String script = c.getString(c.getColumnIndex(PodcastColumns.SCRIPT));
		String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
		Iterable<String> lines = Splitter.on("\n").trimResults().split(script);
		final ScriptListAdapter adapter = new ScriptListAdapter(this, R.layout.listitem, R.id.scriptLine, ImmutableList.copyOf(lines));
		if (StringUtils.isNotBlank(richScript)) {
			adapter.setRichScript(richScript);
		}
		return adapter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);

		final Uri uri = getIntent().getData();
		Log.i(EslPodApplication.LOG_TAG, "working uri:" + uri);
		registerForContextMenu(getListView());
		getContentResolver().registerContentObserver(uri, false, new ContentObserver(handler) {
			@Override
			public void onChange(boolean selfChange) {
				Log.i(EslPodApplication.LOG_TAG, "reset adapter");
				setListAdapter(createAdapter(uri));
			}
		});
		final Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		setTitle(c.getString(c.getColumnIndex(PodcastColumns.TITLE)));
		setListAdapter(createAdapter(uri));

		player = new MediaPlayer();
		try {
			String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URI));
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
