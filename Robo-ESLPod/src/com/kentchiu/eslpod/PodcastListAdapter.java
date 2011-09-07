package com.kentchiu.eslpod;

import org.apache.commons.lang.StringUtils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.kentchiu.eslpod.cmd.MediaCommand;
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
		String downloadFrom = cursor.getString(cursor.getColumnIndex(PodcastColumns.MEDIA_URL));
		view.setTag(downloadFrom);
		int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		button.setTag(ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id));
		String mediaCache = cursor.getString(cursor.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
		if (StringUtils.isNotBlank(mediaCache)) {
			button.setText("Clean");
		} else {
			button.setText(button.toString());
		}
	}


	public Handler downloadHandler() {

		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String from = msg.getData().getString("from");
				Button button = findButtonByDownloadUrl(from);
				if (button == null) {
					return;
				}
				switch(msg.what) {
				case MediaCommand.DOWNLOAD_START:
					button.setText("Downloading");
					break;
				case MediaCommand.DOWNLOAD_PROCESSING:
					button.setText(msg.arg1 + "/100");
					break;
				case MediaCommand.DOWNLOAD_COMPLETED:
					button.setText("Clean");
					break;
				default :
					Log.w(EslPodApplication.TAG, "Unknow message "  + msg);
				}

			}
		};
	}

	protected Button findButtonByDownloadUrl(String from) {
		return null;
	}



}
