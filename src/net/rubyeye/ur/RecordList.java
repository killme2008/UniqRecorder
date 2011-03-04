package net.rubyeye.ur;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RecordList extends ListActivity {
	private RecordDBHelper dbHelper;
	private ProgressDialog progressDialog;
	private Cursor cursor;
	public static final String CLASSTAG = RecordList.class.getSimpleName();
	private ListAdapter listAdapter;

	// Menu item ids
	public static final int MENU_RECORD_EDIT = Menu.FIRST;
	public static final int MENU_RECORD_INSERT = Menu.FIRST + 1;
	public static final int MENU_RECORD_DELETE = Menu.FIRST + 2;
	public static final int MENU_ITEM_LIST = Menu.FIRST + 3;
	public static final int MENU_RECORD_CHART = Menu.FIRST + 4;

	static final int LOAD_RECORDS = 1;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == LOAD_RECORDS) {
				Log.v(Constants.LOG_TAG, " " + RecordList.CLASSTAG
						+ " worker thread done, setup list");
				if (progressDialog != null)
					progressDialog.dismiss();
				progressDialog = null;
				if ((cursor == null) || (cursor.getCount() == 0)) {
					setListAdapter(null);
					return;
				} else {
					listAdapter = new SimpleCursorAdapter(RecordList.this,
							R.layout.record, cursor, new String[] { "name" },
							new int[] { R.id.record_name });
					setListAdapter(listAdapter);
				}
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_list);
		getListView().setOnCreateContextMenuListener(this);
		dbHelper = new RecordDBHelper(this);
		dbHelper.openDB(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_RECORD_INSERT, 0, R.string.menu_insert_record)
				.setIcon(android.R.drawable.ic_menu_add);
		return true;
	}

	static final int ADD_RECORD = 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RECORD_INSERT:
			// Launch activity to insert a new record
			startActivityForResult(new Intent(this, RecordEdit.class),
					ADD_RECORD);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ADD_RECORD:
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(Constants.LOG_TAG, "bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {

			return;
		}

		menu.setHeaderTitle(cursor.getString(1));
		menu.add(0, MENU_RECORD_EDIT, 0, R.string.menu_edit_record);
		menu.add(0, MENU_ITEM_LIST, 0, R.string.menu_list_item);
		menu.add(0, MENU_RECORD_CHART, 0, R.string.menu_record_chart);
		menu.add(0, MENU_RECORD_DELETE, 0, R.string.menu_delete_record);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(Constants.LOG_TAG, "bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) {
		case MENU_RECORD_EDIT: {
			Intent intent = new Intent(this, RecordEdit.class);
			intent.putExtra("extra_record_id", info.id);
			startActivity(intent);
			return true;
		}
		case MENU_RECORD_DELETE: {
			
			this.dbHelper.deleteRecord(info.id);
			loadRecords();
			return true;
		}
		case MENU_RECORD_CHART: {
			Intent intent = new Intent(this, ChartSelector.class);
			intent.putExtra("extra_record_id", info.id);
			startActivity(intent);
			return true;
		}
		case MENU_ITEM_LIST: {
			Intent intent = new Intent(this, ItemList.class);
			intent.putExtra("extra_record_id", info.id);
			startActivity(intent);
			return true;
		}
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ItemList.class);
		intent.putExtra("extra_record_id", id);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadRecords();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.dbHelper.closeDB();
	}

	private void loadRecords() {
		progressDialog = ProgressDialog.show(this,
				getResources().getString(R.string.progress_title),
				getResources().getString(R.string.progress_message));
		new Thread() {
			@Override
			public void run() {
				cursor = dbHelper.findAllRecords();
				startManagingCursor(cursor);
				handler.sendEmptyMessage(LOAD_RECORDS);
			}
		}.start();
	}
}
