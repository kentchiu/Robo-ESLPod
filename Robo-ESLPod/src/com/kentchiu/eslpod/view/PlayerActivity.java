package com.kentchiu.eslpod.view;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.BatchWordCommand;
import com.kentchiu.eslpod.cmd.MediaDownloadCommand;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.MusicService;
import com.kentchiu.eslpod.view.adapter.ScriptListAdapter;

public class PlayerActivity extends RoboListActivity implements MediaPlayerControl, SeekBar.OnSeekBarChangeListener {

	public class PlaybackOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.playButton:
			case R.id.pauseButton:
				if (MusicService.getInstance() == null || !MusicService.getInstance().isPlaying()) {
					start();
					playButton.setVisibility(View.GONE);
					pauseButton.setVisibility(View.VISIBLE);
				} else {
					pauseButton.setVisibility(View.GONE);
					playButton.setVisibility(View.VISIBLE);
					pause();
				}
				break;
			default:
				break;
			}

		}

	}

	MediaController		ctrl;
	String				mUrl;
	Resources			res;
	@InjectView(R.id.musicCurrentLoc)
	TextView			musicCurLoc;
	@InjectView(R.id.musicDuration)
	TextView			musicDuration;
	@InjectView(R.id.musicSeekBar)
	SeekBar				musicSeekBar;
	@InjectView(R.id.playButton)
	ImageButton			playButton;
	@InjectView(R.id.pauseButton)
	ImageButton			pauseButton;
	@InjectView(R.id.reverseButton)
	ImageButton			reverseButton;
	@InjectView(R.id.forwardButton)
	ImageButton			forwardButton;
	@InjectView(R.id.downloadButton)
	ImageButton			downloadButton;

	protected boolean	musicThreadFinished	= false;

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	ScriptListAdapter createAdapter(final Uri uri) {
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		if (c.moveToFirst()) {
			String script = c.getString(c.getColumnIndex(PodcastColumns.SCRIPT));
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			c.getString(c.getColumnIndex(PodcastColumns.LINK));
			if (StringUtils.isBlank(richScript)) {
				//new Thread(new RichScriptCommand(this, uri, link)).start();
			}
			Iterable<String> lines = Splitter.on("\n").trimResults().split(StringUtils.substringBefore(script, "Script by Dr. Lucy Tse"));
			ImmutableList<String> copyOf = ImmutableList.copyOf(lines);
			ScriptListAdapter result = new ScriptListAdapter(this, R.layout.script_list_item, R.id.scriptLine, copyOf);
			if (StringUtils.isNotBlank(richScript)) {
				result.setRichScript(richScript);
			}
			return result;
		} else {
			return new ScriptListAdapter(this, R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		}
	}

	@SuppressWarnings("boxing")
	protected String getAsTime(int t) {
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toSeconds(t) / 60, TimeUnit.MILLISECONDS.toSeconds(t) - TimeUnit.MILLISECONDS.toSeconds(t) / 60 * 60);
	}

	@Override
	public int getBufferPercentage() {
		if (MusicService.getInstance() != null) {
			return MusicService.getInstance().getBufferPercentage();
		}
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (MusicService.getInstance() != null) {
			return MusicService.getInstance().getCurrentPosition();
		}
		return 0;
	}

	@Override
	public int getDuration() {
		if (MusicService.getInstance() != null) {
			return MusicService.getInstance().getMusicDuration();
		}
		return 0;
	}

	@Override
	public boolean isPlaying() {
		if (MusicService.getInstance() != null) {
			return MusicService.getInstance().isPlaying();
		}
		return false;
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

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW, DictionaryColumns.DICTIONARY_URI);
		intent.putExtra(SearchManager.QUERY, item.getTitle());
		startActivity(intent);
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);

		registerForContextMenu(getListView());
		final Uri uri = getIntent().getData();
		Ln.i("working uri:" + uri);
		getContentResolver().registerContentObserver(uri, false, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				Ln.i("reset adapter");
				setListAdapter(createAdapter(uri));
			}
		});
		final Cursor c = getContentResolver().query(getIntent().getData(), null, null, null, null);
		//mUrl = "http://www.vorbis.com/music/Epoq-Lepidoptera.ogg";
		if (!c.moveToFirst()) {
			throw new IllegalStateException();
		}
		String localUrl = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
		final String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
		String title = c.getString(c.getColumnIndex(PodcastColumns.TITLE));

		setTitle(title);
		setListAdapter(createAdapter(uri));

		new BatchWordCommand(this, getIntent()).run();

		ctrl = new MediaController(this);
		ctrl.setMediaPlayer(this);
		//ctrl.setAnchorView(songPicture);

		//		musicCurLoc = (TextView) findViewById(R.id.musicCurrentLoc);
		//		musicDuration = (TextView) findViewById(R.id.musicDuration);
		//		musicSeekBar = (SeekBar) findViewById(R.id.musicSeekBar);
		//playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
		musicSeekBar.setOnSeekBarChangeListener(this);

		//		playPauseButton.setOnClickListener(new OnClickListener() {
		//			@Override
		//			public void onClick(View v) {
		//				// Perform action on clicks
		//				if (playPauseButton.isChecked()) { // Checked -> Pause icon visible
		//					start();
		//				} else { // Unchecked -> Play icon visible
		//					pause();
		//				}
		//			}
		//		});
		PlaybackOnClickListener playbackOnClickListener = new PlaybackOnClickListener();
		playButton.setOnClickListener(playbackOnClickListener);
		pauseButton.setOnClickListener(playbackOnClickListener);
		downloadButton.setEnabled(StringUtils.isBlank(localUrl));
		downloadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final MediaDownloadCommand cmd = new MediaDownloadCommand(getIntent().getData(), PlayerActivity.this, null);
				new Thread(cmd).start();
				downloadButton.setEnabled(false);
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				int currentPosition = 0;
				while (!musicThreadFinished) {
					try {
						Thread.sleep(1000);
						currentPosition = getCurrentPosition();
					} catch (InterruptedException e) {
						return;
					} catch (Exception e) {
						return;
					}
					final int total = getDuration();

					final String totalTime = getAsTime(total);
					final String curTime = getAsTime(currentPosition);

					musicSeekBar.setMax(total);
					musicSeekBar.setProgress(currentPosition);
					musicSeekBar.setSecondaryProgress(getBufferPercentage());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//							if (isPlaying()) {
							//								if (!playPauseButton.isChecked()) {
							//									playPauseButton.setChecked(true);
							//								}
							//							} else {
							//								if (playPauseButton.isChecked()) {
							//									playPauseButton.setChecked(false);
							//								}
							//							}
							musicDuration.setText(totalTime);
							musicCurLoc.setText(curTime);
						}
					});

				}
			}
		}).start();

		if (StringUtils.isNotBlank(localUrl)) {
			mUrl = localUrl;
		} else {
			mUrl = url;
		}
		MusicService.setSong(mUrl, "Temp Song");

		new Thread(new Runnable() {

			@Override
			public void run() {
				//startService(new Intent("PLAY"));
			}
		}).start();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		ScriptListAdapter adapter = (ScriptListAdapter) getListAdapter();
		menu.setHeaderTitle("Headword");
		final String item = (String) adapter.getItem(info.position);
		Iterable<String> words = RichScriptCommand.extractWord(adapter.getRichScript());
		Iterable<String> filter = listWordsMatchToMenuItem(words, item);
		int i = 1;
		for (String each : RichScriptCommand.excludeBaseWord(PlayerActivity.this, filter)) {
			menu.add(0, i++, 0, each);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void pause() {
		if (MusicService.getInstance() != null) {
			MusicService.getInstance().pauseMusic();
		}
	}

	@Override
	public void seekTo(int pos) {
		if (MusicService.getInstance() != null) {
			MusicService.getInstance().seekMusicTo(pos);
		}

	}

	@Override
	public void start() {
		if (MusicService.getInstance() != null) {
			MusicService.getInstance().startMusic();
		} else {
			startService(new Intent("PLAY"));
		}
	}
}
