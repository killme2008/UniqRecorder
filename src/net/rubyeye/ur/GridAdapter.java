package net.rubyeye.ur;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 表格适配器
 * 
 * @author dennis
 * 
 */
public class GridAdapter extends BaseAdapter {

	private class GridHolder {
		ImageView appImage;
		TextView appName;
	}

	private final Context context;

	private List<GridInfo> list;
	private LayoutInflater mInflater;

	public GridAdapter(Context c) {
		super();
		this.context = c;
	}

	public void setList(List<GridInfo> list) {
		this.list = list;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int index) {
		return list.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		GridHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_item, null);
			holder = new GridHolder();
			holder.appImage = (ImageView) convertView
					.findViewById(R.id.itemImage);
			holder.appImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			holder.appImage.setPadding(8, 8, 8, 8);
			holder.appName = (TextView) convertView.findViewById(R.id.itemText);
			convertView.setTag(holder);

		} else {
			holder = (GridHolder) convertView.getTag();
		}

		holder.appImage.setClickable(false);
		holder.appName.setClickable(false);
		GridInfo info = list.get(index);
		if (info != null) {
			holder.appName.setText(info.getName());
			holder.appImage.setImageResource(info.getDrawable());
		}
		return convertView;
	}
}