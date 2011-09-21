package com.kentchiu.eslpod.cmd;

import com.kentchiu.eslpod.cmd.RichScriptCommand.Trim;

import junit.framework.TestCase;

public class TrimTest extends TestCase {
	private Trim trim = new Trim();
	public void  testRemoveEndDot() {
		assertEquals("foo", trim.apply("foo"));
		assertEquals("foo", trim.apply("foo."));
		assertEquals("foo", trim.apply("foo,"));
		assertEquals("foo", trim.apply(",foo"));
		assertEquals("foo", trim.apply("[foo]"));
		assertEquals("foo", trim.apply("foo.\n"));
	}
}
