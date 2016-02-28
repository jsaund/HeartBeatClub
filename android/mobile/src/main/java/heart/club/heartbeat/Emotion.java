package heart.club.heartbeat;

public class Emotion {
  public final String title;
  public final String text;
  public final String url;
  public final long x;

  public Emotion(String asciiEmoji, String text, String url, long timestamp) {
    this.title = asciiEmoji;
    this.text = text;
    this.url = url;
    this.x = timestamp;
  }
}
