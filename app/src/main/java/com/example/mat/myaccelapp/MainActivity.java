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
import android.widget.ProgressBar;
import android.widget.TextView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[4];
    private final float[] mMagnetometerReading = new float[4];
    private final float[] mAccelerometerWorld = new float[4];

    private float mAccelerometerTimestampPrevious = 0;
    private final float[] mVelocityWorld = new float[4];
    private final float[] mPositionWorld = new float[4];

    private final float[] mRotationMatrix = new float[16];
    private final float[] mOrientationAngles = new float[3];

    private int mCalibrationCounter = 0;
    private float mGravityCalibrationValue = SensorManager.STANDARD_GRAVITY;
    private boolean mIsCalibrated = FALSE;
    public static final int CALIBRATION_COUNT = 20;

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
            float deltaTime = (event.timestamp - mAccelerometerTimestampPrevious) / 1000000000.0f;  // seconds
            mAccelerometerTimestampPrevious = event.timestamp;
            updateOrientationAngles();
            onAccelerometerUpdate(deltaTime);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, 3);
            // Transform acceleration values from device coordinates to world coordinates
            updateOrientationAngles();
        }

    }

    // Called when accelerometer reading is updated.
    // Transform to world coordinates and sum up for velocity and position
    public void onAccelerometerUpdate(float deltaTime) {

        // v_W = R^{-1} * v_B (therefore we need to calc the inverse first)
        float[] inv = new float[16];
        android.opengl.Matrix.invertM(inv, 0, mRotationMatrix, 0);
        android.opengl.Matrix.multiplyMV(mAccelerometerWorld, 0, inv, 0, mAccelerometerReading, 0);

        // Remove gravity offset (do calibration at startup)
        if (mIsCalibrated == FALSE) {
            if (mCalibrationCounter < CALIBRATION_COUNT) {
                if (mCalibrationCounter == 0) {
                    mGravityCalibrationValue = mAccelerometerWorld[2];
                } else {
                    mGravityCalibrationValue += (mAccelerometerWorld[2] - mGravityCalibrationValue) * 0.2;
                }
                Log.d("Gravity:", String.format("Calibration value (%d): %.6f", mCalibrationCounter, mGravityCalibrationValue));
                ProgressBar progressBar = findViewById(R.id.progressBarCalibration);
                if (progressBar != null) {
                    progressBar.setProgress((100 * mCalibrationCounter) / (CALIBRATION_COUNT-1));
                }
                mCalibrationCounter++;
            } else {
                mIsCalibrated = TRUE;
            }
        }


        // Display text
        TextView textView;
        textView = findViewById(R.id.textViewAccel);
        if (textView != null) {
            textView.setText(String.format("%.2f, %.2f, %.2f", mAccelerometerReading[0], mAccelerometerReading[1], mAccelerometerReading[2]));
        }

        textView = findViewById(R.id.textViewUpdateRate);
        if (textView != null) {
            textView.setText(String.format("%.3f", deltaTime));
        }

        if (mIsCalibrated == TRUE) {
            mAccelerometerWorld[2] -= mGravityCalibrationValue;
            for (int i = 0; i < 3; i++) {
                mVelocityWorld[i] += deltaTime * mAccelerometerWorld[i];
                mPositionWorld[i] += deltaTime * mVelocityWorld[i];
            }


            textView = findViewById(R.id.textViewAccelWorld);
            if (textView != null) {
                textView.setText(String.format("%.2f, %.2f, %.2f", mAccelerometerWorld[0], mAccelerometerWorld[1], mAccelerometerWorld[2]));
            }

            textView = findViewById(R.id.textViewVelocity);
            if (textView != null) {
                textView.setText(String.format("%.2f, %.2f, %.2f", mVelocityWorld[0], mVelocityWorld[1], mVelocityWorld[2]));
            }

            textView = findViewById(R.id.textViewPosition);
            if (textView != null) {
                textView.setText(String.format("%.2f, %.2f, %.2f", mPositionWorld[0], mPositionWorld[1], mPositionWorld[2]));
            }

        }

        //Log.d("Raw Magnetic Field","Values: (" + mMagnetometerReading[0] + ", " + mMagnetometerReading[1] + ", " + mMagnetometerReading[2] + ")");
        //Log.d("Raw Acceleration::","Values: (" + mAccelerometerReading[0] + ", " + mAccelerometerReading[1] + ", " + mAccelerometerReading[2] + ")");
        //Log.d("Earth Acceleration","Values: (" + mAccelerometerWorld[0] + ", " + mAccelerometerWorld[1] + ", " + mAccelerometerWorld[2] + ")");
        //Log.d("R:", String.format("Values:%.2f, %.2f, %.2f, %.2f; %.2f, %.2f, %.2f, %.2f; %.2f, %.2f, %.2f, %.2f; %.2f, %.2f, %.2f, %.2f;", rot[0], rot[1], rot[2], rot[3], rot[4], rot[5], rot[6], rot[7], rot[8], rot[9], rot[10], rot[11], rot[12], rot[13], rot[14], rot[15]));

        //Log.d("Velocity", String.format("%.2f, %.2f, %.2f", mVelocityWorld[0], mVelocityWorld[1], mVelocityWorld[2]));
        //Log.d("Position", String.format("%.2f, %.2f, %.2f", mPositionWorld[0], mPositionWorld[1], mPositionWorld[2]));


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
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_FASTEST);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
