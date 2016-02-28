package heart.club.heartbeat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

  private BoxInsetLayout mContainerView;
  private TextView mHeartRate;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setAmbientEnabled();

    final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    final Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    sensorManager.registerListener(new HeartRateSensorListener(), heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

    mContainerView = (BoxInsetLayout) findViewById(R.id.container);
    mHeartRate = (TextView) findViewById(R.id.heart_rate);
  }

  @Override
  public void onEnterAmbient(Bundle ambientDetails) {
    super.onEnterAmbient(ambientDetails);
    updateDisplay();
  }

  @Override
  public void onUpdateAmbient() {
    super.onUpdateAmbient();
    updateDisplay();
  }

  @Override
  public void onExitAmbient() {
    updateDisplay();
    super.onExitAmbient();
  }

  private void updateDisplay() {
    if (isAmbient()) {
      mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
      mHeartRate.setTextColor(getResources().getColor(android.R.color.white));
    } else {
      mContainerView.setBackground(null);
      mHeartRate.setTextColor(getResources().getColor(android.R.color.black));
    }
  }

  private class HeartRateSensorListener implements SensorEventListener2 {
    private static final String TAG = "HeartRateSensorListener";

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
        final float heartRate = event.values[0];
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
