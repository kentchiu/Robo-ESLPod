package com.kentchiu.eslpod.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.LocalBinder;
import com.kentchiu.eslpod.service.MediaService;
import com.kentchiu.eslpod.service.WordFetchService;

public class PlayerActivity extends ListActivity implements OnTouchListener, OnGestureListener, OnClickListener {

	private GestureDetector		gd;
	private SeekBar				seekBar;
	private Handler				handler	= new Handler();
	private MediaPlayer			player;
	private ImageButton playButton;
	private ServiceConnection	connection;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playButton:
			if (player.isPlaying()) {
				player.pause();
				playButton.setBackgroundResource(R.drawable.playback_play);
			} else {
				player.start();
				playButton.setBackgroundResource(R.drawable.playback_pause);
			}
			updatingPlayPosition();
			break;
		case R.id.forwardButton:
			// action for myButton2 click
			int pos = player.getCurrentPosition();
			player.seekTo(pos + 10 * 1000);
			break;
		case R.id.reverseButton:
			int pos2 = player.getCurrentPosition();
			player.seekTo(pos2 - 10 * 1000);
			break;
		default:
			break;
		}
	}

	private void updatingPlayPosition() {
		final TextView playPosition = (TextView) findViewById(R.id.playPosition);
		final Handler h = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String pos = String.valueOf(msg.what);
				playPosition.setText(pos);
			}
		};

		new Thread(new Runnable() {
			@Override
			public void run() {
				while(player.isPlaying()) {
					h.sendEmptyMessage(player.getCurrentPosition());
				}
			}

		}).start();
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
		menu.setHeaderTitle("字典搜尋");
		final String item = (String) adapter.getItem(info.position);
		Iterable<String> words = RichScriptCommand.extractWord(adapter.getRichScript());
		Iterable<String> filter = listWordsMatchToMenuItem(words, item);
		int i = 1;
		for (String each : RichScriptCommand.headword(PlayerActivity.this, filter)) {
			menu.add(0, i++, 0, each);
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
		if (c.moveToFirst()) {
			String script = c.getString(c.getColumnIndex(PodcastColumns.SCRIPT));
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			String link = c.getString(c.getColumnIndex(PodcastColumns.LINK));
			if (StringUtils.isBlank(richScript)) {
				new Thread(new RichScriptCommand(this, uri, link)).start();
			}
			Iterable<String> lines = Splitter.on("\n").trimResults().split(script);
			ScriptListAdapter result = new ScriptListAdapter(this, R.layout.script_list_item, R.id.scriptLine, ImmutableList.copyOf(lines));
			if (StringUtils.isNotBlank(richScript)) {
				result.setRichScript(richScript);
			}
			return result;
		} else {
			return new ScriptListAdapter(this, R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);

		registerForContextMenu(getListView());
		final Uri uri = getIntent().getData();
		Log.i(EslPodApplication.TAG, "working uri:" + uri);
		getContentResolver().registerContentObserver(uri, false, new ContentObserver(handler) {
			@Override
			public void onChange(boolean selfChange) {
				Log.i(EslPodApplication.TAG, "reset adapter");
				setListAdapter(createAdapter(uri));
			}
		});
		final Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		String title = c.getString(c.getColumnIndex(PodcastColumns.TITLE));
		setTitle(title);
		setListAdapter(createAdapter(uri));
		fetchWord(uri);

		Intent intent = new Intent(this, MediaService.class);
		connection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className, IBinder service) {
				MediaService s = ((LocalBinder<MediaService>) service).getService();
				s.prepare(getIntent().getData());
				player = s.getPlayer();
				initSeekBar();
			}

			@Override
			public void onServiceDisconnected(ComponentName className) {
				// As our service is in the same process, this should never be called
			}
		};
		bindService(intent, connection, BIND_AUTO_CREATE);
		playButton = (ImageButton) findViewById(R.id.playButton);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		unbindService(connection);
		super.onDestroy();
	}

	private void fetchWord(final Uri uri) {
		Intent intent = new Intent(this, WordFetchService.class);
		intent.setData(uri);
		startService(intent);
	}

	private void initSeekBar() {
		new MediaController(this);
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
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
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

	private Iterable<String> listWordsMatchToMenuItem(Iterable<String> words, final String item) {
		Iterable<String> filter = Iterables.filter(words, new Predicate<String>() {

			@Override
			public boolean apply(String input) {
				Matcher matcher = Pattern.compile("\\b" + input + "\\b").matcher(item);
				return matcher.find();
			}
		});
		return filter;
	}

}
