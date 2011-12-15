package com.kentchiu.eslpod.receiver;

import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class DefaultBroadcastReceiverTest extends AndroidTestCase {
	public void testReceive() {
		MyReceiver receiver = new MyReceiver();
		receiver.setDebugUnregister(true);
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.kentchiu.eslpod.NEW");
		getContext().registerReceiver(receiver, filter );
		Uri uri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, 1);
		Intent intent = new Intent();
		intent.setAction("com.kentchiu.eslpod.NEW");
		intent.setData(uri);
		getContext().sendBroadcast(intent);
		//receiver.onReceive(getContext(), intent);
	}

}
