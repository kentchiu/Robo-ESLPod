package com.kentchiu.eslpod.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaDownloadService extends IntentService {

	class MediaDownloadTask extends AsyncTask<Uri, Void, File> {
		@Override
		protected File doInBackground(Uri... params) {
			Uri uri = params[0];
			Cursor c = getContentResolver().query(uri, null, null, null, null);
			if (c.moveToFirst()) {
				//String urlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
				String localUrlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL_LOCAL));
				String urlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
				if (localUrlStr == null || !new File(localUrlStr).exists()) {
					try {
						URL url = new URL(urlStr);
						File remoteFile = new File(url.getFile());
						Log.i(EslPodApplication.LOG_TAG, "Downloading file from " + urlStr);
						InputStream openStream = url.openStream();
						File localFile = new File(getFilesDir(), remoteFile.getName());
						localFile.createNewFile();
						IOUtils.copyLarge(openStream, new FileOutputStream(localFile));
						Log.i(EslPodApplication.LOG_TAG, "Downloaded file " + localFile.toString() + " completed");
						ContentValues cv = new ContentValues();
						cv.put(PodcastColumns.MEDIA_URL_LOCAL, localFile.getPath());
						getContentResolver().update(uri, cv, null, null);
						return localFile;
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(File result) {
		}
	}

	public MediaDownloadService() {
		super(DictionaryService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final Uri podcastUri = intent.getData();
		new MediaDownloadTask().execute(podcastUri);
	}

}
