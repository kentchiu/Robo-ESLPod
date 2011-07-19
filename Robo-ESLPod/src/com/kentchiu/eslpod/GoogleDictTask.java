package com.kentchiu.eslpod;

import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.google.common.base.Joiner;


class GoogleDictTask extends AbstractDictTask {

	public GoogleDictTask(DictFlipActivity activity) {
		super(activity);
	}

	@Override
	protected String getWebContent() {
		return getContent();


	}
	protected String getContent() {
		String query = getQuery();
		Log.d(EslPodApplication.LOG_TAG, "query " + query + " at Google Suggestion");
		String urlStr = "http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=" + query;
		//String content = HttpUtils.getContent(this, url + query);
		String content;
		try {
			URL url = new URL(urlStr);
			List<String> lines = IOUtils.readLines(url.openStream(), "BIG5");
			String join = Joiner.on("").join(lines);
			String str1 = StringUtils.substringAfter(join, "window.google.ac.hr(");
			content = StringUtils.substringBeforeLast(str1, ")");
		} catch (Exception e) {
			content = "[\"" + query + "\",[],{\"k\":1}]";

		}

		Log.d(EslPodApplication.LOG_TAG, query + " : " + content);
		return content;
	}

	@Override
	protected int topViewId() {
		return R.id.dict1;
	}

}