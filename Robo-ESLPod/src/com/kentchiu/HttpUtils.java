package com.kentchiu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.SimpleWikiHelper.ApiException;

public class HttpUtils {

	private static final int	HTTP_STATUS_OK	= 200;

	/**
	 * Pull the raw text content of the given URL. This call blocks until the
	 * operation has completed, and is synchronized because it uses a shared
	 * buffer {@link #sBuffer}.
	 *
	 * @param url The exact URL to request.
	 * @param sUserAgent
	 * @return The raw content returned by the server.
	 * @throws ApiException If any connection or server error occurs.
	 */
	protected static synchronized String getUrlContent(String url, String sUserAgent) throws ApiException {

		if (sUserAgent == null) {
			throw new ApiException("User-Agent string must be prepared");
		}

		// Create client and set our specific user-agent string
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent", sUserAgent);

		try {
			HttpResponse response = client.execute(request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {
				throw new ApiException("Invalid response from server: " + status.toString());
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			return new String(content.toByteArray());
		} catch (IOException e) {
			throw new ApiException("Problem communicating with API", e);
		}
	}

	/**
	 * Prepare the internal User-Agent string for use. This requires a
	 * {@link Context} to pull the package name and version number for this
	 * application.
	 */
	protected static String prepareUserAgent(Context context) {
		try {
			// Read package name and version number from manifest
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return String.format("%1$s/%2$s (Linux; Android)", info.packageName, info.versionName);
		} catch (NameNotFoundException e) {
			Log.e(EslPodApplication.LOG_TAG, "Couldn't find package information in PackageManager", e);
			return null;
		}
	}

	public String getContent(Context context, String url) {
		String agent = prepareUserAgent(context);
		try {
			return getUrlContent(url, agent);
		} catch (ApiException e) {
			Log.e(EslPodApplication.LOG_TAG, "get content from " + url + " fail", e);
			return "";
		}

	}

}
