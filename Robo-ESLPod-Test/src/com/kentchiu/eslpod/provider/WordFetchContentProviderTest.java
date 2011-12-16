package com.kentchiu.eslpod.provider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.ProviderTestCase2;

import com.kentchiu.eslpod.provider.WordFetch.WordFetchColumns;

public class WordFetchContentProviderTest extends ProviderTestCase2<WordFetchContentProvider> {

	private SQLiteDatabase				db;
	private WordFetchContentProvider	provider;

	public WordFetchContentProviderTest() {
		super(WordFetchContentProvider.class, WordFetchContentProvider.class.getName());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		provider = getProvider();
		db = provider.getDatabaseHelper().getWritableDatabase();
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('book', 1, 1)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('book', 2, 1)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('book', 3, 1)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('test', 1, 1)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('test', 2, 1)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('test', 3, 1)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('book', 1, 2)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('book', 2, 2)");
		db.execSQL("insert into word_fetch(word, dictionary_id,podcast_id) values('book', 3, 2)");
	}

	public void testInsertWord() throws Exception {
		ContentValues cv = new ContentValues();
		cv.put(WordFetchColumns.WORD, "foo");
		cv.put(WordFetchColumns.DICTIONARY_ID, 1);
		cv.put(WordFetchColumns.PODCAST_ID, 1);
		provider.insert(WordFetchColumns.WORD_FETCH_URI, cv);
		Cursor c = db.rawQuery("select * from word_fetch where word=?", new String[] { "foo" });
		assertThat(c.getCount(), is(1));
	}

}
