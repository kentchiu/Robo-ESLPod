package com.kentchiu.eslpod;


class DictionaryDictTask extends AbstractDictTask {

	public DictionaryDictTask(DictFlipActivity activity) {
		super(activity);
	}

	@Override
	protected String getWebContent() {
		return "Dictionary Content";
	}

	@Override
	protected int topViewId() {
		return R.id.dict3;
	}

}