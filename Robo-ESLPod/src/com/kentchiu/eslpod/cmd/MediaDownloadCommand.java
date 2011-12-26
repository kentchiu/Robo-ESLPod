package com.kentchiu.eslpod.cmd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

import roboguice.util.Ln;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaDownloadCommand implements Runnable {

	public static final int	DOWNLOAD_COMPLETED	= 1;
	public static final int	DOWNLOAD_PROCESSING	= 2;
	public static final int	DOWNLOAD_START		= 3;

	private URL				from;

	private File			to;
	private Handler			handler;
	private Context			context;
	private Uri				uri;

	public MediaDownloadCommand(Uri uri, Context context, Handler handler) {
		this.context = context;
		this.uri = uri;
		this.handler = handler;
		final Cursor c = context.getContentResolver().query(uri, null, null, null, null);
		if (c.moveToFirst()) {
			final String url = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));
			try {
				from = new URL(url);
				String name = StringUtils.substringAfterLast(from.getFile(), "/");
				to = new File(getDownloadFolder(context), name);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	private Bundle createBundle() {
		String f = from.toString();
		String t = to.toString();
		Bundle data = new Bundle();
		data.putString("from", f);
		data.putString("to", t);
		return data;
	}

	private void downloadFile() throws IOException, FileNotFoundException {
		Ln.i("Downloading file from " + from.toString());
		markAsStart();

		URLConnection conn = from.openConnection();
		conn.connect();
		// this will be useful so that you can show a typical 0-100% progress bar
		long lenghtOfFile = conn.getContentLength();
		Ln.v("file length : " + lenghtOfFile);
		if (to.exists() && to.length() == lenghtOfFile) {
			Ln.i(to.getAbsolutePath() + " exists");
			markAsEnd(lenghtOfFile, lenghtOfFile);
		} else {
			// downloading the file
			InputStream input = new BufferedInputStream(from.openStream());
			OutputStream output = new FileOutputStream(to.getAbsolutePath());
			byte data[] = new byte[1024];
			long total = 0;
			int count;
			int cache = -1; // Using cache to reduce sending message
			while ((count = input.read(data)) != -1) {
				total += count;
				int processing = (int) (total * 100 / lenghtOfFile);
				// publishing the progress....
				if (cache != processing) {
					cache = processing;
				}
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
			markAsEnd(lenghtOfFile, cache);
		}
	}

	private File getDownloadFolder(Context context) {
		String state = Environment.getExternalStorageState();
		if (StringUtils.equals(Environment.MEDIA_MOUNTED, state)) {
			return context.getExternalCacheDir();
		} else {
			Ln.w("SD card no found, save to internl storage");
			return context.getCacheDir();
		}
	}

	public URL getFrom() {
		return from;
	}

	public Handler getHandler() {
		return handler;
	}

	public File getTo() {
		return to;
	}

	private void markAsEnd(long lenghtOfFile, long cache) {
		ContentValues cv = new ContentValues();
		cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.STATUS_DOWNLOADED);
		cv.put(PodcastColumns.MEDIA_URL_LOCAL, to.getAbsolutePath());
		context.getContentResolver().update(uri, cv, null, null);
		sendMessage(DOWNLOAD_COMPLETED, cache, (int) lenghtOfFile);
		Ln.i("Downloaded file " + to.toString() + " completed");
	}

	private void markAsStart() {
		sendMessage(DOWNLOAD_START, 0, 0);
		ContentValues cv = new ContentValues();
		cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.STATUS_DOWNLOADING);
		context.getContentResolver().update(uri, cv, null, null);
	}

	@Override
	public void run() {
		try {
			downloadFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(int what, long process, long total) {
		if (handler != null) {
			Message m = handler.obtainMessage(what, (int) process, (int) total);
			m.setData(createBundle());
			m.sendToTarget();
		} else {
			Ln.w("try to sending but handler is null");
		}
	}

	public void setFrom(URL from) {
		this.from = from;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void setTo(File to) {
		this.to = to;
	}

}
