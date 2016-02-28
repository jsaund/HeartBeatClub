package heart.club.heartbeat;

import org.json.JSONException;
import org.json.JSONObject;

public class Emotion {
  public static final int EMOTION_HAPPY = 1;
  public static final int EMOTION_SAD = 2;
  public static final int EMOTION_ANGRY = 3;
  public static final int EMOTION_EXCITED = 4;

  private final int mEmotion;
  private final String mText;
  private final String mUrl;
  private final long mTimestamp;

  public Emotion(int e, String text, String url, long timestamp) {
    mEmotion = e;
    mText = text;
    mUrl = url;
    mTimestamp = timestamp;
  }

  public JSONObject toJson() throws JSONException {
    final JSONObject o = new JSONObject();
    o.put("title", toAsciiEmoji(mEmotion));
    o.put("text", mText);
    o.put("url", mUrl);
    o.put("x", mTimestamp);

    return o;
  }

  public static String toAsciiEmoji(int emotion) {
    if (emotion == EMOTION_HAPPY) {
      return ":)";
    } else if (emotion == EMOTION_SAD) {
      return ":(";
    } else if (emotion == EMOTION_ANGRY) {
      return ">:(";
    } else if (emotion == EMOTION_EXCITED) {
      return ":D";
    } else {
      return ":|";
    }
  }

}
