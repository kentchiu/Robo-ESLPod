package com.kentchiu.eslpod;

import org.apache.commons.lang.StringUtils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends BaseAdapter {

	private int		layout;
	private Context	context;
	private Cursor	cursor;

	public PodcastListAdapter(Context context, int layout, Cursor cursor) {
		this.context = context;
		this.layout = layout;
		this.cursor = cursor;
	}

	@Override
	public int getCount() {
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		cursor.move(position);
		return cursor;
	}

	@Override
	public long getItemId(int position) {
		cursor.moveToPosition(position);
		return cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		cursor.moveToPosition(position);
		String downloadFrom = cursor.getString(cursor.getColumnIndex(PodcastColumns.MEDIA_URL));
		int downloadTo = cursor.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL);
		int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		Uri uri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
		String title = cursor.getString(cursor.getColumnIndex(PodcastColumns.TITLE));
		String mediaCache = cursor.getString(downloadTo);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(layout, parent, false);

		TextView tv = (TextView) view.findViewById(R.id.podcastTitle);
		Button button = (Button) view.findViewById(R.id.downloadButton);

		view.setTag(downloadFrom);
		tv.setText(title);

		button.setTag(uri);
		if (StringUtils.isNotBlank(mediaCache)) {
			button.setText("Clean");
		} else {
			button.setText("Download");
		}
		return view;
	}

}
