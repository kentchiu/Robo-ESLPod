package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import android.test.AndroidTestCase;

public class CustomHttpClientTest extends AndroidTestCase {
	private HttpClient	client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		client = CustomHttpClient.getHttpClient();
	}

	public void testGetChineseConent_page_encoding_is_utf8_and_processing_by_UnicodeResponseHandler() throws Exception {
		HttpGet request = new HttpGet("http://www.yahoo.com.tw");
		String page = client.execute(request, new UnicodeResponseHandler());
		assertThat(page, containsString("搜尋"));
	}

	// TODO page is iso-88-59-1
	// TODO page is big5

	public void testGetChineseContent_is_utf8_and_processing_by_UnicodeResponseHandler() throws Exception {
		HttpGet request = new HttpGet("http://www.google.com.tw");
		String page = client.execute(request, new BasicResponseHandler());
		assertThat(page, containsString("搜尋"));
	}
}
