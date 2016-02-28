package heart.club.heartbeat;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import io.fabric.sdk.android.Fabric;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "r7KODuasYuBzQle7opCBf0a4t";
    private static final String TWITTER_SECRET = "E9j6sELkGfsKj8AfexMlX1qfFzHVxVhN7feaRyPqX0affi71j8";

  private static final String TAG = "Adrenaline";
  private static final String FIREBASE_DB_URL = "https://ryan.firebaseio.com";
  private Firebase mFirebaseRef;

  private GoogleApiClient mGoogleApiClient;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
    Fabric.with(this, new Twitter(authConfig));
    setContentView(R.layout.activity_main);
    Firebase.setAndroidContext(this);
    mFirebaseRef = new Firebase(FIREBASE_DB_URL);

    mGoogleApiClient = new GoogleApiClient.Builder(this)
      .addApi(Wearable.API)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .build();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
      Wearable.DataApi.removeListener(mGoogleApiClient, this);
      mGoogleApiClient.disconnect();
    }
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
        if (childName.contains("flags")) {
          final String title = dataMap.getString("title");
          final String text = dataMap.getString("text");
          final String url = dataMap.getString("url");
          final long x = dataMap.getLong("x");
          final int heartRate = dataMap.getInt("heart_rate");
          final Emotion e = new Emotion(title, text, url, x);
          sendShare(childName, e, heartRate);
        } else {
          sendHeartRate(childName, (int) dataMap.get("heart_rate"));
        }
      }
    }
  }

  private void sendShare(String child, Emotion data, int heartRate) {
    mFirebaseRef.child(child).setValue(data, new Firebase.CompletionListener() {
      @Override
      public void onComplete(FirebaseError firebaseError, Firebase firebase) {
        if (firebaseError != null) {
          Log.d(TAG, "Firebase error: " + firebaseError.getMessage());
        } else {
          Log.d(TAG, "Firebase: sent data");
        }
      }
    });
    final Uri uri = Uri.parse(String.format("http://104.197.44.2/img/HB%d.gif", heartRate));
    TweetComposer.Builder builder = new TweetComposer.Builder(this)
      .text(data.title + " " + data.text + " " + uri.toString());
    builder.show();
  }

  private void sendHeartRate(String child, int heartRate) {
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
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Wearable.DataApi.addListener(mGoogleApiClient, this);
  }

  @Override
  public void onConnectionSuspended(int i) {

  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }
}

