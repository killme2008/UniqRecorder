package net.rubyeye.ur;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DB helper
 * 
 * @author dennis
 * 
 */
public class RecordDBHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "records_db";
	public static final String RECORD_TABLE_NAME = "record";
	public static final String ITEM_TABLE_NAME = "item";
	public static final String TRIGGER_NAME = "del_record";

	public static final int DB_VERSION = 1;

	public static final String RECORD_CREATE = "CREATE TABLE "
			+ RECORD_TABLE_NAME
			+ " (_id INTEGER PRIMARY KEY,name TEXT UNIQUE NOT NULL,desc TEXT,created INTEGER);";

	public static final String ITEM_CREATE = "CREATE TABLE "
			+ ITEM_TABLE_NAME
			+ " (_id INTEGER PRIMARY KEY,record_id INTEGER NOT NULL,value REAL NOT NULL,created String NOT NULL);";

	public static final String CREATE_ITEM_INDEX = "CREATE UNIQUE INDEX record_created_index ON "
			+ ITEM_TABLE_NAME + "(record_id,created);";

	public static final String TRIGGER_CREATE = "CREATE TRIGGER "
			+ TRIGGER_NAME + " after DELETE ON " + RECORD_TABLE_NAME
			+ " BEGIN " + "DELETE FROM " + ITEM_TABLE_NAME
			+ " WHERE _id = old._id;" + "END;";

	public RecordDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL(RECORD_CREATE);
			db.execSQL(ITEM_CREATE);
			db.execSQL(CREATE_ITEM_INDEX);
			db.execSQL(TRIGGER_CREATE);
			insertDemoData(db);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(Constants.LOG_TAG, "create table failed", e);
		} finally {
			db.endTransaction();
		}
	}

	private void insertDemoData(SQLiteDatabase db) {
		String demoRecordSQL = "INSERT INTO record(name,desc) values('示例_体重','减肥记录器');";
		db.execSQL(demoRecordSQL);
		String demoItemSQL = "INSERT INTO item(record_id,value,created) values('%d','%.2f','%s');";
		Calendar calendar = Calendar.getInstance();
		Random rand = new Random();
		for (int i = 0; i < 30; i++) {
			calendar.add(Calendar.DATE, -1);
			String created = calendar.get(Calendar.YEAR) + "-"
					+ normalize(calendar.get(Calendar.MONTH) + 1) + "-"
					+ normalize(calendar.get(Calendar.DAY_OF_MONTH));
			String format = String.format(demoItemSQL, 1,
					50 + rand.nextDouble() * 10, created);
			Log.d(Constants.LOG_TAG, format);
			db.execSQL(format);
		}
	}

	private String normalize(int dayOrMonth) {
		String s = String.valueOf(dayOrMonth);
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + RECORD_CREATE);
		db.execSQL("DROP TABLE IF EXISTS " + ITEM_CREATE);
		this.onCreate(db);
	}

	private SQLiteDatabase writeDB;
	private SQLiteDatabase readDB;
	private SQLiteOpenHelper helper;

	public void openDB(Context context) {
		helper = new RecordDBHelper(context);
		this.writeDB = helper.getWritableDatabase();
		this.readDB = helper.getReadableDatabase();
	}

	public void closeDB() {
		if (this.writeDB != null) {
			writeDB.close();
			writeDB = null;
		}
		if (this.readDB != null) {
			readDB.close();
			readDB = null;
		}
		if (helper != null) {
			helper.close();
			helper = null;
		}
	}

	public static class Record {
		public static final String[] COLS = { "_id", "name", "desc" };
		public static final String DEFAULT_ORDER_BY = "_id asc";
		public long id;
		public String name;
		public String desc;

		public Record(long id, String name, String desc) {
			super();
			this.id = id;
			this.name = name;
			this.desc = desc;
		}

		@Override
		public String toString() {
			return this.name;
		}

	}

	public static class Item {
		public static final String[] COLS = { "_id", "record_id", "value",
				"created" };
		public static final String DEFAULT_ORDER_BY = "created asc";
		public long id;
		public long record_id;
		public double value;
		public String created;

		public Item(long id, long record_id, double value, String created) {
			super();
			this.id = id;
			this.record_id = record_id;
			this.value = value;
			this.created = created;
		}

		@Override
		public String toString() {
			return "Item [id=" + id + ", record_id=" + record_id + ", value="
					+ value + ", created=" + created + "]";
		}
	}

	public long insertRecord(Record record) {
		ContentValues values = new ContentValues();
		values.put("name", record.name);
		values.put("desc", record.desc);
		values.put("created", System.currentTimeMillis());
		return this.writeDB.insert(RECORD_TABLE_NAME, null, values);
	}

	public long insertItem(Item item) {
		ContentValues values = new ContentValues();
		values.put("record_id", item.record_id);
		values.put("value", item.value);
		values.put("created", item.created);
		return this.writeDB.insert(ITEM_TABLE_NAME, null, values);
	}

	public int updateRecord(Record record) {
		ContentValues values = new ContentValues();
		values.put("name", record.name);
		values.put("desc", record.desc);
		return this.writeDB.update(RECORD_TABLE_NAME, values, "_id="
				+ record.id, null);
	}

	public int deleteItemByRecordId(long recordId) {
		return this.writeDB.delete(ITEM_TABLE_NAME, "record_id=" + recordId,
				null);
	}

	public int updateItem(Item item) {
		ContentValues values = new ContentValues();
		values.put("created", item.created);
		values.put("value", item.value);
		return this.writeDB.update(ITEM_TABLE_NAME, values, "_id=" + item.id,
				null);
	}

	public void deleteRecord(long id) {
		writeDB.beginTransaction();
		try {
			this.writeDB.delete(RECORD_TABLE_NAME, "_id=" + id, null);
			deleteItemByRecordId(id);
			this.writeDB.setTransactionSuccessful();
		} finally {
			writeDB.endTransaction();
		}
	}

	public int deleteItem(long id) {
		return this.writeDB.delete(ITEM_TABLE_NAME, "_id=" + id, null);
	}

	public Cursor findAllRecords() {
		Cursor cursor = this.readDB.query(RECORD_TABLE_NAME, Record.COLS, null,
				null, null, null, Record.DEFAULT_ORDER_BY);
		cursor.moveToFirst();
		return cursor;
	}

	public Cursor findRecordById(long recordId) {
		Cursor cursor = this.readDB.query(RECORD_TABLE_NAME, Record.COLS,
				"_id=?", new String[] { String.valueOf(recordId) }, null, null,
				Record.DEFAULT_ORDER_BY);
		cursor.moveToFirst();
		return cursor;
	}

	public Cursor findItemById(long itemId) {
		Cursor cursor = this.readDB.query(ITEM_TABLE_NAME, Item.COLS, "_id=?",
				new String[] { String.valueOf(itemId) }, null, null,
				Item.DEFAULT_ORDER_BY);
		cursor.moveToFirst();
		return cursor;
	}

	public Cursor findItemsByRecordId(long recordId) {
		Cursor cursor = this.readDB.query(ITEM_TABLE_NAME, Item.COLS,
				"record_id=?", new String[] { String.valueOf(recordId) }, null,
				null, Item.DEFAULT_ORDER_BY);
		cursor.moveToFirst();
		return cursor;
	}

	public Cursor findItemsBewtten(long recordId, String start, String end) {
		String selection = "record_id=?";
		List<String> args = new ArrayList<String>();
		args.add(String.valueOf(recordId));
		if (start != null) {
			selection += " and created>=?";
			args.add(start);
		}
		if (start != null) {
			selection += " and created<=?";
			args.add(end);
		}

		Cursor cursor = this.readDB.query(ITEM_TABLE_NAME, Item.COLS,
				selection, args.toArray(new String[args.size()]), null, null,
				Item.DEFAULT_ORDER_BY);
		cursor.moveToFirst();
		return cursor;
	}

}
