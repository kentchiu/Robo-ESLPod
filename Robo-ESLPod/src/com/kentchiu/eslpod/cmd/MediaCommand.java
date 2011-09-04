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

import android.os.Handler;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;

public class MediaCommand implements Runnable {

	private URL		from;

	private File	to;

	private Handler	handler;

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
			downloadFile(from, to, handler);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

	private void downloadFile(URL from, File to, Handler h) throws IOException, FileNotFoundException {
		Log.i(EslPodApplication.TAG, "Downloading file from " + from.toString());
		URLConnection conn = from.openConnection();

		conn.connect();
		// this will be useful so that you can show a typical 0-100% progress bar
		int lenghtOfFile = conn.getContentLength();
		Log.v(EslPodApplication.TAG, "file length : " + lenghtOfFile);
		if (to.exists() && to.length() == lenghtOfFile) {
			Log.i(EslPodApplication.TAG, to.getAbsolutePath() + " exists");
		} else {
			// downloading the file
			InputStream input = new BufferedInputStream(from.openStream());
			OutputStream output = new FileOutputStream(to.getAbsolutePath());
			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				total += count;
				if (h != null) {
					// publishing the progress....
					int processing = (int) (total * 100 / lenghtOfFile);
					h.sendEmptyMessage(processing);
				}
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
			// h.sendMessage(null); TODO send download completed message
			Log.i(EslPodApplication.TAG, "Downloaded file " + to.toString() + " completed");
		}
	}

}
