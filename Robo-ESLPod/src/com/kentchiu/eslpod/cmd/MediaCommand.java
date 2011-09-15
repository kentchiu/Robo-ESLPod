package com.kentchiu.eslpod.cmd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kentchiu.eslpod.view.EslPodApplication;

public class MediaCommand implements Runnable {

	public static final int	DOWNLOAD_COMPLETED	= 1;
	public static final int	DOWNLOAD_PROCESSING	= 2;
	public static final int	DOWNLOAD_START		= 3;
	private URL				from;
	private File			to;
	private Handler			handler;

	public MediaCommand(URL from, File to) {
		this(from, to, null);
	}

	public MediaCommand(URL from, File to, Handler handler) {
		super();
		this.from = from;
		this.to = to;
		this.handler = handler;
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
			Log.w(EslPodApplication.TAG, "try to sending but handler is null");
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

	private Bundle createBundle() {
		String f = from.toString();
		String t = to.toString();
		Bundle data = new Bundle();
		data.putString("from", f);
		data.putString("to", t);
		return data;
	}

	private void downloadFile() throws IOException, FileNotFoundException {
		Log.i(EslPodApplication.TAG, "Downloading file from " + from.toString());
		sendMessage(DOWNLOAD_START, 0, 0);

		URLConnection conn = from.openConnection();

		conn.connect();
		// this will be useful so that you can show a typical 0-100% progress bar
		long lenghtOfFile = conn.getContentLength();
		Log.v(EslPodApplication.TAG, "file length : " + lenghtOfFile);
		if (to.exists() && to.length() == lenghtOfFile) {
			Log.i(EslPodApplication.TAG, to.getAbsolutePath() + " exists");
			sendMessage(DOWNLOAD_COMPLETED, (int) lenghtOfFile, (int) lenghtOfFile);
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
					sendMessage(DOWNLOAD_PROCESSING, total, lenghtOfFile);
					cache = processing;
				}
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
			sendMessage(DOWNLOAD_COMPLETED, cache, (int) lenghtOfFile);
			Log.i(EslPodApplication.TAG, "Downloaded file " + to.toString() + " completed");
		}
	}

}
