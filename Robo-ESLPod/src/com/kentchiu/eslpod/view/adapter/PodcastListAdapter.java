package com.kentchiu.eslpod.view.adapter;

import org.apache.commons.lang3.StringUtils;

import roboguice.util.Ln;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.google.common.base.Splitter;
import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.RichScriptCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastListAdapter extends ResourceCursorAdapter {

	public PodcastListAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String title = cursor.getString(cursor.getColumnIndex(PodcastColumns.TITLE));
		int status = cursor.getInt(cursor.getColumnIndex(PodcastColumns.MEDIA_DOWNLOAD_STATUS));
		TextView tv = (TextView) view.findViewById(R.id.podcastTitle);
		tv.setText(title);
		cursor.getString(cursor.getColumnIndex(PodcastColumns.RICH_SCRIPT));
		ImageView mp3StatusImage = (ImageView) view.findViewById(R.id.mp3StatusImage);
		view.findViewById(R.id.dictionaryStatusImage);
		cursor.getString(cursor.getColumnIndex(PodcastColumns.RICH_SCRIPT));

		updateStatus(status, mp3StatusImage);
	}

	private int getWordCount(Context context, String richScript) {
		Iterable<String> words = RichScriptCommand.extractWord(richScript);
		Iterable<String> headword = RichScriptCommand.headword(context, words);
		int entityCount = 0;
		for (String phase : headword) {
			Iterable<String> ws = Splitter.onPattern("(\n| )").trimResults().split(phase);
			for (String w : ws) {
				String input = StringUtils.trim(w.replaceAll(",", ""));
				Ln.v("word is %s", input);
				entityCount++;
			}
		}
		return entityCount * 3;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return super.newView(context, cursor, parent);
	}

	private void updateStatus(int status, ImageView statusImage) {
		switch (status) {
		case PodcastColumns.STATUS_DOWNLOADED:
			statusImage.setImageResource(R.drawable.presence_online);
			break;
		case PodcastColumns.STATUS_DOWNLOADING:
			statusImage.setImageResource(R.drawable.presence_busy);
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
