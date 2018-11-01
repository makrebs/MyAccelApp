package com.example.mat.myaccelapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mLinAccel;

    public static final String EXTRA_MESSAGE = "com.example.mat.myaccelapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /** Called when the user taps the Check sensor button */
    public void showSensor(View view) {
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Many sensors return 3 values, one for each axis.
            float[] acc = new float[3];
            acc[0] = event.values[0];
            acc[1] = event.values[1];
            acc[2] = event.values[2];
            // Do something with this sensor value.
            TextView textView = findViewById(R.id.textViewAccel);
            textView.setText(String.format("%.2f, %.2f, %.2f", acc[0], acc[1], acc[2]));
        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Many sensors return 3 values, one for each axis.
            float[] acc = new float[3];
            acc[0] = event.values[0];
            acc[1] = event.values[1];
            acc[2] = event.values[2];
            // Do something with this sensor value.
            TextView textView = findViewById(R.id.textViewLinAccel);
            textView.setText(String.format("%.2f, %.2f, %.2f", acc[0], acc[1], acc[2]));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLinAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
