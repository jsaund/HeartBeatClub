package heart.club.heartbeat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity {
  private static final String TAG = "Wearable";
  private static final String KEY_HEART_RATE = "heart_rate";

  private TextView mHeartRate;
  private TextView mDebug;
  private SensorManager mSensorManager;
  private Sensor mHeartRateSensor;
  private SensorEventListener2 mHeartRateListener;

  private GoogleApiClient mGoogleApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setAmbientEnabled();

    mGoogleApiClient = new GoogleApiClient
      .Builder(this)
      .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
          registerHeartRateListener();
        }

        @Override
        public void onConnectionSuspended(int i) {
          unregisterHeartRateListener();
        }
      })
      .addApi(Wearable.API)
      .build();
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    mHeartRate = (TextView) findViewById(R.id.heart_rate);
    mDebug = (TextView) findViewById(R.id.debug);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mGoogleApiClient.disconnect();
    unregisterHeartRateListener();
  }

  private void sendHeartRate(int heartRate) {
    mDebug.setText("Send heart rate");
    final String childName = "/heart_rate/jag/data/" + System.currentTimeMillis();
    PutDataMapRequest data = PutDataMapRequest.create(childName);
    data.getDataMap().putInt(KEY_HEART_RATE, heartRate);

    PutDataRequest req = data.asPutDataRequest();
    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, req);
    pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
      @Override
      public void onResult(@NonNull final DataApi.DataItemResult result) {
        if(result.getStatus().isSuccess()) {
          mDebug.setText("sent! " + result.getDataItem().getUri());
        } else {
          mDebug.setText("send failed");
        }
      }
    });
  }

  private void registerHeartRateListener() {
    mHeartRateListener = new HeartRateSensorListener();
    mSensorManager.registerListener(mHeartRateListener, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  private void unregisterHeartRateListener() {
    if (mHeartRateListener != null) {
      mSensorManager.unregisterListener(mHeartRateListener);
    }
  }

  private class HeartRateSensorListener implements SensorEventListener2 {
    private static final String TAG = "HeartRateSensorListener";

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
        final int accuracy = event.accuracy;
        if (accuracy > 2) {
          mHeartRate.setTextColor(getResources().getColor(R.color.reading));
          return;
        }
        final int heartRate = (int) event.values[0];
        mHeartRate.setText(String.valueOf(heartRate));
        mHeartRate.setTextColor(getResources().getColor(R.color.heart_rate));
        Log.d(TAG, "Heart rate: " + heartRate);
        if (heartRate > 0) {
          sendHeartRate(heartRate);
        }
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
