package com.kentchiu.eslpod;

import android.test.AndroidTestCase;

import com.google.common.collect.ImmutableList;

public class ScriptListAdapterTest extends AndroidTestCase {

	public void testRichText() throws Exception {
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.script_list_item, R.id.scriptLine, ImmutableList.<String> of());
		adapter.richText("this is a foo bar string", ImmutableList.of("foo", "string"));
	}

}
