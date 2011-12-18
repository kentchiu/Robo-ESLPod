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
import com.kentchiu.eslpod.service.AutoFetchService;
import com.kentchiu.eslpod.service.RichScriptFetchService;
import com.kentchiu.eslpod.view.adapter.PodcastListAdapter;

public class HomeActivity extends RoboListActivity {

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

	private static final int	DIALOG_IMPORT		= 0;
	public static int			LOCAL_PODCAST_COUNT	= 15;

	public void fetchNewEpisode(Handler handler) throws MalformedURLException, IOException {
		InputStream is = new URL(PodcastCommand.RSS_URI).openStream();
		PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is, handler);
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
		//		Intent intent2 = new Intent();
		//	    intent2.setAction(PodcastCommand.ACTION_NEW_PODCAST);
		//	    intent2.setData(ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, 1));
		//	    sendBroadcast(intent2);

		//		Intent intent3 = new Intent();
		//	    intent3.setAction(PodcastCommand.ACTION_NEW_PODCAST);
		//		sendBroadcast(intent3);

		//		if (cursor.getCount() < LOCAL_PODCAST_COUNT) {
		//			// parse exists podcast xml to db
		//			importLocal(new ImportHandler());
		//		} else {
		try {
			Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
			if (cursor.getCount() == 0) {
				fetchNewEpisode(new ImportHandler());
			} else {
				fetchNewEpisode(null);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startService(new Intent(this, RichScriptFetchService.class));
		startService(new Intent(this, AutoFetchService.class));

	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
