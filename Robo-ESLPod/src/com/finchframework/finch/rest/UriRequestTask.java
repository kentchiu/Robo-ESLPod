package com.finchframework.finch.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kentchiu.eslpod.EslPodApplication;

/**
 * Provides a runnable that uses an HttpClient to asynchronously load a given
 * URI.  After the network content is loaded, the task delegates handling of the
 * request to a ResponseHandler specialized to handle the given content.
 */
public class UriRequestTask implements Runnable {
	private HttpUriRequest			mRequest;
	private ResponseHandler			mHandler;

	protected Context				mAppContext;

	private RESTfulContentProvider	mSiteProvider;
	private String					mRequestTag;

	private int						mRawResponse	= -1;

	//    private int mRawResponse = R.raw.map_src;

	public UriRequestTask(HttpUriRequest request, ResponseHandler handler, Context appContext) {
		this(null, null, request, handler, appContext);
	}

	public UriRequestTask(String requestTag, RESTfulContentProvider siteProvider, HttpUriRequest request, ResponseHandler handler, Context appContext) {
		mRequestTag = requestTag;
		mSiteProvider = siteProvider;
		mRequest = request;
		mHandler = handler;
		mAppContext = appContext;
	}

	public Uri getUri() {
		return Uri.parse(mRequest.getURI().toString());
	}

	/**
	 * Carries out the request on the complete URI as indicated by the protocol,
	 * host, and port contained in the configuration, and the URI supplied to
	 * the constructor.
	 */
	@Override
	public void run() {
		HttpResponse response;

		try {
			response = execute(mRequest);
			mHandler.handleResponse(response, getUri());
		} catch (IOException e) {
			Log.w(EslPodApplication.LOG_TAG, "exception processing asynch request", e);
		} finally {
			if (mSiteProvider != null) {
				mSiteProvider.requestComplete(mRequestTag);
			}
		}
	}

	public void setRawResponse(int rawResponse) {
		mRawResponse = rawResponse;
	}

	private HttpResponse execute(HttpUriRequest mRequest) throws IOException {
		return null;
	}
}
