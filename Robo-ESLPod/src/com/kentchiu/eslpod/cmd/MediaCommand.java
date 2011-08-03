package com.kentchiu.eslpod.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaCommand implements Runnable {

	private Context	context;
	private Uri		podacstUri;

	public MediaCommand(Context context, Uri podacstUri) {
		super();
		this.context = context;
		this.podacstUri = podacstUri;
	}

	@Override
	public void run() {
		Cursor c = context.getContentResolver().query(podacstUri, null, null, null, null);
		if (c.moveToFirst()) {
			String localUrlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
			String urlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
			long length = c.getLong(c.getColumnIndex(PodcastColumns.MEDIA_LENGTH));
			if (localUrlStr == null || !new File(localUrlStr).exists()) {
				try {
					File localFile = downloadFrom(urlStr, length);
					updateDatabase(localFile);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected File downloadFrom(String urlStr, long length) throws MalformedURLException, IOException, FileNotFoundException {
		URL url = new URL(urlStr);
		File remoteFile = new File(url.getFile());
		Log.i(EslPodApplication.TAG, "Downloading file from " + urlStr);
		InputStream openStream = url.openStream();
		// TODO move to sdcard
		File localFile = new File(context.getCacheDir(), remoteFile.getName());
		Log.v(EslPodApplication.TAG, "file length : " + localFile.length());
		if (localFile.exists() && localFile.length() == length) {
			Log.i(EslPodApplication.TAG, localFile.toString() + " exists");
		} else {
			localFile.createNewFile();
			IOUtils.copyLarge(openStream, new FileOutputStream(localFile));
			Log.i(EslPodApplication.TAG, "Downloaded file " + localFile.toString() + " completed");
		}
		return localFile;
	}

	protected void updateDatabase(File localFile) {
		ContentValues cv = new ContentValues();
		cv.put(PodcastColumns.MEDIA_URL_LOCAL, localFile.getPath());
		context.getContentResolver().update(podacstUri, cv, null, null);
	}

}
