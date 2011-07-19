package com.kentchiu.eslpod.service;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

public class DictionaryServiceTest extends ServiceTestCase<DictionaryService> {

	public DictionaryServiceTest() {
		super(DictionaryService.class);
	}

	public void testQuery() throws Exception {
		Context context = getContext();
		Intent intent = new Intent(context, DictionaryService.class);
		intent.putExtra(SearchManager.QUERY, "book");
		startService(intent);
		assertNotNull(getService());
		waitForServiceComplete();

	}

	private void waitForServiceComplete() {
	}

}
