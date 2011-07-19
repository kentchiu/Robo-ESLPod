package com.kentchiu.eslpod;

import com.kentchiu.eslpod.SimpleWikiHelper.ApiException;
import com.kentchiu.eslpod.SimpleWikiHelper.ParseException;

class WikiDictTask extends AbstractDictTask {

	public WikiDictTask(DictFlipActivity parent) {
		super(parent);
	}

	@Override
	protected String getWebContent() {
		String parsedText;
		try {
			String wikiText = SimpleWikiHelper.getPageContent(getQuery(), true);
			parsedText = ExtendedWikiHelper.formatWikiText(wikiText);
			return parsedText;
		} catch (ApiException e) {
			return "get content fail";
		} catch (ParseException e) {
			return "get content fail";
		}
	}

	@Override
	protected int topViewId() {
		return R.id.dict2;
	}
}