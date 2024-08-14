package com.influxdata.demo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Gyroscope {
  private SensorManager sensorManager;
  private Sensor gyroscope;
  private Listener listener;

  public Gyroscope(Context context) {
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
  }

  public void register() {
    sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
  }

  public void unregister() {
    sensorManager.unregisterListener(sensorEventListener);
  }

  private SensorEventListener sensorEventListener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent event) {
      if (listener != null) {
        listener.onSensorChanged(event.values[0], event.values[1], event.values[2]);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
  };

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public interface Listener {
    void onSensorChanged(float x, float y, float z);
  }
}