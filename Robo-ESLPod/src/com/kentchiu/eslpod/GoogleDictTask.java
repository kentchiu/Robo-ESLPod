package com.kentchiu.eslpod;


class GoogleDictTask extends AbstractDictTask {

	public GoogleDictTask(DictFlipActivity activity) {
		super(activity);
	}

	@Override
	protected String getWebContent() {
		return "Google Content";
	}

	@Override
	protected int topViewId() {
		return R.id.dict2;
	}

}