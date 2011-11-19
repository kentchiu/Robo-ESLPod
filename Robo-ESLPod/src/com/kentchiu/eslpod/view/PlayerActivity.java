package com.kentchiu.eslpod.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectView;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.LocalBinder;
import com.kentchiu.eslpod.service.MediaService;
import com.kentchiu.eslpod.service.WordFetchService;
import com.kentchiu.eslpod.view.adapter.ScriptListAdapter;

public class PlayerActivity extends RoboListActivity implements OnClickListener {

	private class MediaConnection implements ServiceConnection {

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

	}

	private class MySeekbarChangeListener implements OnSeekBarChangeListener {
		private boolean	dragging	= false;

		public boolean isDragging() {
			return dragging;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			Log.d(EslPodApplication.TAG, "progress:" + progress + ", from User:" + fromUser);
			if (fromUser) {
				player.seekTo(progress);
				int sec = progress / 1000;
				String pos = String.format("%02d:%02d", sec / 60, sec % 60);
				playPosition.setText(pos);
			} else {
				// the event was fired from code and you shouldn't call player.seekTo()
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			dragging = true;
			Log.w(EslPodApplication.TAG, "start -" + seekBar.getProgress());
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			dragging = false;
			handler.removeMessages(SHOW_PROGRESS);
			Message m = handler.obtainMessage();
			m.what = SHOW_PROGRESS;
			m.arg1 = player.getCurrentPosition();
			handler.sendMessage(m);
			Log.w(EslPodApplication.TAG, "stop -" + seekBar.getProgress());
		}
	}

	private class SyncHandler extends Handler {
		@SuppressWarnings("synthetic-access")
		@Override
		public void handleMessage(Message msg) {
			if (SHOW_PROGRESS == msg.what) {
				int current = msg.arg1;
				seekBar.setProgress(current);
				if (!seekbarChangeListener.isDragging()) {
					Message m = obtainMessage();
					m.what = SHOW_PROGRESS;
					m.arg1 = player.getCurrentPosition();
					sendMessageDelayed(m, 1000 - player.getCurrentPosition() % 1000);
				}
			} else {
				throw new IllegalArgumentException("Unkonw msg : " + msg);
			}
		}
	}

	private GestureDetector			gd;
	private Handler					handler					= new SyncHandler();
	private MediaConnection			mediaConn				= new MediaConnection();
	private MediaPlayer				player;
	@InjectView(R.id.playPosition)
	private TextView				playPosition;
	@InjectView(R.id.seekBar)
	private SeekBar					seekBar;
	private MySeekbarChangeListener	seekbarChangeListener	= new MySeekbarChangeListener();
	private static final int		SHOW_PROGRESS			= 1;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playButton:
			if (!player.isPlaying()) {
				player.start();
				findViewById(R.id.playButton).setVisibility(View.GONE);
				findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
			}
			seekBar.setMax(player.getDuration());
			Message m = handler.obtainMessage();
			m.what = SHOW_PROGRESS;
			m.arg1 = player.getCurrentPosition();
			handler.sendMessage(m);
			break;
		case R.id.pauseButton:
			if (player.isPlaying()) {
				player.pause();
				findViewById(R.id.playButton).setVisibility(View.VISIBLE);
				findViewById(R.id.pauseButton).setVisibility(View.GONE);
			}
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
		getContentResolver().registerContentObserver(uri, false, new ContentObserver(new Handler()) {
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
		bindService(intent, mediaConn, BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		unbindService(mediaConn);
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void fetchWord(final Uri uri) {
		Intent intent = new Intent(this, WordFetchService.class);
		intent.setData(uri);
		startService(intent);
	}

	private void initSeekBar() {
		seekBar.setOnSeekBarChangeListener(seekbarChangeListener);
		getListView().setLongClickable(true);
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
