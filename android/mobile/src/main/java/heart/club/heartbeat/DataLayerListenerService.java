package heart.club.heartbeat;

import android.os.Binder;
import android.util.Log;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public class DataLayerListenerService extends WearableListenerService {

  private static final String TAG = "DataLayerService";
  private static final String FIREBASE_DB_URL = "https://ryan.firebaseio.com";

  private Firebase mFirebaseRef;

  @Override
  public void onCreate() {
    super.onCreate();
    Firebase.setAndroidContext(this);
    mFirebaseRef = new Firebase(FIREBASE_DB_URL);
  }

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "onDataChanged: " + dataEvents);
    }
    final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);


    // Loop through the events and send a message
    // to the node that created the data item.
    for (DataEvent event : events) {
      if (event.getType() == DataEvent.TYPE_CHANGED) {
        final DataItem item = event.getDataItem();
        final String childName = item.getUri().getPath().substring(1);
        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
        sendHeartRate(childName,(int) dataMap.get("heart_rate"));
      }
    }
  }

  private void sendHeartRate(String child, int heartRate) {
    long token = Binder.clearCallingIdentity();
    try {
      mFirebaseRef.child(child).setValue(heartRate, new Firebase.CompletionListener() {
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
          if (firebaseError != null) {
            Log.d(TAG, "Firebase error: " + firebaseError.getMessage());
          } else {
            Log.d(TAG, "Firebase: sent data");
          }
        }
      });
    } finally {
      Binder.restoreCallingIdentity(token);
    }
  }

}