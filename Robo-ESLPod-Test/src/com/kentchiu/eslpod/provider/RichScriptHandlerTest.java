package com.kentchiu.eslpod.provider;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Joiner;

public class RichScriptHandlerTest extends TestCase {

	public void testExtractScript() throws Exception {
		RichScriptHandler handler = new RichScriptHandler(null);
		List<String> lines = IOUtils.readLines(this.getClass().getResourceAsStream("/script.htm"));
		List<String> lines2 = handler.extractScript(lines);
		String script = Joiner.on("").join(lines2);
		System.out.println(script);
		assertTrue(script.startsWith("Cherise:  <b>Rise and shine</b>"));
		assertTrue(script.endsWith("after all!"));
	}

}
