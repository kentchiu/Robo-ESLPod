package com.kentchiu.eslpod;

class WikiDictTask extends AbstractDictTask {

	public WikiDictTask(DictFlipActivity parent) {
		super(parent);
	}

	@Override
	protected String getWebContent() {
		//String wikiText = SimpleWikiHelper.getPageContent(query, true);
		//parsedText = ExtendedWikiHelper.formatWikiText(wikiText);
		return "Wiki Conent";
	}

	@Override
	protected int topViewId() {
		return R.id.dict1;
	}
}