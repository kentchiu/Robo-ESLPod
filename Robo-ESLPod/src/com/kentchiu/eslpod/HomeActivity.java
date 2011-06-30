package com.kentchiu.eslpod;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kentchiu.eslpod.provider.Podcast;

public class HomeActivity extends ListActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Cursor cursor = managedQuery(Podcast.PODCAST_URI, null, null, null, null);
		setListAdapter(new PodcastListAdapter(this, cursor));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = Uri.withAppendedPath(Podcast.PODCAST_URI, Long.toString(id));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}