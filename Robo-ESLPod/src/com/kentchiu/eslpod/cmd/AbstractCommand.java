package com.kentchiu.eslpod.cmd;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public abstract class AbstractCommand implements Runnable {
	public static final int	START	= 99999;
	public static final int	END		= -99999;
	protected Context		context;

	protected Intent		intent;

	protected Handler		handler;

	public AbstractCommand(Context context, Intent intent) {
		this(context, intent, null);
	}

	public AbstractCommand(Context context, Intent intent, Handler handler) {
		super();
		this.context = context;
		this.intent = intent;
		this.handler = handler;
	}

	protected abstract boolean execute();

	@Override
	public void run() {
		sendMessage(START);
		execute();
		sendMessage(END);
	}

	private void sendMessage(int what) {
		if (handler != null) {
			handler.sendEmptyMessage(what);
		}
	};

}
