package com.kentchiu.eslpod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.kentchiu.eslpod.cmd.DownloadTask;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.FetchService;

public class HomeActivity extends ListActivity {

	private static final int	DIALOG_INIT_LIST	= 0;

	public void downloadClickHandler(View view) {
		Integer id = (Integer) view.getTag();
		final Uri podcastUri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);

		Cursor c = HomeActivity.this.getContentResolver().query(podcastUri, null, null, null, null);
		if (c.moveToFirst()) {
			String urlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
			downloadMedia(view, podcastUri, urlStr);
		}

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
		Intent intent = new Intent(this, FetchService.class);
		startService(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		return ProgressDialog.show(HomeActivity.this, "", "Loading. Please wait...", true);
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

	private void downloadMedia(View view, final Uri podcastUri, String urlStr) {
		try {
			URL url = new URL(urlStr);
			String name = StringUtils.substringAfterLast(url.getFile(), "/");
			File localFile = new File(HomeActivity.this.getCacheDir(), name);
			new DownloadTask(localFile, view) {
				@Override
				protected void afterDownload() {
					ContentValues cv = new ContentValues();
					cv.put(PodcastColumns.MEDIA_URL_LOCAL, getDownloadTo().getPath());
					HomeActivity.this.getContentResolver().update(podcastUri, cv, null, null);
				};

				@Override
				protected void onPostExecute(String result) {
					Button button = (Button) getProgressView();
					button.setText("Clean");
					button.setEnabled(true);
				}

				@Override
				protected void onPreExecute() {
					Button button = (Button) getProgressView();
					button.setText("Download...");
					button.setEnabled(false);
				}

				@Override
				protected void onProgressUpdate(Integer... values) {
					Button button = (Button) getProgressView();
					button.setText(Integer.toString(values[0]) + "/100");
				}

			}.execute(urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
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
