package net.rubyeye.ur;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.youmi.android.AdManager;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordChart extends Activity {
	private RecordDBHelper dbHelper;
	private TextView totalView;
	private TextView averageView;
	private TextView dateView;
	private LinearLayout layout;
	static {
		if (Constants.ENABLE_AD) {
			AdManager.init(Constants.APP_ID, Constants.APP_PASS,
					Constants.APP_INTERVAL, Constants.TEST_MODE,
					Constants.APP_VERSION);
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_chart);

		dbHelper = new RecordDBHelper(this);
		dbHelper.openDB(this);

		layout = (LinearLayout) findViewById(R.id.recordChartLayout);
		totalView = (TextView) findViewById(R.id.total_label);
		averageView = (TextView) findViewById(R.id.average_label);
		dateView = (TextView) findViewById(R.id.date_label);

		long recordId = getIntent().getLongExtra("extra_record_id", -1);
		String start = getIntent().getStringExtra("extra_start");
		String end = getIntent().getStringExtra("extra_end");

		dateView.setText(start + "至" + end);

		if (recordId < 0) {
			TextView textView = new TextView(this);
			textView.setText("此项目不存在");
			layout.addView(textView);
			return;
		}

		Cursor recordCur = dbHelper.findRecordById(recordId);
		Cursor listCursor = dbHelper.findItemsBewtten(recordId, start, end);
		startManagingCursor(recordCur);
		startManagingCursor(listCursor);

		List<Double> values = new ArrayList<Double>();
		List<Double> days = new ArrayList<Double>();
		int count = listCursor.getCount();

		if (count == 0) {
			TextView textView = new TextView(this);
			textView.setText("暂无此段时间内的记录，请重新选择");
			layout.addView(textView);
		}
		if (count > 0) {
			displayChart(recordCur, listCursor, values, days, count);
		}
	}

	private void displayChart(Cursor recordCur, Cursor listCursor,
			List<Double> values, List<Double> days, int count) {
		WindowManager manager = getWindowManager();
		int width = manager.getDefaultDisplay().getWidth();
		boolean wasFirst = true;
		int step = (width - 30) / count;
		if (step <= 0)
			step = 1;
		if (step > width / 3) {
			step = width / 3;
		}
		double begin = 1;
		double maxValue = 0;
		double total = 0;
		for (int i = 0; i < count; i++) {
			double value = listCursor.getDouble(listCursor
					.getColumnIndex("value"));
			values.add(value);
			total += value;
			if (value > maxValue) {
				maxValue = value;
			}
			if (wasFirst) {
				wasFirst = false;
			} else {
				begin += step;
			}
			days.add(begin);
			listCursor.moveToNext();
		}

		String recordName = recordCur.getString(recordCur
				.getColumnIndex("name"));
		String title = recordName + "记录曲线图";
		setTitle(title);
		String[] titles = new String[] { recordName };

		List<List<Double>> x = new ArrayList<List<Double>>();
		List<List<Double>> y = new ArrayList<List<Double>>();

		x.add(days);

		y.add(values);

		XYMultipleSeriesDataset dataset = buildDataset(titles, x, y);

		int[] colors = new int[] { getColor() };
		PointStyle[] styles = new PointStyle[] { getPointStyle() };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);

		setChartSettings(renderer, "", "日期", recordName, -1, begin, 0,
				maxValue, Color.BLACK, Color.BLACK);

		GraphicalView chart = ChartFactory.getLineChartView(this, dataset,
				renderer);
		chart.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		chart.setBackgroundColor(Color.WHITE);
		totalView.setText(String.format("总计:%.2f", total));
		averageView.setText(String.format("平均:%.2f", total / count));
		// chart.startAnimation(AnimationUtils.loadAnimation(this,
		// R.anim.scaler));
		layout.addView(chart);
	}

	private static final int[] colors = new int[] { Color.BLUE, Color.RED,
			Color.BLACK };

	private static final PointStyle[] styles = new PointStyle[] {
			PointStyle.DIAMOND, PointStyle.CIRCLE, PointStyle.POINT,
			PointStyle.SQUARE, PointStyle.TRIANGLE };

	public int getColor() {
		Random rand = new Random();
		return colors[rand.nextInt(colors.length)];
	}

	public PointStyle getPointStyle() {
		Random rand = new Random();
		return styles[rand.nextInt(styles.length)];
	}

	protected XYMultipleSeriesDataset buildDataset(String[] titles,
			List<List<Double>> xValues, List<List<Double>> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length; // 有几条线
		for (int i = 0; i < length; i++) {
			XYSeries series = new XYSeries(titles[i]); // 根据每条线的名称创建
			List<Double> xV = xValues.get(i); // 获取第i条线的数据
			List<Double> yV = yValues.get(i);
			int seriesLength = xV.size(); // 有几个点

			for (int k = 0; k < seriesLength; k++) // 每条线里有几个点
			{

				series.add(xV.get(k), yV.get(k));
			}

			dataset.addSeries(series);
		}

		return dataset;
	}

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			r.setFillPoints(fill);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	@Override
	protected void onDestroy() {
		this.dbHelper.closeDB();
		super.onDestroy();
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setDisplayChartValues(true);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}
}
