package com.kentchiu.eslpod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.app.ListActivity;
import android.app.SearchManager;
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
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.MediaDownloadService;

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
		Intent intent = new Intent(Intent.ACTION_VIEW, DictionaryColumns.DICTIONARY_URI);
		intent.putExtra(SearchManager.QUERY, item.getTitle());
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
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling left
			Uri.withAppendedPath(DictionaryColumns.DICTIONARY_URI, Long.toString(DICT_GOOGLE));
			Intent intent = new Intent(Intent.ACTION_VIEW, DictionaryColumns.DICTIONARY_URI);
			intent.putExtra("PODCAST_URI", getIntent().getDataString());
			startActivity(intent);
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
		}
		return false;
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
		final ScriptListAdapter adapter = new ScriptListAdapter(this, R.layout.script_list_item, R.id.scriptLine, ImmutableList.copyOf(lines));
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
		Log.i(EslPodApplication.TAG, "working uri:" + uri);
		registerForContextMenu(getListView());
		getContentResolver().registerContentObserver(uri, false, new ContentObserver(handler) {
			@Override
			public void onChange(boolean selfChange) {
				Log.i(EslPodApplication.TAG, "reset adapter");
				setListAdapter(createAdapter(uri));
			}
		});
		final Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		setTitle(c.getString(c.getColumnIndex(PodcastColumns.TITLE)));
		setListAdapter(createAdapter(uri));

		player = new MediaPlayer();
		try {
			String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
			String path = "";
			if (StringUtils.isNotBlank(url)) {
				File file = new File(url);
				if (file.exists()) {
					path = file.getAbsolutePath();
					player.setDataSource(new FileInputStream(file).getFD());
					player.prepareAsync();
				} else {
					path = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
					player.setDataSource(path);
					player.prepareAsync();
					sendDownloadIntent(uri);
				}
			} else {
				path = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
				player.setDataSource(path);
				player.prepareAsync();
				sendDownloadIntent(uri);
			}
			Log.d(EslPodApplication.TAG, "media url : " + path);
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
				Log.d(EslPodApplication.TAG, "progress:" + progress + ", from User:" + fromUser);
				if (fromUser) {
					player.seekTo(progress);
				} else {
					// the event was fired from code and you shouldn't call player.seekTo()
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d(EslPodApplication.TAG, "onStartTrackingTouch");

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(EslPodApplication.TAG, "onStopTrackingTouch");

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

	private void sendDownloadIntent(Uri uri) {
		Intent intent = new Intent(this, MediaDownloadService.class);
		intent.setData(uri);
		startService(intent);
	}

}
