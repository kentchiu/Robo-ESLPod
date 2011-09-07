package com.kentchiu.eslpod;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.kentchiu.eslpod.cmd.MediaCommand;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.LocalBinder;
import com.kentchiu.eslpod.service.MediaDownloadService;
import com.kentchiu.eslpod.service.RichScriptFetchService;

public class HomeActivity extends ListActivity {

	private static final int		DIALOG_INIT_LIST	= 0;
	private MediaDownloadService	downloadService;
	private ServiceConnection		connection;

	public Handler createDownloadHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String from = msg.getData().getString("from");
				String to = msg.getData().getString("to");
				if (from == null) {
					Log.w(EslPodApplication.TAG, "Download fail with illegal message " + msg);
					return;
				}
				Button button = ((PodcastListAdapter) getListAdapter()).findButtonByDownloadUrl(from);
				if (button == null) {
					Log.w(EslPodApplication.TAG, "Not Button associates to download url " + from);
					return;
				}
				switch (msg.what) {
				case MediaCommand.DOWNLOAD_START:
					button.setText("Downloading");
					break;
				case MediaCommand.DOWNLOAD_PROCESSING:
					button.setText(msg.arg1 + "/100");
					break;
				case MediaCommand.DOWNLOAD_COMPLETED:
					button.setEnabled(true);
					button.setText("Clean");
					ContentValues cv = new ContentValues();
					cv.put(PodcastColumns.MEDIA_URL_LOCAL, to);
					int count = getContentResolver().update(PodcastColumns.PODCAST_URI, cv, PodcastColumns.MEDIA_URL + "=?", new String[] { from });
					if (count != 1) {
						Log.w(EslPodApplication.TAG, "exception row updated but " + count);
					}
					break;
				default:
					Log.w(EslPodApplication.TAG, "Unknow message " + msg);
				}

			}
		};
	}

	public void downloadClickHandler(View view) {
		Button btn = (Button) view;
		Uri uri = (Uri) btn.getTag();
		btn.setEnabled(false);
		btn.setText("Wating");
		downloadService.download(uri);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		PodcastListAdapter adapter = new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor);
		setListAdapter(adapter);
		try {
			InputStream is = new URL(PodcastCommand.RSS_URI).openStream();
			PodcastCommand command = new PodcastCommand(this, is);
			new Thread(command).start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(this, RichScriptFetchService.class);
		startService(intent);
		connection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				downloadService = ((LocalBinder<MediaDownloadService>) service).getService();
				downloadService.setDownloadHandler(createDownloadHandler());
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		};
		bindService(new Intent(this, MediaDownloadService.class), connection, BIND_AUTO_CREATE);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		return ProgressDialog.show(HomeActivity.this, "", "Loading. Please wait...", true);
	}

	@Override
	protected void onDestroy() {
		unbindService(connection);
		super.onDestroy();
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
		Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
		if (cursor.getCount() == 0) {
			// parse exists podcast xml to db
			importPodcasts();
		}
	}

	private void importPodcasts() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				InputStream is = getResources().openRawResource(R.raw.podcast);
				PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is);
				// doInBackground is already in work thread, no need to using a new one
				podcastCommand.run();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				dismissDialog(DIALOG_INIT_LIST);
				final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
				setListAdapter(new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor));
				super.onPostExecute(result);
			}

			@Override
			protected void onPreExecute() {
				showDialog(DIALOG_INIT_LIST);
			}
		}.execute();
	}
}
