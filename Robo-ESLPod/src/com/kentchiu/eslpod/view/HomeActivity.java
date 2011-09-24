package com.kentchiu.eslpod.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.RejectedExecutionException;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.LocalBinder;
import com.kentchiu.eslpod.service.MediaDownloadService;
import com.kentchiu.eslpod.service.PodcastFetchService;
import com.kentchiu.eslpod.service.RichScriptFetchService;

public class HomeActivity extends ListActivity {

	private class DownloadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String from = msg.getData().getString("from");
			String to = msg.getData().getString("to");
			if (from == null) {
				Log.w(EslPodApplication.TAG, "Download fail with illegal message " + msg);
				return;
			}
			ContentValues cv = new ContentValues();
			switch (msg.what) {
			case MediaCommand.DOWNLOAD_START:
				cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADING);
				cv.put(PodcastColumns.MEDIA_DOWNLOAD_LENGTH, msg.arg1);
				int count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
				if (count != 1) {
					Log.w(EslPodApplication.TAG, "exception row updated but " + count);
				}
				break;
			case MediaCommand.DOWNLOAD_COMPLETED:
				cv.put(PodcastColumns.MEDIA_URL_LOCAL, to);
				cv.put(PodcastColumns.MEDIA_DOWNLOAD_LENGTH, msg.arg1);
				cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.MEDIA_STATUS_DOWNLOADED);
				count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
				if (count != 1) {
					Log.w(EslPodApplication.TAG, "exception row updated but " + count);
				}
				break;
			}
			PodcastListAdapter adapter = (PodcastListAdapter) getListAdapter();
			adapter.changeCursor(managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC"));
			adapter.notifyDataSetChanged();
		}
	}

	private final class ImportHandler extends Handler {
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

	private static final int		DIALOG_IMPORT	= 0;
	private ServiceConnection		mediaConn;
	private ServiceConnection		podcastConn;
	private PodcastFetchService		podcastFetchService;
	private MediaDownloadService	mediaDownloadService;

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
			Log.e(EslPodApplication.TAG, "Download fail, invalid url , the source uri is " + uri);
			btn.setEnabled(true);
		}

	}

	/** Called when the activity is first created. */
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
		Intent intent = new Intent(this, RichScriptFetchService.class);
		startService(intent);
		bindPodcastFetchService();
		bindMediadownloadService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mediaConn);
		unbindService(podcastConn);
	}

	private void bindMediadownloadService() {
		mediaConn = new ServiceConnection() {

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
		bindService(new Intent(this, MediaDownloadService.class), mediaConn, BIND_AUTO_CREATE);
	}

	private void bindPodcastFetchService() {
		podcastConn = new ServiceConnection() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				podcastFetchService = ((LocalBinder<PodcastFetchService>) service).getService();
				final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
				if (cursor.getCount() < PodcastFetchService.LOCAL_PODCAST_COUNT) {
					// parse exists podcast xml to db
					podcastFetchService.importLocal(new ImportHandler());
				} else {
					try {
						podcastFetchService.fetchNew(null);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		};
		bindService(new Intent(this, PodcastFetchService.class), podcastConn, BIND_AUTO_CREATE);
	}
}
