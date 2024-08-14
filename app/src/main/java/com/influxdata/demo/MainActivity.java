package com.influxdata.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private Accelerometer accelerometer;
  private Gyroscope gyroscope;
  private TextView logTextView;
  private InfluxDBManager influxDBManager;
  private Button loggingButton;
  private boolean isLogging = false;

  // Sensor data
  private float lastAccelX, lastAccelY, lastAccelZ;
  private float lastGyroX, lastGyroY, lastGyroZ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    logTextView = findViewById(R.id.logTextView);
    logTextView.setText("Waiting for sensor data...");
    logTextView.setMovementMethod(new ScrollingMovementMethod());

    accelerometer = new Accelerometer(this);
    gyroscope = new Gyroscope(this);

    influxDBManager = new InfluxDBManager(this);
    setupSensorListeners();

    loggingButton = findViewById(R.id.loggingButton);
    loggingButton.setText("Start Logging");
    loggingButton.setOnClickListener(v -> toggleLogging());

    Button openGraphButton = findViewById(R.id.openGraphButton);
    openGraphButton.setOnClickListener(v -> {
      if (!influxDBManager.areCredentialsSet()) {
        Toast.makeText(this, "Please set InfluxDB credentials in Settings before viewing data", Toast.LENGTH_LONG).show();
      }
      else {
        try {
          Intent intent = new Intent(MainActivity.this, GraphActivity.class);
          startActivity(intent);
        }
        catch (Exception e) {
          Toast.makeText(this, "Error opening data view: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  private void setupSensorListeners() {
    accelerometer.setListener((tx, ty, tz) -> {
      Log.d("MainActivity", "Accelerometer: " + tx + ", " + ty + ", " + tz);
      lastAccelX = tx;
      lastAccelY = ty;
      lastAccelZ = tz;
      updateBackgroundColor(tx);
      if (isLogging) {
        influxDBManager.writeSensorData("sensor_data", lastAccelX, lastAccelY, lastAccelZ, lastGyroX, lastGyroY, lastGyroZ);
      }
      logSensorData("Accelerometer", tx, ty, tz);
    });

    gyroscope.setListener((rx, ry, rz) -> {
      Log.d("MainActivity", "Gyroscope: " + rx + ", " + ry + ", " + rz);
      lastGyroX = rx;
      lastGyroY = ry;
      lastGyroZ = rz;
      if (isLogging) {
        influxDBManager.writeSensorData("sensor_data", lastAccelX, lastAccelY, lastAccelZ, lastGyroX, lastGyroY, lastGyroZ);
      }
      logSensorData("Gyroscope", rx, ry, rz);
    });
  }

  private void toggleLogging() {
    if (!isLogging) {
      if (!influxDBManager.areCredentialsSet()) {
        Toast.makeText(this, "Please enter InfluxDB credentials", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return;
      }
      influxDBManager.initializeClientWithCurrentCredentials();
    }
    isLogging = !isLogging;
    loggingButton.setText(isLogging ? "Stop Logging" : "Start Logging");
    Toast.makeText(this, isLogging ? "Logging started" : "Logging stopped", Toast.LENGTH_SHORT).show();
  }

  private void logSensorData(String sensorType, float x, float y, float z) {
    String logMessage = String.format("%s - X: %.2f, Y: %.2f, Z: %.2f\n", sensorType, x, y, z);
    runOnUiThread(() -> {
      logTextView.append(logMessage);
      // Scroll to the bottom
      logTextView.scrollTo(0, logTextView.getBottom());
    });
  }

  private void updateBackgroundColor(float value) {
    int bgColor = Color.WHITE;
    if (value > 5) {
      bgColor = Color.RED;
    }
    else if (value > 2) {
      bgColor = Color.YELLOW;
    }
    logTextView.setBackgroundColor(bgColor);
  }

  @Override
  protected void onResume() {
    super.onResume();
    accelerometer.register();
    gyroscope.register();
  }

  @Override
  protected void onPause() {
    super.onPause();
    accelerometer.unregister();
    gyroscope.unregister();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (influxDBManager != null) {
      influxDBManager.close();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}