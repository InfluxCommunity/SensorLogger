package com.influxdata.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

  private EditText editTextInfluxUrl, editTextApiToken, editTextInfluxOrg, editTextInfluxBucket;
  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    editTextInfluxUrl = findViewById(R.id.editTextInfluxUrl);
    editTextApiToken = findViewById(R.id.editTextApiToken);
    editTextInfluxOrg = findViewById(R.id.editTextInfluxOrg);
    editTextInfluxBucket = findViewById(R.id.editTextInfluxBucket);
    Button buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
    Button buttonResetSettings = findViewById(R.id.buttonResetSettings);

    sharedPreferences = getSharedPreferences("InfluxDBPrefs", MODE_PRIVATE);
    loadPreferences();

    buttonSaveSettings.setOnClickListener(v -> savePreferences());
    buttonResetSettings.setOnClickListener(v -> resetPreferences());
  }

  private void savePreferences() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("INFLUX_URL", editTextInfluxUrl.getText().toString());
    editor.putString("INFLUX_API_TOKEN", editTextApiToken.getText().toString());
    editor.putString("INFLUX_ORG", editTextInfluxOrg.getText().toString());
    editor.putString("INFLUX_BUCKET", editTextInfluxBucket.getText().toString());
    editor.apply();

    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
  }

  private void loadPreferences() {
    editTextInfluxUrl.setText(sharedPreferences.getString("INFLUX_URL", ""));
    editTextApiToken.setText(sharedPreferences.getString("INFLUX_API_TOKEN", ""));
    editTextInfluxOrg.setText(sharedPreferences.getString("INFLUX_ORG", ""));
    editTextInfluxBucket.setText(sharedPreferences.getString("INFLUX_BUCKET", ""));
  }

  private void resetPreferences() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.clear();
    editor.apply();
    loadPreferences();  // Refresh fields with cleared values

    Toast.makeText(this, "Settings reset", Toast.LENGTH_SHORT).show();
  }
}