package net.rubyeye.ur;

import net.rubyeye.ur.RecordDBHelper.Record;
import net.youmi.android.AdManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RecordEdit extends Activity {
	private EditText recordName;
	private EditText recordDesc;
	private Button updateBtn;
	private Button cancelBtn;
	private Cursor mRecord;
	private RecordDBHelper dbHelper;

	private static final int STATE_EDIT = 1;
	private static final int STATE_INSERT = 2;
	static {
		if (Constants.ENABLE_AD) {
			AdManager.init(Constants.APP_ID, Constants.APP_PASS,
					Constants.APP_INTERVAL,  Constants.TEST_MODE, Constants.APP_VERSION);
		}

	}
	private int state;
	private long id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_edit);
		this.recordName = (EditText) findViewById(R.id.recordName);
		this.recordDesc = (EditText) findViewById(R.id.recordDesc);
		this.updateBtn = (Button) findViewById(R.id.recordUpdate);
		this.cancelBtn = (Button) findViewById(R.id.recordCancel);

		dbHelper = new RecordDBHelper(this);
		dbHelper.openDB(this);
		state = STATE_INSERT;
		id = getIntent().getLongExtra("extra_record_id", -1);
		if (id >= 0) {
			this.mRecord = dbHelper.findRecordById(id);
			startManagingCursor(this.mRecord);
			this.state = STATE_EDIT;
		}
		populateFields();

		this.updateBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (recordName.getText().toString() == null
						|| recordName.getText().toString().length() <= 0) {
					new AlertDialog.Builder(RecordEdit.this).setTitle("发生错误")
							.setMessage("项目名称不能为空")
							.setPositiveButton("Okay", null).show();
				} else {
					switch (state) {
					case STATE_INSERT:
						insertRecord();
						break;
					case STATE_EDIT:
						updateRecord();
						break;
					}
					setResult(RESULT_OK);
					finish();
				}
			}

		});
		this.cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mRecord != null) {
					populateFields();
				} else {
					recordName.setText("");
					recordDesc.setText("");
				}
			}
		});
	}

	private void updateRecord() {
		if (dbHelper.updateRecord(new Record(id, recordName.getText()
				.toString(), recordDesc.getText().toString())) < 0) {
			new AlertDialog.Builder(RecordEdit.this).setTitle("保存失败")
					.setMessage("保存项目失败，项目名称不能重复")
					.setPositiveButton("Okay", null).show();
			return;
		}
		Toast.makeText(RecordEdit.this, "保存项目成功", Toast.LENGTH_SHORT).show();
	}

	private void insertRecord() {
		if (dbHelper.insertRecord(new Record(0,
				recordName.getText().toString(), recordDesc.getText()
						.toString())) < 0) {
			new AlertDialog.Builder(RecordEdit.this).setTitle("保存失败")
					.setMessage("保存项目失败，项目名称不能重复")
					.setPositiveButton("Okay", null).show();
			return;
		}
		Toast.makeText(RecordEdit.this, "保存项目成功", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.dbHelper.closeDB();
	}

	private void populateFields() {
		if (this.mRecord != null) {
			this.mRecord.moveToFirst();
			this.recordName.setText(this.mRecord.getString(this.mRecord
					.getColumnIndex("name")));
			this.recordDesc.setText(this.mRecord.getString(this.mRecord
					.getColumnIndex("desc")));
		}
	}
}
