package com.kentchiu.eslpod.cmd;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class BatchWordCommand extends AbstractCommand {

	public BatchWordCommand(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected boolean execute() {
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		builder.setPriority(Thread.NORM_PRIORITY);
		ThreadPoolExecutor executorService = new ThreadPoolExecutor(3, // core size
				6, // max size
				10 * 60, // idle timeout
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200), builder.build(), new AbortPolicy()); // queue with a size

		Cursor c = context.getContentResolver().query(intent.getData(), null, null, null, null);
		if (c.moveToFirst()) {
			String richScript = c.getString(c.getColumnIndex(PodcastColumns.RICH_SCRIPT));
			Iterable<String> words = RichScriptCommand.preareForDownload(context, richScript);
			markAsBatchStarting(executorService);

			for (String word : words) {
				List<AbstractDictionaryCommand> cmds = AbstractDictionaryCommand.newDictionaryCommands(context, word);
				for (AbstractDictionaryCommand cmd : cmds) {
					executorService.execute(cmd);
				}
			}
			markAsBatchEnding(executorService);
			return true;
		} else {
			return false;
		}
	}

	private void markAsBatchEnding(ThreadPoolExecutor executorService) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				ContentValues cv = new ContentValues();
				cv.put(PodcastColumns.DICTIONARY_DOWNLOAD_STATUS, PodcastColumns.STATUS_DOWNLOADED);
				context.getContentResolver().update(intent.getData(), cv, null, null);
			}
		});
	}

	private void markAsBatchStarting(ThreadPoolExecutor executorService) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				ContentValues cv = new ContentValues();
				cv.put(PodcastColumns.DICTIONARY_DOWNLOAD_STATUS, PodcastColumns.STATUS_DOWNLOADING);
				context.getContentResolver().update(intent.getData(), cv, null, null);
			}
		});
	}
}
