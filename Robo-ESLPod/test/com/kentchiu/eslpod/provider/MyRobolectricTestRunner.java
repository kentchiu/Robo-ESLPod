package com.kentchiu.eslpod.provider;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowContentUris;
import com.xtremelabs.robolectric.shadows.ShadowUriMatcher;

public class MyRobolectricTestRunner extends RobolectricTestRunner {

	public MyRobolectricTestRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	public MyRobolectricTestRunner(Class<?> testClass, File androidProjectRoot) throws InitializationError {
		super(testClass, androidProjectRoot);
	}

	@Override
	public void beforeTest(Method method) {
		Robolectric.bindShadowClass(ShadowContentUris.class);
		Robolectric.bindShadowClass(ShadowUriMatcher.class);
	}

}
