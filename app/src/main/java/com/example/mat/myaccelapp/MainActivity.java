package com.example.mat.myaccelapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[4];
    private final float[] mMagnetometerReading = new float[4];
    private final float[] mAccelerometerWorld = new float[4];

    private final float[] mRotationMatrix = new float[16];
    private final float[] mOrientationAngles = new float[3];

    public static final String EXTRA_MESSAGE = "com.example.mat.myaccelapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, 3);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, 3);
        }

        updateOrientationAngles();

        float[] rot = new float[16];
        float[] inv = new float[16];

        System.arraycopy(mRotationMatrix, 0, rot, 0, mRotationMatrix.length);

        android.opengl.Matrix.invertM(inv, 0, mRotationMatrix, 0);
        android.opengl.Matrix.multiplyMV(mAccelerometerWorld, 0, inv, 0, mAccelerometerReading, 0);

        // Do something with this sensor value.
        TextView textView = findViewById(R.id.textViewAccel);
        if (textView != null) {
            textView.setText(String.format("%.2f, %.2f, %.2f", mAccelerometerReading[0], mAccelerometerReading[1], mAccelerometerReading[2]));
        }

        // Do something with this sensor value.
        TextView textView2 = findViewById(R.id.textViewAccelWorld);
        if (textView2 != null) {
            textView2.setText(String.format("%.2f, %.2f, %.2f", mAccelerometerWorld[0], mAccelerometerWorld[1], mAccelerometerWorld[2]));
        }


        //Log.d("Raw Magnetic Field","Values: (" + mMagnetometerReading[0] + ", " + mMagnetometerReading[1] + ", " + mMagnetometerReading[2] + ")");
        //Log.d("Raw Acceleration::","Values: (" + mAccelerometerReading[0] + ", " + mAccelerometerReading[1] + ", " + mAccelerometerReading[2] + ")");
        //Log.d("Earth Acceleration","Values: (" + mAccelerometerWorld[0] + ", " + mAccelerometerWorld[1] + ", " + mAccelerometerWorld[2] + ")");
        //Log.d("R:", String.format("Values:%.2f, %.2f, %.2f, %.2f; %.2f, %.2f, %.2f, %.2f; %.2f, %.2f, %.2f, %.2f; %.2f, %.2f, %.2f, %.2f;", rot[0], rot[1], rot[2], rot[3], rot[4], rot[5], rot[6], rot[7], rot[8], rot[9], rot[10], rot[11], rot[12], rot[13], rot[14], rot[15]));


    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        // "mOrientationAngles" now has up-to-date information.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
