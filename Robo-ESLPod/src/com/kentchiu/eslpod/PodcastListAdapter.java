package com.kentchiu.eslpod;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends ResourceCursorAdapter {

	public PodcastListAdapter(Context context, int layout, Cursor c) {
		super(context, layout, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int idx = cursor.getColumnIndex(PodcastColumns.TITLE);
		cursor.getString(idx);
		view.findViewById(R.id.podcastTitle);

	}

	//	@Override
	//	public void bindView(View view, Context context, Cursor cursor) {
	////		TextView textView = (TextView) view;
	////		int idx = cursor.getColumnIndex(PodcastColumns.TITLE);
	////		textView.setText(cursor.getString(idx));
	////		System.out.println(view);
	//		TextView tv = (TextView) view.findViewById(R.id.PodcastTitle);
	//		int idx = cursor.getColumnIndex(PodcastColumns.TITLE);
	//		tv.setText(cursor.getString(idx));
	//	}
	//
	//	@Override
	//	public View newView(Context context, Cursor cursor, ViewGroup parent) {
	//		return view;
	//	}

}
