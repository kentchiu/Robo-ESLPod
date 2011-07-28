package com.kentchiu.eslpod.provider;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.common.base.Joiner;
import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;
import com.kentchiu.eslpod.provider.Dictionary.WordBankColumns;

public class DictionaryContentProvider extends ContentProvider {
	private static final int	WORDS			= 1;
	private static final int	WORD			= 2;
	private static final int	DICTIONARIES	= 3;
	private static final int	DICTIONARY		= 4;
	private UriMatcher			uriMatcher;
	private DatabaseHelper		databaseHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case WORDS:
			return WordBankColumns.CONTENT_TYPE_WORDS;
		case WORD:
			return WordBankColumns.CONTENT_TYPE_WORD;
		case DICTIONARIES:
			return DictionaryColumns.CONTENT_TYPE_DICTIONARIES;
		case DICTIONARY:
			return DictionaryColumns.CONTENT_TYPE_DICTIONARY;
		default:
			throw new IllegalArgumentException("Unknow URI : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final long id;
		switch (uriMatcher.match(uri)) {
		case DICTIONARIES:
			id = db.insert(DatabaseHelper.DICTIONARY_TABLE_NAME, null, values);
			break;
		case WORDS:
			id = db.insert(DatabaseHelper.WORD_BANK_TABLE_NAME, null, values);
			final String word = values.getAsString(WordBankColumns.WORD);
			new AsyncTask<String, String, String>() {

				@Override
				protected String doInBackground(String... params) {
					String urlStr = "http://suggestqueries.google.com/complete/search?ds=d&hl=zh-TW&jsonp=window.google.ac.hr&q=" + word;
					//String content = HttpUtils.getContent(this, url + query);
					String content;
					try {
						URL url = new URL(urlStr);
						List<String> lines = IOUtils.readLines(url.openStream(), "BIG5");
						String join = Joiner.on("").join(lines);
						String str1 = StringUtils.substringAfter(join, "window.google.ac.hr(");
						content = StringUtils.substringBeforeLast(str1, ")");
					} catch (Exception e) {
						content = "[\"" + word + "\",[],{\"k\":1}]";
					}
					return content;
				}

				@Override
				protected void onPostExecute(String result) {
					ContentValues vs = new ContentValues();
					vs.put(DictionaryColumns.DICTIONARY_ID, Dictionary.DICTIONARY_GOOGLE_SUGGESTION);
					vs.put(DictionaryColumns.WORD_ID, id);
					vs.put(DictionaryColumns.CONTENT, result);
					db.insert(DatabaseHelper.DICTIONARY_TABLE_NAME, null, vs);
					getContext().getContentResolver().notifyChange(DictionaryColumns.DICTIONARY_URI, null);
				};
			}.execute(word);

			new AsyncTask<String, String, String>() {

				@Override
				protected String doInBackground(String... params) {
					try {
						String urlStr = "http://en.wiktionary.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&rvexpandtemplates=true&titles=" + word;
						URL url = new URL(urlStr);
						InputStream is = url.openStream();
						List<String> lines = IOUtils.readLines(is);
						String join = Joiner.on("").join(lines);
						return extractContent(join);
					} catch (Exception e) {
						return "get content fail";
					}
				}

				@Override
				protected void onPostExecute(String result) {
					ContentValues vs = new ContentValues();
					vs.put(DictionaryColumns.DICTIONARY_ID, Dictionary.DICTIONARY_WIKI_DICTIONARY);
					vs.put(DictionaryColumns.WORD_ID, id);
					vs.put(DictionaryColumns.CONTENT, result);
					db.insert(DatabaseHelper.DICTIONARY_TABLE_NAME, null, vs);
					getContext().getContentResolver().notifyChange(DictionaryColumns.DICTIONARY_URI, null);
				}

				private String extractContent(String content) {
					try {
						// Drill into the JSON response to find the content body
						JSONObject response = new JSONObject(content);
						JSONObject query = response.getJSONObject("query");
						JSONObject pages = query.getJSONObject("pages");
						JSONObject page = pages.getJSONObject((String) pages.keys().next());
						JSONArray revisions = page.getJSONArray("revisions");
						JSONObject revision = revisions.getJSONObject(0);
						return revision.getString("*");
					} catch (JSONException e) {
						return "";
					}
				};
			}.execute(word);

			break;
		default:
			throw new IllegalArgumentException("url  not matched");
		}
		return ContentUris.withAppendedId(uri, id);

	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null);
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(Dictionary.AUTHORITY, "word", WORDS);
		uriMatcher.addURI(Dictionary.AUTHORITY, "word" + "/#", WORD);
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict", DICTIONARIES);
		uriMatcher.addURI(Dictionary.AUTHORITY, "dict" + "/#", DICTIONARY);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case WORDS:
			Cursor result = db.query(DatabaseHelper.WORD_BANK_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			if (result.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(WordBankColumns.WORD, selectionArgs[0]);
				insert(uri, values);
			}
			return result;
		case DICTIONARIES:
			Cursor query2 = db.query(DatabaseHelper.DICTIONARY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			return query2;
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
