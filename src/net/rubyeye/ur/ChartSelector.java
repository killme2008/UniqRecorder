package net.rubyeye.ur;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.rubyeye.ur.RecordDBHelper.Record;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

public class ChartSelector extends Activity {
	private Button sureBtn;
	private DatePicker startDate;
	private DatePicker endDate;
	private Spinner spinRecords;
	private RecordDBHelper dbHelper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart_selector);
		sureBtn = (Button) findViewById(R.id.sureBtn);
		startDate = (DatePicker) findViewById(R.id.startDate);
		endDate = (DatePicker) findViewById(R.id.endDate);
		this.spinRecords = (Spinner) findViewById(R.id.spinRecords);
		Calendar calendar = Calendar.getInstance();
		endDate.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), null);
		// 开始日期设置为前一个月同一天
		calendar.add(Calendar.MONTH, -1);
		startDate.init(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), null);

		long recordId = getIntent().getLongExtra("extra_record_id", -1);

		dbHelper = new RecordDBHelper(this);
		dbHelper.openDB(this);

		List<Record> records = new ArrayList<Record>();
		Cursor cursor = this.dbHelper.findAllRecords();
		startManagingCursor(cursor);
		int pos = 0; // 当前项目所在位置
		for (int i = 0; i < cursor.getCount(); i++) {
			long id = cursor.getLong(cursor.getColumnIndex("_id"));
			if (id == recordId) {
				pos = i;
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
		// 初始化项目
		this.spinRecords.setSelection(pos, true);

		sureBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Record record = (Record) spinRecords.getSelectedItem();
				if (record == null || record.id < 0) {
					new AlertDialog.Builder(ChartSelector.this)
							.setTitle("发生错误").setMessage("没有选择项目")
							.setPositiveButton("okay", null).show();
					return;
				}

				String start = startDate.getYear() + "-"
						+ normalize(startDate.getMonth() + 1) + "-"
						+ normalize(startDate.getDayOfMonth());

				String end = endDate.getYear() + "-"
						+ normalize(endDate.getMonth() + 1) + "-"
						+ normalize(endDate.getDayOfMonth());

				Intent intent = new Intent(ChartSelector.this,
						RecordChart.class);
				intent.putExtra("extra_record_id", record.id);
				intent.putExtra("extra_start", start);
				intent.putExtra("extra_end", end);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.dbHelper.closeDB();
	}

	private String normalize(int dayOrMonth) {
		String s = String.valueOf(dayOrMonth);
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}
}
