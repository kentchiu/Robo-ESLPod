package com.kentchiu.eslpod.receiver;

import org.apache.commons.lang3.StringUtils;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.service.AutoFetchService;
import com.kentchiu.eslpod.service.WordFetchService;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (StringUtils.equals(Intent.ACTION_BOOT_COMPLETED, intent.getAction())) {
			new Intent(context, AutoFetchService.class);
		}
		if (StringUtils.equals(PodcastCommand.ACTION_NEW_PODCAST, intent.getAction())) {
			Intent service = new Intent(context, WordFetchService.class);
			Uri data = intent.getData();
			if (data != null) {
				long id = ContentUris.parseId(data);
				if (id > 0) {
					service.setData(data);
					//context.startService(service);
				}
			}
		}
	}

}
