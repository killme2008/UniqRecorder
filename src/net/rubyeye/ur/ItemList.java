package net.rubyeye.ur;

import android.app.AlertDialog;
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

public class ItemList extends ListActivity {
	private RecordDBHelper dbHelper;
	private ProgressDialog progressDialog;
	private Cursor cursor;
	public static final String CLASSTAG = ItemList.class.getSimpleName();
	private ListAdapter listAdapter;

	// Menu item ids
	public static final int MENU_ITEM_EDIT = Menu.FIRST;
	public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
	public static final int MENU_RECORD_CHART = Menu.FIRST + 3;
	public static final int MENU_ITEM_DELETE_ALL = Menu.FIRST + 4;
	private long recordId;

	static final int LOAD_ITEMS = 1;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == LOAD_ITEMS) {
				Log.v(Constants.LOG_TAG, " " + ItemList.CLASSTAG
						+ " worker thread done, setup list");
				if (progressDialog != null)
					progressDialog.dismiss();
				progressDialog = null;
				if ((cursor == null) || (cursor.getCount() == 0)) {
					setListAdapter(null);
					return;
				} else {
					listAdapter = new SimpleCursorAdapter(ItemList.this,
							R.layout.item, cursor, new String[] { "created",
									"value" }, new int[] { R.id.itemCreated,
									R.id.itemValue });
					setListAdapter(listAdapter);
				}
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_list);
		getListView().setOnCreateContextMenuListener(this);
		dbHelper = new RecordDBHelper(this);
		dbHelper.openDB(this);
		this.recordId = getIntent().getLongExtra("extra_record_id", -1);

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (this.recordId < 0) {
			new AlertDialog.Builder(this).setTitle("发生错误").setMessage("无效的项目")
					.show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert_item).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(0, MENU_RECORD_CHART, 0, R.string.menu_record_chart).setIcon(
				R.drawable.chart_36);
		menu.add(0, MENU_ITEM_DELETE_ALL, 0, R.string.menu_delete_item_all)
				.setIcon(android.R.drawable.ic_delete);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_ITEM_INSERT:
			// Launch activity to insert a new record
			Intent intent = new Intent(this, ItemEdit.class);
			intent.putExtra("extra_record_id", this.recordId);
			startActivity(intent);
			return true;
		case MENU_RECORD_CHART: {
			intent = new Intent(this, ChartSelector.class);
			intent.putExtra("extra_record_id", this.recordId);
			startActivity(intent);
			return true;
		}
		case MENU_ITEM_DELETE_ALL: {
			this.dbHelper.deleteItemByRecordId(recordId);
			this.loadItems();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
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
		menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_edit_item);
		menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete_item);
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
		case MENU_ITEM_EDIT: {
			Intent intent = new Intent(this, ItemEdit.class);
			intent.putExtra("extra_item_id", info.id);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
			return true;
		}
		case MENU_ITEM_DELETE: {
			this.dbHelper.deleteItem(info.id);
			loadItems();
			return true;
		}

		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ItemEdit.class);
		intent.putExtra("extra_item_id", id);
		intent.putExtras(getIntent().getExtras());
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadItems();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.dbHelper.closeDB();
	}

	private void loadItems() {
		progressDialog = ProgressDialog.show(this,
				getResources().getString(R.string.progress_title),
				getResources().getString(R.string.progress_message));
		new Thread() {
			@Override
			public void run() {
				cursor = dbHelper.findItemsByRecordId(recordId);
				startManagingCursor(cursor);
				handler.sendEmptyMessage(LOAD_ITEMS);
			}
		}.start();
	}
}
