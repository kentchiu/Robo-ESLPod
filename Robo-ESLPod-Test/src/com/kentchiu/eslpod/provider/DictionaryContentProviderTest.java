package com.kentchiu.eslpod.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;

import com.kentchiu.eslpod.provider.Dictionary.DictionaryColumns;

public class DictionaryContentProviderTest extends ProviderTestCase2<DictionaryContentProvider> {
	private SQLiteDatabase	db;

	public DictionaryContentProviderTest() {
		super(DictionaryContentProvider.class, DictionaryContentProvider.class.getName());
	}

	public void testDelete_words() throws Exception {
		getProvider().delete(DictionaryColumns.DICTIONARY_URI, null, null);
		Cursor c = db.rawQuery("select * from word_bank", null);
		assertThat(c.getCount(), is(0));
	}

	public void testInsert_dictionary() throws Exception {
		ContentValues values = new ContentValues();
		values.put(DictionaryColumns.DICTIONARY_ID, Dictionary.DICTIONARY_DREYE_DICTIONARY);
		values.put(DictionaryColumns.WORD, "foo");
		values.put(DictionaryColumns.CONTENT, "foobar");
		getProvider().insert(DictionaryColumns.DICTIONARY_URI, values);
		Cursor c = db.rawQuery("select * from dictionary where content='foobar' ", null);
		assertThat(c.getCount(), is(1));
		c.moveToFirst();
		assertThat(c.getString(c.getColumnIndex(DictionaryColumns.CONTENT)), is("foobar"));
	}

	public void testQuery_word() throws Exception {
		Cursor c = getProvider().query(DictionaryColumns.DICTIONARY_URI, null, "word='book' and dictionary_id=1", null, null);
		c.moveToFirst();
		assertThat(c.getCount(), is(1));
		assertThat(c.getInt(c.getColumnIndex(BaseColumns._ID)), is(1));
	}

	public void testQuery_words() throws Exception {
		Cursor c = getProvider().query(DictionaryColumns.DICTIONARY_URI, null, "word='book'", null, null);
		c.moveToFirst();
		assertThat(c.getCount(), is(3));
		assertThat(c.getInt(c.getColumnIndex(BaseColumns._ID)), is(3));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		db = getProvider().getDatabaseHelper().getWritableDatabase();
		db.execSQL("insert into word_bank(word) values('book')");
		db.execSQL("insert into word_bank(word) values('books')");
		db.execSQL("insert into word_bank(word) values('booking')");
		db.execSQL("insert into dictionary(dictionary_id, word_id, content) values(1, 1, 'book content')");
		db.execSQL("insert into dictionary(dictionary_id, word_id, content) values(1, 2, 'books content')");
		db.execSQL("insert into dictionary(dictionary_id, word_id, content) values(1, 3, 'booking content')");
	}

}
