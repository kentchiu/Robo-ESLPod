package com.kentchiu.eslpod;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends CursorAdapter {

	public PodcastListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textView = (TextView) view;
		textView.setText(cursor.getString(PodcastColumns.INDEX_OF_TITLE));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		TextView textView = new TextView(context);
		textView.setText(cursor.getString(PodcastColumns.INDEX_OF_TITLE));
		return textView;
	}

}
