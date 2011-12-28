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
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;

import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class MediaDownloadCommand extends AbstractCommand {

	private URL		from;
	private File	to;

	public MediaDownloadCommand(Context context, Intent intent, Handler handler) {
		super(context, intent, handler);
		final Cursor c = context.getContentResolver().query(intent.getData(), null, null, null, null);
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

	@Override
	protected boolean execute() {
		try {
			downloadFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
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
		context.getContentResolver().update(intent.getData(), cv, null, null);
		Ln.i("Downloaded file " + to.toString() + " completed");
	}

	private void markAsStart() {
		ContentValues cv = new ContentValues();
		cv.put(PodcastColumns.MEDIA_DOWNLOAD_STATUS, PodcastColumns.STATUS_DOWNLOADING);
		context.getContentResolver().update(intent.getData(), cv, null, null);
	}
}
