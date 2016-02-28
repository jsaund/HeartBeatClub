package heart.club.heartbeat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

public class InitActivity extends AppCompatActivity {
  private static final String TWITTER_KEY = "7VRY0HXpaXdDYYvLfbX726LSc";
  private static final String TWITTER_SECRET = "ByWVHRbZOwTGlH2I3c423Ft1WGdUwsTywY3KDQVAfAz3W34H8x";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
    Fabric.with(this, new Twitter(authConfig));
    startLoginActivity();
  }

  private void startLoginActivity() {
    startActivity(new Intent(this, LoginActivity.class));
  }
}
