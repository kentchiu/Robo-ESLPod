package com.kentchiu.eslpod;

import com.google.common.collect.ImmutableList;

import android.test.AndroidTestCase;

public class ScriptListAdapterTest extends AndroidTestCase {

	public void testCovenrtFontBoldToRed() throws Exception {
		String line1 = "foo <b>bar</b> baz";
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.listitem, R.id.scriptLine, ImmutableList.of(line1));
		assertEquals("foo <font color='red'>bar</font> baz", adapter.newStyle(line1));
	}
	public void testCovenrtFontBoldToRed_multi_tags() throws Exception {
		String line1 = "<b>foo</b> bar <b>baz</b>";
		ScriptListAdapter adapter = new ScriptListAdapter(getContext(), R.layout.listitem, R.id.scriptLine, ImmutableList.of(line1));
		assertEquals("<font color='red'>foo</font> bar <font color='red'>baz</font>", adapter.newStyle(line1));
	}

}
