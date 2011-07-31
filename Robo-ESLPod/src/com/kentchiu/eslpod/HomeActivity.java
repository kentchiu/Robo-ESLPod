package com.kentchiu.eslpod;

import java.io.InputStream;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.PodcastService;

public class HomeActivity extends ListActivity {
	private static final int	DIALOG_INIT_LIST	= 0;

	public void downloadClickHandler(View view) {
		Integer id = (Integer) view.getTag();
		Uri podcastUri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
		Intent intent = new Intent(HomeActivity.this, PodcastService.class);
		intent.putExtra(PodcastService.COMMAND, PodcastService.COMMAND_RICH_SCRIPT);
		intent.setData(podcastUri);
		startService(intent);
		intent.putExtra(PodcastService.COMMAND, PodcastService.COMMAND_DOWNLOAD_MEDIA);
		startService(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		PodcastListAdapter adapter = new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor);
		setListAdapter(adapter);
		Intent intent = new Intent(this, PodcastService.class);
		intent.putExtra(PodcastService.COMMAND, PodcastService.COMMAND_FETCH_NEW_PODCAST);
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
			showDialog(DIALOG_INIT_LIST);
			// parse exists podcast xml to db
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					InputStream is = getResources().openRawResource(R.raw.podcast);
					PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is);
					// doInBackground is already in work thread, no need to using a new one
					podcastCommand.run();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					dismissDialog(DIALOG_INIT_LIST);
					final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
					setListAdapter(new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor));
					super.onPostExecute(result);
				}
			}.execute();
		}
	}
}