package com.kentchiu.eslpod.cmd;

import java.io.File;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;

public class DownloadTask extends AsyncTask<String, Integer, String> {
	private final View	progressView;
	private Handler		handler;
	private File		downloadTo;

	public DownloadTask(File downloadTo, View progressView) {
		this.downloadTo = downloadTo;
		this.progressView = progressView;
		handler = new Handler(new Callback() {
			@SuppressWarnings("synthetic-access")
			@Override
			public boolean handleMessage(Message msg) {
				publishProgress(msg.what);
				return true;
			}
		});
	}

	public File getDownloadTo() {
		return downloadTo;
	}

	public View getProgressView() {
		return progressView;
	}

	/**
	 * this method called when {@link #doInBackground(String...)} completed, it is the in UIThread, DON'T do any UI operation here.
	 */
	protected void afterDownload() {
	}

	@Override
	protected String doInBackground(String... urls) {
		try {
			new MediaCommand(new URL(urls[0]), downloadTo, handler).run();
			afterDownload();
		} catch (Exception e) {
		}
		return null;
	}
}