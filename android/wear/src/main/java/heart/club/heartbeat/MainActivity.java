package heart.club.heartbeat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

  private TextView mHeartRate;
  private SensorManager mSensorManager;
  private Sensor mHeartRateSensor;
  private SensorEventListener2 mHeartRateListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setAmbientEnabled();

    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    mHeartRate = (TextView) findViewById(R.id.heart_rate);
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerHeartRateListener();
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterHeartRateListener();
  }

  private void registerHeartRateListener() {
    mHeartRateListener = new HeartRateSensorListener();
    mSensorManager.registerListener(mHeartRateListener, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  private void unregisterHeartRateListener() {
    mSensorManager.unregisterListener(mHeartRateListener);
  }

  private class HeartRateSensorListener implements SensorEventListener2 {
    private static final String TAG = "HeartRateSensorListener";

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
        final int accuracy = event.accuracy;
        if (accuracy > 1) {
          mHeartRate.setText("Reading...");
          return;
        }
        final int heartRate = (int) event.values[0];
        mHeartRate.setText(String.valueOf(heartRate));
        Log.d(TAG, "Heart rate: " + heartRate);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
        Log.d(TAG, "Heart rate sensor accuracy changed: " + accuracy);
      }
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }
  }
}
