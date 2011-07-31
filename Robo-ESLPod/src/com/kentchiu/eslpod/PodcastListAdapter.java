package com.kentchiu.eslpod;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends ResourceCursorAdapter {

	public PodcastListAdapter(Context context, int layout, Cursor c) {
		super(context, layout, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int idx = cursor.getColumnIndex(PodcastColumns.TITLE);
		TextView tv = (TextView) view.findViewById(R.id.podcastTitle);
		tv.setText(cursor.getString(idx));
		Button button = (Button) view.findViewById(R.id.downloadButton);
		int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		button.setTag(id);
		String mediaCache = cursor.getString(cursor.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
		if (StringUtils.isNotBlank(mediaCache)) {
			button.setText("Clean");
		} else {
			button.setText("Download");
		}
	}

}
