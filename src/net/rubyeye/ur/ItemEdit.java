package net.rubyeye.ur;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.rubyeye.ur.RecordDBHelper.Item;
import net.rubyeye.ur.RecordDBHelper.Record;
import net.youmi.android.AdManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ItemEdit extends Activity {
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private EditText itemValue;
	private DatePicker itemCreated;
	private Spinner spinRecords;
	private Button updateBtn;
	private Button cancelBtn;
	private Cursor mItem;
	private long prevRecordId;
	private RecordDBHelper dbHelper;
	private Calendar calendar;
	private int origPos;

	private static final int STATE_EDIT = 1;
	private static final int STATE_INSERT = 2;
	static {
		if (Constants.ENABLE_AD) {
			AdManager.init(Constants.APP_ID, Constants.APP_PASS, Constants.APP_INTERVAL, Constants.TEST_MODE, Constants.APP_VERSION);
		}

	}
	private int state;
	private long itemId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_edit);
		this.itemValue = (EditText) findViewById(R.id.itemValue);
		this.itemCreated = (DatePicker) findViewById(R.id.itemCreated);
		this.updateBtn = (Button) findViewById(R.id.recordUpdate);
		this.cancelBtn = (Button) findViewById(R.id.recordCancel);
		this.spinRecords = (Spinner) findViewById(R.id.spinRecords);

		dbHelper = new RecordDBHelper(this);
		dbHelper.openDB(this);
		calendar = Calendar.getInstance();
		itemCreated.init(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), null);

		state = STATE_INSERT;
		prevRecordId = getIntent().getLongExtra("extra_record_id", -1);
		itemId = getIntent().getLongExtra("extra_item_id", -1);
		if (itemId >= 0) {
			this.mItem = dbHelper.findItemById(itemId);
			startManagingCursor(this.mItem);
			state = STATE_EDIT;
		}

		// 初始化下拉列表
		List<Record> records = new ArrayList<Record>();
		Cursor cursor = this.dbHelper.findAllRecords();
		startManagingCursor(cursor);
		for (int i = 0; i < cursor.getCount(); i++) {
			long id = cursor.getLong(cursor.getColumnIndex("_id"));
			if (id == prevRecordId) {
				origPos = i;
			}
			records.add(new Record(id, cursor.getString(cursor
					.getColumnIndex("name")), null));
			cursor.moveToNext();
		}
		ArrayAdapter<Record> aspnEmployers = new ArrayAdapter<Record>(this,
				android.R.layout.simple_spinner_item, records);
		aspnEmployers
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinRecords.setAdapter(aspnEmployers);

		// 填充字段
		populateFields();

		this.updateBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Editable value = itemValue.getText();
				Record record = (Record) spinRecords.getSelectedItem();
				if (value.toString() == null || value.toString().length() <= 0) {
					new AlertDialog.Builder(ItemEdit.this).setTitle("发生错误")
							.setMessage("记录值不能为空")
							.setPositiveButton("Okay", null).show();
					return;
				}
				if (record.id < 0) {
					new AlertDialog.Builder(ItemEdit.this).setTitle("发生错误")
							.setMessage("请选择记录所在的项目")
							.setPositiveButton("Okay", null).show();
					return;
				}
				String created = itemCreated.getYear() + "-"
						+ normalize(itemCreated.getMonth() + 1) + "-"
						+ normalize(itemCreated.getDayOfMonth());
				switch (state) {
				case STATE_INSERT:
					insertItem(value, created, record.id);
					break;
				case STATE_EDIT:
					UpdateItem(value, created, record.id);
					break;

				}
			}

		});
		this.cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItem != null) {
					populateFields();
				} else {
					itemValue.setText("");
					calendar = Calendar.getInstance();
					itemCreated.init(calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH),
							calendar.get(Calendar.DAY_OF_MONTH), null);
					spinRecords.setSelection(0, true);
				}
			}
		});
	}

	private String normalize(int dayOrMonth) {
		String s = String.valueOf(dayOrMonth);
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	private void insertItem(Editable value, String created, long recordId) {
		try {
			double d = Double.parseDouble(value.toString());
			if (dbHelper.insertItem(new Item(0, recordId, d, created)) < 0) {
				new AlertDialog.Builder(ItemEdit.this).setTitle("保存失败")
						.setMessage("保存记录失败，同一天一个项目只能保存一条记录")
						.setPositiveButton("Okay", null).show();
				return;
			}
			Toast.makeText(ItemEdit.this, "保存记录成功", Toast.LENGTH_SHORT).show();
			finish();
		} catch (NumberFormatException e) {
			new AlertDialog.Builder(ItemEdit.this).setTitle("发生错误")
					.setMessage("记录值必须为数字").setPositiveButton("Okay", null)
					.show();
		}
	}

	private void UpdateItem(Editable value, String created, long recordId) {
		try {
			double d = Double.parseDouble(value.toString());

			if (dbHelper.updateItem(new Item(itemId, recordId, d, created)) < 0) {
				new AlertDialog.Builder(ItemEdit.this).setTitle("保存失败")
						.setMessage("保存记录失败，日期不能重复")
						.setPositiveButton("Okay", null).show();
				return;
			}
			Toast.makeText(ItemEdit.this, "保存记录成功", Toast.LENGTH_SHORT).show();
			finish();
		} catch (NumberFormatException e) {
			new AlertDialog.Builder(ItemEdit.this).setTitle("发生错误")
					.setMessage("记录值必须为数字").setPositiveButton("Okay", null)
					.show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.dbHelper.closeDB();
	}

	private void populateFields() {
		this.spinRecords.setSelection(origPos, true);
		if (this.mItem != null) {
			this.mItem.moveToFirst();
			this.itemValue.setText(this.mItem.getString(this.mItem
					.getColumnIndex("value")));
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					DATE_FORMAT);
			try {
				Date date = simpleDateFormat.parse(this.mItem
						.getString(this.mItem.getColumnIndex("created")));
				calendar.setTime(date);
				itemCreated.updateDate(calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH),
						calendar.get(Calendar.DAY_OF_MONTH));
			} catch (Exception e) {
				Log.e(Constants.LOG_TAG, "Parse date error");
			}
		}
	}
}
