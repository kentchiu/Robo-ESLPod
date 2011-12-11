package com.kentchiu.eslpod.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kentchiu.eslpod.service.AutoFetchService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, AutoFetchService.class);
		context.startService(service);
	}

}
