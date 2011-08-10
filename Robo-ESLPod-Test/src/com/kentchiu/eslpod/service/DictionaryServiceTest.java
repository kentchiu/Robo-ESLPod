package com.kentchiu.eslpod.service;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.test.ServiceTestCase;

public class DictionaryServiceTest extends ServiceTestCase<DictionaryService> {

	public DictionaryServiceTest() {
		super(DictionaryService.class);
	}

	//	public void testGetBasicForm() throws Exception {
	//		assertThat(DictionaryService.getBasicForm("ran"), is("run"));
	//		assertThat(DictionaryService.getBasicForm("running"), is("run"));
	//		assertThat(DictionaryService.getBasicForm("books"), is("book"));
	//
	//	}

	public void testQuery() throws Exception {
		Context context = getContext();
		Intent intent = new Intent(context, DictionaryService.class);
		intent.putExtra(DictionaryService.COMMAND, DictionaryService.COMMAND_DOWNLOAD_WORD);
		intent.putExtra(SearchManager.QUERY, "booking");
		startService(intent);
		DictionaryService service = getService();
		assertNotNull(service);
		service.onHandleIntent(intent);
	}

}
