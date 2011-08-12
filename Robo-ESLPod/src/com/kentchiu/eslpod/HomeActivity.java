package com.kentchiu.eslpod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.kentchiu.eslpod.cmd.PodcastCommand;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;
import com.kentchiu.eslpod.service.PodcastService;

public class HomeActivity extends ListActivity {
	private static final int	DIALOG_INIT_LIST	= 0;

	public void downloadClickHandler(View view) {
		Integer id = (Integer) view.getTag();
		final Uri podcastUri = ContentUris.withAppendedId(PodcastColumns.PODCAST_URI, id);
		final Button button = (Button) view;

		Cursor c = HomeActivity.this.getContentResolver().query(podcastUri, null, null, null, null);
		if (c.moveToFirst()) {
			String urlStr = c.getString(c.getColumnIndex(PodcastColumns.MEDIA_URL));

			new AsyncTask<String, Integer, String>() {
				@Override
				protected String doInBackground(String... urls) {
					int count;
					try {
						URL url = new URL(urls[0]);
						Log.i(EslPodApplication.TAG, "Downloading file from " + url.toString());
						URLConnection conn = url.openConnection();

						conn.connect();
						// this will be useful so that you can show a tipical 0-100% progress bar
						int lenghtOfFile = conn.getContentLength();
						Log.v(EslPodApplication.TAG, "file length : " + lenghtOfFile);
						String name = StringUtils.substringAfterLast(url.getFile(), "/");
						File localFile = new File(HomeActivity.this.getCacheDir(), name);
						if (localFile.exists() && localFile.length() == lenghtOfFile) {
							Log.i(EslPodApplication.TAG, localFile.toString() + " exists");
						} else {
							// downlod the file
							InputStream input = new BufferedInputStream(url.openStream());
							OutputStream output = new FileOutputStream(localFile.getAbsolutePath());
							//OutputStream output = new FileOutputStream("/sdcard/somewhere/nameofthefile.ext");

							byte data[] = new byte[1024];

							long total = 0;

							while ((count = input.read(data)) != -1) {
								total += count;
								// publishing the progress....
								publishProgress((int) (total * 100 / lenghtOfFile));
								output.write(data, 0, count);
							}

							output.flush();
							output.close();
							input.close();
							Log.i(EslPodApplication.TAG, "Downloaded file " + localFile.toString() + " completed");
							updateDatabase(localFile);
							Intent intent = new Intent(HomeActivity.this, PodcastService.class);
							intent.putExtra(PodcastService.COMMAND, PodcastService.COMMAND_RICH_SCRIPT);
							intent.setData(podcastUri);
							startService(intent);
						}
					} catch (Exception e) {
					}
					return null;
				};

				@Override
				protected void onPostExecute(String result) {
					button.setText("Clean");
					button.setEnabled(true);
				};

				@Override
				protected void onPreExecute() {
					button.setText("Download...");
					button.setEnabled(false);
				}

				@Override
				protected void onProgressUpdate(Integer... values) {
					button.setText(Integer.toString(values[0]) + "/100");
				}

				private void updateDatabase(File localFile) {
					ContentValues cv = new ContentValues();
					cv.put(PodcastColumns.MEDIA_URL_LOCAL, localFile.getPath());
					HomeActivity.this.getContentResolver().update(podcastUri, cv, null, null);
				};
			}.execute(urlStr);

		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, PodcastColumns.TITLE + " DESC");
		PodcastListAdapter adapter = new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor);
		setListAdapter(adapter);
		Intent intent = new Intent(this, PodcastService.class);
		intent.setAction(PodcastService.FETCH_NEW_PODCAST);
		//intent.putExtra(PodcastService.COMMAND, PodcastService.COMMAND_FETCH_NEW_PODCAST);
		startService(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		return ProgressDialog.show(HomeActivity.this, "", "Loading. Please wait...", true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = Uri.withAppendedPath(PodcastColumns.PODCAST_URI, Long.toString(id));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
		if (cursor.getCount() == 0) {
			showDialog(DIALOG_INIT_LIST);
			// parse exists podcast xml to db
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					InputStream is = getResources().openRawResource(R.raw.podcast);
					PodcastCommand podcastCommand = new PodcastCommand(HomeActivity.this, is);
					// doInBackground is already in work thread, no need to using a new one
					podcastCommand.run();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					dismissDialog(DIALOG_INIT_LIST);
					final Cursor cursor = managedQuery(PodcastColumns.PODCAST_URI, null, null, null, null);
					setListAdapter(new PodcastListAdapter(HomeActivity.this, R.layout.episode_list_item, cursor));
					super.onPostExecute(result);
				}
			}.execute();
		}
	}
}