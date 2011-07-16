package com.kentchiu.eslpod.service;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;
import android.widget.RemoteViews.ActionException;
import junit.framework.TestCase;

public class DictionServiceTest extends ServiceTestCase<DictionService> {

	public DictionServiceTest() {
		super(DictionService.class);
	}

	public void testQuery() throws Exception {
		Context context = getContext();
		Intent intent = new Intent(context, DictionService.class);
		intent.putExtra(SearchManager.QUERY, "book");
		startService(intent);
		assertNotNull(getService());
		waitForServiceComplete();
		

	}

	private void waitForServiceComplete() {
	}

}
