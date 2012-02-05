package com.kentchiu.eslpod.view;

import java.io.IOException;
import java.net.MalformedURLException;

import roboguice.activity.RoboListActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.AbstractCommand;
import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.view.adapter.PodcastListAdapter;

public class HomeActivity extends RoboListActivity {

	private class ImportHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AbstractCommand.START:
				showDialog(DIALOG_IMPORT);
				break;
			case AbstractCommand.END:
				dismissDialog(DIALOG_IMPORT);
				PodcastListAdapter adapter = (PodcastListAdapter) getListAdapter();
				Cursor newCursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
				adapter.changeCursor(newCursor);
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}

	public final static String	ACTION_SCRIPT_FETCHED	= "com.kentchiu.eslpod.intent.action.SCRIPT_FETCH_COMPLETED";
	private static final int	DIALOG_IMPORT			= 0;
	public static int			LOCAL_PODCAST_COUNT		= 15;

	public void fetchNewEpisode(Handler handler) throws MalformedURLException, IOException {
		Intent intent = new Intent();
		intent.putExtra("RSS_URL", PodcastCommand.RSS_URL);
		PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, intent, handler);
		Thread t = new Thread(podcastCommand);
		t.start();
	}

	//	public void importLocal(Handler handler) {
	//		InputStream is = getResources().openRawResource(R.raw.podcast_680_685);
	//		PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is, handler);
	//		Thread t = new Thread(podcastCommand);
	//		t.start();
	//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		PodcastListAdapter adapter = new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor, true);
		setListAdapter(adapter);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
		//startService(new Intent(this, RichScriptFetchService.class));

	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
