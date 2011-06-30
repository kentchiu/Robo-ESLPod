package com.kentchiu.eslpod.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Joiner;

@RunWith(MyRobolectricTestRunner.class)
public class RichScriptHandlerTest {

	@Test
	public void extractScript() throws Exception {
		RichScriptHandler handler = new RichScriptHandler(null, null);
		List<String> lines = IOUtils.readLines(this.getClass().getResourceAsStream("/script.htm"));
		List<String> lines2 = handler.extractScript(lines);
		String script = Joiner.on("").join(lines2);
		System.out.println(script);
		assertThat(script, startsWith("Cherise:  <b>Rise and shine</b>"));
		assertThat(script, endsWith("after all!"));
	}

}
