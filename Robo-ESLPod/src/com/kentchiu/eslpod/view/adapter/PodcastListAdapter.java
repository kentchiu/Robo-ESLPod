package com.kentchiu.eslpod.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
		String title = cursor.getString(cursor.getColumnIndex(PodcastColumns.TITLE));
		int status = cursor.getInt(cursor.getColumnIndex(PodcastColumns.MEDIA_DOWNLOAD_STATUS));
		cursor.getLong(cursor.getColumnIndex(PodcastColumns.MEDIA_LENGTH));
		cursor.getLong(cursor.getColumnIndex(PodcastColumns.MEDIA_DOWNLOAD_LENGTH));
		TextView tv = (TextView) view.findViewById(R.id.podcastTitle);
		tv.setText(title);
		ImageView statusImage = (ImageView) view.findViewById(R.id.downloadStatusImage);
		switch (status) {
		case PodcastColumns.MEDIA_STATUS_DOWNLOADED:
			statusImage.setImageResource(R.drawable.presence_online);
			break;
		case PodcastColumns.MEDIA_STATUS_DOWNLOADING:
			statusImage.setImageResource(R.drawable.presence_busy);
			break;
		case PodcastColumns.MEDIA_STATUS_DOWNLOADABLE:
			statusImage.setImageResource(R.drawable.presence_invisible);
			break;
		default:
			statusImage.setImageResource(R.drawable.presence_invisible);
			break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return super.newView(context, cursor, parent);
	}

}
