package com.kentchiu.eslpod.service;

import java.lang.ref.WeakReference;

import android.os.Binder;

public class LocalBinder<S> extends Binder {
	private WeakReference<S>	service;

	public LocalBinder(S service) {
		this.service = new WeakReference<S>(service);
	}

	public S getService() {
		return service.get();
	}
}