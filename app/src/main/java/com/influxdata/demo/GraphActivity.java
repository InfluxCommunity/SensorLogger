package com.influxdata.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.view.MenuItem;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphActivity extends AppCompatActivity {

  private LineChart chart;
  private InfluxDBManager influxDBManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_graph);

    // Enable back button in ActionBar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Sensor Data Graph");
    }

    chart = findViewById(R.id.chart);
    Button refreshButton = findViewById(R.id.refreshButton);

    setupChart();
    influxDBManager = new InfluxDBManager(this);

    refreshButton.setOnClickListener(v -> refreshData());

    // Initial data load
    refreshData();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish(); // Close this activity and go back to the previous one
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupChart() {
    chart.getDescription().setTextColor(Color.WHITE);
    chart.getDescription().setTextSize(12f);

    chart.getXAxis().setTextColor(Color.WHITE);
    chart.getXAxis().setTextSize(12f);

    chart.getAxisLeft().setTextColor(Color.WHITE);
    chart.getAxisLeft().setTextSize(12f);

    chart.getLegend().setTextColor(Color.WHITE);
    chart.getLegend().setTextSize(12f);

    // Increase the size of the chart
    ViewGroup.LayoutParams params = chart.getLayoutParams();
    params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7); // 70% of screen height
    chart.setLayoutParams(params);

    Description description = new Description();
    description.setText("Sensor Data");
    chart.setDescription(description);

    XAxis xAxis = chart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setValueFormatter(new ValueFormatter() {
      private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

      @Override
      public String getFormattedValue(float value) {
        long millis = (long) value;
        return mFormat.format(new Date(millis));
      }
    });

    chart.getAxisLeft().setAxisMinimum(-10f);
    chart.getAxisLeft().setAxisMaximum(10f);
    chart.getAxisRight().setEnabled(false);
    chart.setDragEnabled(true);
    chart.setScaleEnabled(true);
  }

  private void refreshData() {
    // Retrieve the bucket name dynamically
    String bucket = influxDBManager.getBucket();

    String flux = "from(bucket:\"" + bucket + "\") " +
        "|> range(start: -10m) " +
        "|> filter(fn: (r) => r._measurement == \"sensor_data\") " +
        "|> filter(fn: (r) => r._field == \"accel_x\" or r._field == \"gyro_x\") " +
        "|> aggregateWindow(every: 5s, fn: mean, createEmpty: false) " +
        "|> yield(name: \"mean\")";

    influxDBManager.querySensorData(flux, new InfluxDBManager.QueryCallback() {
      @Override
      public void onSuccess(List<FluxTable> tables) {
        List<Entry> accelEntries = new ArrayList<>();
        List<Entry> gyroEntries = new ArrayList<>();

        for (FluxTable table : tables) {
          for (FluxRecord record : table.getRecords()) {
            long time = record.getTime().toEpochMilli();
            double value = (double) record.getValue();
            String field = record.getField();

            if ("accel_x".equals(field)) {
              accelEntries.add(new Entry(time, (float) value));
            } else if ("gyro_x".equals(field)) {
              gyroEntries.add(new Entry(time, (float) value));
            }
          }
        }
        runOnUiThread(() -> updateChart(accelEntries, gyroEntries));
      }

      @Override
      public void onError(Exception e) {
        Log.e("GraphActivity", "Error fetching data from InfluxDB", e);
        runOnUiThread(() -> Toast.makeText(GraphActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show());
      }
    });
  }

  private void updateChart(List<Entry> accelEntries, List<Entry> gyroEntries) {
    LineDataSet accelDataSet = new LineDataSet(accelEntries, "Accelerometer X");
    accelDataSet.setColor(Color.BLUE);
    accelDataSet.setDrawCircles(false);

    LineDataSet gyroDataSet = new LineDataSet(gyroEntries, "Gyroscope X");
    gyroDataSet.setColor(Color.RED);
    gyroDataSet.setDrawCircles(false);

    LineData lineData = new LineData(accelDataSet, gyroDataSet);
    chart.setData(lineData);
    chart.invalidate();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (influxDBManager != null) {
      influxDBManager.close();
    }
  }
}