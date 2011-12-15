package com.kentchiu.eslpod;
import roboguice.util.Ln;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Ln.e(intent);
		Uri data = intent.getData();
		if (data != null) {
			long id = ContentUris.parseId(data);
			Ln.e("id: %d", id );
		}
	}

}
