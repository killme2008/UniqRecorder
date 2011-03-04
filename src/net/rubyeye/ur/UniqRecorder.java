package net.rubyeye.ur;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class UniqRecorder extends Activity {
	private GridView gridview;
	private List<GridInfo> list;
	private GridAdapter adapter;
	private RecordDBHelper recordDBHelper;

	private static final int NEW_ITEM = 0;
	private static final int NEW_RECORD = 1;
	private static final int RECORD_LIST = 2;
	private static final int RECORD_CHART = 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.recordDBHelper = new RecordDBHelper(this);
		this.recordDBHelper.openDB(this);

		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				Intent intent = null;
				switch (pos) {
				case NEW_ITEM:
					intent = new Intent(UniqRecorder.this, ItemEdit.class);
					startActivity(intent);
					break;
				case NEW_RECORD:
					intent = new Intent(UniqRecorder.this, RecordEdit.class);
					startActivityForResult(intent, NEW_RECORD);
					break;
				case RECORD_LIST:
					intent = new Intent(UniqRecorder.this, RecordList.class);
					startActivity(intent);
					break;
				case RECORD_CHART:
					intent = new Intent(UniqRecorder.this, ChartSelector.class);
					startActivity(intent);
					break;
				}

			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NEW_RECORD) {
			loadMenus();
		}
	}

	private void loadMenus() {
		Cursor cursor = recordDBHelper.findAllRecords();
		startManagingCursor(cursor);
		int recordCount = cursor.getCount();
		list = new ArrayList<GridInfo>();
		list.add(new GridInfo(getResources().getString(
				R.string.menu_insert_item), R.drawable.new_item));
		list.add(new GridInfo(getResources().getString(
				R.string.menu_insert_record), R.drawable.new_record));
		list.add(new GridInfo(getResources().getString(
				R.string.menu_list_record)
				+ "(" + recordCount + ")", R.drawable.records));
		list.add(new GridInfo(getResources().getString(
				R.string.menu_record_chart), R.drawable.chart));
		adapter = new GridAdapter(this);
		adapter.setList(list);
		gridview.setAdapter(adapter);
	}

	static final int ABOUT = Menu.FIRST;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ABOUT, 0, "关于").setIcon(R.drawable.about_36);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ABOUT:
			Intent intent = new Intent(this, About.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadMenus();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recordDBHelper.closeDB();
	}

}