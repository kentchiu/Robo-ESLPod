package com.kentchiu.eslpod.cmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URL;

import android.test.AndroidTestCase;

public class MediaCommandTest extends AndroidTestCase {

	public void testDownload() throws Exception {
		URL resource = getClass().getResource("/ESLPod700.mp3");
		File dir = getContext().getCacheDir();
		File file = new File(dir, "ESLPod700.mp3");
		MediaCommand command = new MediaCommand(resource, file);
		command.run();
		assertThat(file.exists(), is(true));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

}
