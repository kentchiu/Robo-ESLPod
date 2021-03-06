package com.kentchiu.eslpod.service;

import java.io.IOException;

import roboguice.service.RoboService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.google.inject.Inject;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.PlayerActivity;

public class MusicService extends RoboService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
	
	public class LocalBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	// indicates the state our service:
	enum State {
		Retrieving, // the MediaRetriever is retrieving music
		Stopped, // media player is stopped and not prepared to play
		Preparing, // media player is preparing...
		Playing, // playback active (media player ready!). (but the media player may actually be
					// paused in this state if we don't have audio focus. But we stay in this state
					// so that we know we have to resume playback once we get focus back)
		Paused
		// playback paused (media player ready!)
	}

	public static final String	ACTION_PLAY	= "PLAY";

	public static MusicService getInstance() {
		return mInstance;
	}

	NotificationManager			mNotificationManager;
	Notification				mNotification	= null;
	// The ID we use for the notification (the onscreen alert that appears at the notification
	// area at the top of the screen as an icon -- and as text as well if the user expands the
	// notification area).
	final int					NOTIFICATION_ID	= 1;
	private static MusicService	mInstance		= null;
	MediaPlayer					mMediaPlayer	= null;					;
	private final IBinder		mBinder			= new LocalBinder();
	State						mState			= State.Retrieving;
	private int					mBufferPosition;
	//private static String		mSongTitle;
	private Intent				intent;

	public int getBufferPercentage() {
		// if (mState.equals(State.Preparing)) {
		return mBufferPosition;
		// }
		// return getMusicDuration();
	};

	//private static String		mSongPicUrl;

	public int getCurrentPosition() {
		if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public Intent getIntent() {
		return intent;
	}

	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}

	public int getMusicDuration() {
		if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
			return mMediaPlayer.getDuration();
		}
		return 0;
	}

	private void initMediaPlayer() {
		Uri uri = getIntent().getData();
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		if (!c.moveToFirst()) {
			return;
		}
		int id = c.getInt(c.getColumnIndex(PodcastColumns._ID));
		String  title = c.getString(c.getColumnIndex(PodcastColumns.TITLE));
		String  linke = c.getString(c.getColumnIndex(PodcastColumns.LINK));
		try {
			mMediaPlayer.setDataSource(intent.getExtras().getString(PodcastColumns.MEDIA_URL));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Workaround for bug: http://code.google.com/p/android/issues/detail?id=957
			mMediaPlayer.reset();
			try {
				mMediaPlayer.setDataSource(PodcastColumns.MEDIA_URL);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			mMediaPlayer.prepareAsync(); // prepare async to not block main thread
		} catch (IllegalStateException e) {
			// TODO Workaround for bug: http://code.google.com/p/android/issues/detail?id=957
			mMediaPlayer.reset();
			try {
				mMediaPlayer.setDataSource(PodcastColumns.MEDIA_URL);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mMediaPlayer.prepareAsync();
		}
		mState = State.Preparing;
		setUpAsForeground(intent.getExtras().getString(PodcastColumns.TITLE) + " (loading)");
	}

	public boolean isPlaying() {
		if (mState.equals(State.Playing)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		setBufferPosition(percent * getMusicDuration() / 100);
	}

	@Override
	public void onCreate() {
		mInstance = this;
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	}

	@Override
	public void onDestroy() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
		mState = State.Retrieving;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}

	/** Called when MediaPlayer is ready */
	@Override
	public void onPrepared(MediaPlayer player) {
		mState = State.Playing;
		mMediaPlayer.start();
		setUpAsForeground(intent.getExtras().getString(PodcastColumns.TITLE) + " (playing)");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.intent = intent;
		if (intent.getAction().equals(ACTION_PLAY)) {
			mMediaPlayer = new MediaPlayer(); // initialize it here
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			initMediaPlayer();
		}
		return START_STICKY;
	}

	public void pauseMusic() {
		if (mState.equals(State.Playing)) {
			mMediaPlayer.pause();
			mState = State.Paused;
			updateNotification(intent.getExtras().getString(PodcastColumns.TITLE) + " (paused)");
		}
	}

	public void restartMusic() {
		mState = State.Retrieving;
		mMediaPlayer.reset();
		initMediaPlayer();
	}

	public void seekMusicTo(int pos) {
		if (mState.equals(State.Playing) || mState.equals(State.Paused)) {
			mMediaPlayer.seekTo(pos);
		}

	}

	protected void setBufferPosition(int progress) {
		mBufferPosition = progress;
	}

	/**
	 * Configures service as a foreground service. A foreground service is a service that's doing something the user is
	 * actively aware of (such as playing music), and must appear to the user as a notification. That's why we create
	 * the notification here.
	 */
	void setUpAsForeground(String text) {
		Intent intent2 = new Intent(getApplicationContext(), PlayerActivity.class);
		intent2.setData(getIntent().getData());
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification = new Notification();
		mNotification.tickerText = text;
		mNotification.icon = R.drawable.ic_launcher;
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), text, pi);
		startForeground(NOTIFICATION_ID, mNotification);
	}

	public void startMusic() {
		if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
			mMediaPlayer.start();
			mState = State.Playing;
			updateNotification(intent.getExtras().getString(PodcastColumns.TITLE) + " (playing)");
		}
	}

	/** Updates the notification. */
	void updateNotification(String text) {
		Intent intent2 = new Intent(getApplicationContext(), PlayerActivity.class);
		intent2.setData(intent.getData());
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), text, pi);
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	}
}
