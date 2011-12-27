package com.kentchiu.eslpod.view.adapter;

import org.apache.commons.lang3.StringUtils;

import roboguice.util.RoboAsyncTask;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends ResourceCursorAdapter {

	public PodcastListAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		String title = cursor.getString(cursor.getColumnIndex(PodcastColumns.TITLE));
		TextView tv = (TextView) view.findViewById(R.id.podcastTitle);
		tv.setText(title);
		String richScript = cursor.getString(cursor.getColumnIndex(PodcastColumns.RICH_SCRIPT));
		if (StringUtils.isBlank(richScript)) {
			new RoboAsyncTask<Void>(context) {

				@Override
				public Void call() throws Exception {
					Uri uri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
					Intent intent = new Intent();
					intent.setData(uri);
					RichScriptCommand command = new RichScriptCommand(context, intent);
					command.run();
					return null;
				}
			}.execute();
		}

		int mediaStatus = cursor.getInt(cursor.getColumnIndex(PodcastColumns.MEDIA_DOWNLOAD_STATUS));
		updateStatus(mediaStatus, (ImageView) view.findViewById(R.id.mp3StatusImage));
		int dictStatus = cursor.getInt(cursor.getColumnIndex(PodcastColumns.DICTIONARY_DOWNLOAD_STATUS));
		updateStatus(dictStatus, (ImageView) view.findViewById(R.id.dictionaryStatusImage));
		//view.findViewById(R.id.dictionaryStatusImage);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return super.newView(context, cursor, parent);
	}

	private void updateStatus(int status2, ImageView imgView) {
		int status = status2;
		ImageView statusImage = imgView;

		switch (status) {
		case PodcastColumns.STATUS_DOWNLOADED:
			statusImage.setImageResource(R.drawable.presence_online);
			break;
		case PodcastColumns.STATUS_DOWNLOADING:
			statusImage.setImageResource(R.drawable.presence_away);
			break;
		case PodcastColumns.STATUS_DOWNLOADABLE:
			statusImage.setImageResource(R.drawable.presence_invisible);
			break;
		default:
			statusImage.setImageResource(R.drawable.presence_invisible);
			break;
		}
	}

}
