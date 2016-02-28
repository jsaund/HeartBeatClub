package heart.club.heartbeat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity {

  private TwitterLoginButton mTwitterLoginButton;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_button);
    mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
      @Override
      public void success(Result<TwitterSession> result) {
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.app_name),
          Toast.LENGTH_SHORT)
          .show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
      }

      @Override
      public void failure(TwitterException e) {
        Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.app_name),
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
  }
}
