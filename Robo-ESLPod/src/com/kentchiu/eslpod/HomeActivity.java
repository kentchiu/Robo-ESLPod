package com.kentchiu.eslpod;

import java.io.InputStream;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kentchiu.eslpod.provider.Podcast;
import com.kentchiu.eslpod.provider.PodcastHandler;

public class HomeActivity extends ListActivity {
	private static final int	DIALOG_INIT_LIST	= 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Cursor cursor = managedQuery(Podcast.PODCAST_URI, null, null, null, null);
		setListAdapter(new PodcastListAdapter(HomeActivity.this, cursor));
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		ProgressDialog dialog = ProgressDialog.show(HomeActivity.this, "", "Loading. Please wait...", true);
		return dialog;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = Uri.withAppendedPath(Podcast.PODCAST_URI, Long.toString(id));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Cursor cursor = managedQuery(Podcast.PODCAST_URI, null, null, null, null);
		if (cursor.getCount() == 0) {
			showDialog(DIALOG_INIT_LIST);
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					InputStream is = getResources().openRawResource(R.raw.podcast);
					PodcastHandler podcastHandler = new PodcastHandler(getContentResolver(), is);
					podcastHandler.run();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					dismissDialog(DIALOG_INIT_LIST);
					final Cursor cursor = managedQuery(Podcast.PODCAST_URI, null, null, null, null);
					setListAdapter(new PodcastListAdapter(HomeActivity.this, cursor));
					super.onPostExecute(result);
				}
			}.execute(null);
		}

	}
}