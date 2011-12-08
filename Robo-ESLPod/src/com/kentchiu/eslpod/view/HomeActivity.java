package com.kentchiu.eslpod.view;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import roboguice.activity.RoboListActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.adapter.PodcastListAdapter;

public class HomeActivity extends RoboListActivity {
	/*
		private class DownloadHandler extends Handler {
			@Override
			public void handleMessage(Message msg) {
				String from = msg.getData().getString("from");
				String to = msg.getData().getString("to");
				if (from == null) {
					Ln.w("Download fail with illegal message %s", msg);
					return;
				}
				ContentValues cv = new ContentValues();
				switch (msg.what) {
				case MediaCommand.DOWNLOAD_START:
					cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADING);
					cv.put(PodcastColumns.MEDIA_DOWNLOAD_LENGTH, msg.arg1);
					int count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
					if (count != 1) {
						Ln.w("exception row updated but " + count);
					}
					break;
				case MediaCommand.DOWNLOAD_COMPLETED:
					cv.put(PodcastColumns.MEDIA_URL_LOCAL, to);
					cv.put(PodcastColumns.MEDIA_DOWNLOAD_LENGTH, msg.arg1);
					cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADED);
					count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
					if (count != 1) {
						Ln.w("exception row updated but %d", count);
					}
					Toast.makeText(HomeActivity.this, new File(to).getName() + " download completed", Toast.LENGTH_SHORT).show();
					break;
				}
				PodcastListAdapter adapter = (PodcastListAdapter) getListAdapter();
				adapter.changeCursor(managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC"));
				adapter.notifyDataSetChanged();
			}
		}
	*/
	private class ImportHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PodcastCommand.START_GET_ITEM_NODES:
				showDialog(DIALOG_IMPORT);
				break;
			case PodcastCommand.END_IMPORT:
				dismissDialog(DIALOG_IMPORT);
				PodcastListAdapter adapter = (PodcastListAdapter) getListAdapter();
				Cursor newCursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
				adapter.changeCursor(newCursor);
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}

	/*
	private class MediaConnection implements ServiceConnection {

		@SuppressWarnings("synthetic-access")
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mediaDownloadService = ((LocalBinder<MediaDownloadService>) service).getService();
			mediaDownloadService.refreshStatus();
			mediaDownloadService.setDownloadHandler(new DownloadHandler());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	private class PodcastConnection implements ServiceConnection {
		@SuppressWarnings("synthetic-access")
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			podcastFetchService = ((LocalBinder<PodcastFetchService>) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	}

	 */
	private static final int	DIALOG_IMPORT		= 0;
	//private MediaConnection			mediaConn		= new MediaConnection();	// TODO refectory to Local Serice
	//private PodcastConnection		podcastConn		= new PodcastConnection();	// TODO refectory to Local Serice
	//private PodcastFetchService		podcastFetchService;
	//private MediaDownloadService	mediaDownloadService;

	/*
	public void downloadClickHandler(View view) {
		Button btn = (Button) view;
		Uri uri = (Uri) btn.getTag();
		btn.setEnabled(false);
		try {
			mediaDownloadService.download(uri);
		} catch (RejectedExecutionException e) {
			Toast.makeText(HomeActivity.this, "Too many tasks, try it later", Toast.LENGTH_SHORT).show();
			btn.setEnabled(true);
		} catch (MalformedURLException e) {
			Toast.makeText(HomeActivity.this, "Download fail, invalid url", Toast.LENGTH_SHORT).show();
			Ln.e("Download fail, invalid url , the source uri is %s", uri);
			btn.setEnabled(true);
		}

	}
	*/

	public static int			LOCAL_PODCAST_COUNT	= 5;

	public void fetchNewEpisode() throws MalformedURLException, IOException {
		InputStream is = new URL(PodcastCommand.RSS_URI).openStream();
		PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is, null);
		Thread t = new Thread(podcastCommand);
		t.start();
	}

	public void importLocal(Handler handler) {
		InputStream is = getResources().openRawResource(R.raw.podcast_680_685);
		PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is, handler);
		Thread t = new Thread(podcastCommand);
		t.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		PodcastListAdapter adapter = new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor, true);
		setListAdapter(adapter);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		return ProgressDialog.show(HomeActivity.this, "Import Podcast", "Loading. Please wait...it's take a minute", true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = Uri.withAppendedPath(PodcastColumns.PODCAST_URI, Long.toString(id));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
		if (cursor.getCount() < LOCAL_PODCAST_COUNT) {
			// parse exists podcast xml to db
			importLocal(new ImportHandler());
		} else {
			try {
				fetchNewEpisode();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//Intent intent = new Intent(this, RichScriptFetchService.class);
		//startService(intent);
		//bindService(new Intent(this, PodcastFetchService.class), podcastConn, BIND_AUTO_CREATE);
		//bindService(new Intent(this, MediaDownloadService.class), mediaConn, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		//unbindService(mediaConn);
		//unbindService(podcastConn);
	}
}
