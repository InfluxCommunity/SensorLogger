package com.influxdata.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InfluxDBManager {

  private InfluxDBClient influxDBClient;
  private WriteApi writeApi;
  private ExecutorService executorService;
  private boolean isOpen = false; // Track whether the connection is open

  private final SharedPreferences sharedPreferences;

  public InfluxDBManager(Context context) {
    this.sharedPreferences = context.getSharedPreferences("InfluxDBPrefs", Context.MODE_PRIVATE);

    if (areCredentialsSet()) {
      initializeClient();
    }
  }

  private void initializeClient() {
    String url = sharedPreferences.getString("INFLUX_URL", "");
    String apiToken = sharedPreferences.getString("INFLUX_API_TOKEN", "");
    String org = sharedPreferences.getString("INFLUX_ORG", "");
    String bucket = sharedPreferences.getString("INFLUX_BUCKET", "");

    this.influxDBClient = InfluxDBClientFactory.create(url, apiToken.toCharArray(), org, bucket);
    this.writeApi = influxDBClient.makeWriteApi();
    this.executorService = Executors.newSingleThreadExecutor();
    this.isOpen = true;
  }

  public void writeSensorData(String measurement, float accelX, float accelY, float accelZ, float gyroX, float gyroY, float gyroZ) {
    if (!isOpen) {
      Log.e("InfluxDBManager", "InfluxDB client is not initialized. Please set credentials first.");
      return;
    }

    Log.d("InfluxDBManager", "Writing data: AccelX=" + accelX + ", AccelY=" + accelY + ", AccelZ=" + accelZ + ", GyroX=" + gyroX + ", GyroY=" + gyroY + ", GyroZ=" + gyroZ);
    Point point = Point.measurement(measurement)
        .addTag("device", android.os.Build.MODEL)
        .addField("accel_x", accelX)
        .addField("accel_y", accelY)
        .addField("accel_z", accelZ)
        .addField("gyro_x", gyroX)
        .addField("gyro_y", gyroY)
        .addField("gyro_z", gyroZ)
        .time(Instant.now(), WritePrecision.MS);

    executorService.execute(() -> {
      try {
        writeApi.writePoint(point);
      } catch (Exception e) {
        Log.e("InfluxDBManager", "Error writing to InfluxDB", e);
      }
    });
  }

  public void querySensorData(String query, QueryCallback callback) {
    executorService.execute(() -> {
      try {
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(query);
        callback.onSuccess(tables);
      }
      catch (Exception e) {
        Log.e("InfluxDBManager", "Error querying InfluxDB", e);
        callback.onError(e);
      }
    });
  }

  public String getBucket() {
    return sharedPreferences.getString("INFLUX_BUCKET", ""); // Default to an empty string if not found
  }

  public void close() {
    if (isOpen) {
      executorService.execute(() -> {
        try {
          influxDBClient.close();
          isOpen = false;
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
  }

  public void initializeClientWithCurrentCredentials() {
    if (areCredentialsSet()) {
      initializeClient();
    } else {
      Log.e("InfluxDBManager", "Credentials are not set. Cannot initialize client.");
    }
  }

  public boolean areCredentialsSet() {
    String url = sharedPreferences.getString("INFLUX_URL", "");
    String apiToken = sharedPreferences.getString("INFLUX_API_TOKEN", "");
    String org = sharedPreferences.getString("INFLUX_ORG", "");
    String bucket = sharedPreferences.getString("INFLUX_BUCKET", "");

    return !url.isEmpty() && !apiToken.isEmpty() && !org.isEmpty() && !bucket.isEmpty();
  }

  public interface QueryCallback {
    void onSuccess(List<FluxTable> tables);

    void onError(Exception e);
  }
}