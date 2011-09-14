package com.kentchiu.eslpod.view;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends ResourceCursorAdapter {

	public PodcastListAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		Uri uri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
		String title = cursor.getString(cursor.getColumnIndex(PodcastColumns.TITLE));
		int status = cursor.getInt(cursor.getColumnIndex(PodcastColumns.MEDIA_STATUS));
		TextView tv = (TextView) view.findViewById(R.id.podcastTitle);
		final Button button = (Button) view.findViewById(R.id.downloadButton);
		tv.setText(title);
		button.setTag(uri);
		switch (status) {
		case PodcastColumns.MEDIA_DOWNLOADED:
			button.setEnabled(true);
			button.setText("CLEAN");
			break;
		case PodcastColumns.MEDIA_DOWNLOADING:
			button.setEnabled(false);
			button.setText("DOWNLOADING...");
			break;
		case PodcastColumns.MEDIA_CLEAN:
		default:
			button.setEnabled(true);
			button.setText("DOWNLOAD");
			break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return super.newView(context, cursor, parent);
	}

}
