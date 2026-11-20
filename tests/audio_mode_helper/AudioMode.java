package kr.personal.usagitest;
public class AudioMode extends android.app.Activity {
 public void onCreate(android.os.Bundle b){super.onCreate(b);android.media.AudioManager a=getSystemService(android.media.AudioManager.class);if(getIntent().hasExtra("ringer"))a.setRingerMode(getIntent().getIntExtra("ringer",2));if(getIntent().hasExtra("volume"))a.setStreamVolume(3,getIntent().getIntExtra("volume",5),0);finish();}
}
