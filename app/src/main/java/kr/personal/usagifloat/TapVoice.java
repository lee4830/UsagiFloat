package kr.personal.usagifloat;
import android.app.*;import android.content.*;import android.content.res.AssetFileDescriptor;import android.media.*;import android.os.*;
/** Short on-demand voices; no idle polling, delayed playback, volume changes or wake lock. */
final class TapVoice {
 private static final int[] CLIPS={R.raw.voice_yaha,R.raw.voice_una,R.raw.voice_puru,R.raw.voice_haa,R.raw.voice_wuu,R.raw.voice_high,R.raw.voice_intro,R.raw.voice_kkiyat,R.raw.voice_owung};
 private final Context context;private final SharedPreferences prefs;private final AudioManager audio;private final AudioFocusRequest focus;private final java.util.Random random=new java.util.Random();
 private final Handler guardHandler=new Handler(Looper.getMainLooper());
 private final Runnable guard=new Runnable(){public void run(){if(player==null)return;if(!allowed()){stop();return;}guardHandler.postDelayed(this,150);}};
 private MediaPlayer player;private int last=-1;int played;boolean preparing;private boolean registered;
 private final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(i.getAction())||!allowed())stop();}};
 TapVoice(Context context,SharedPreferences prefs){
  this.context=context;this.prefs=prefs;audio=context.getSystemService(AudioManager.class);
  focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(attributes()).setAcceptsDelayedFocusGain(false).setWillPauseWhenDucked(true).setOnAudioFocusChangeListener(change->{if(change<0)stop();},new Handler(Looper.getMainLooper())).build();
  IntentFilter f=new IntentFilter();f.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);f.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);f.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
  if(Build.VERSION.SDK_INT>=33)context.registerReceiver(receiver,f,Context.RECEIVER_NOT_EXPORTED);else context.registerReceiver(receiver,f);registered=true;
 }
 private AudioAttributes attributes(){return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();}
 boolean allowed(){return VoicePolicy.allowed(prefs.getBoolean("tap_voice",false),audio.getRingerMode(),audio.getStreamVolume(AudioManager.STREAM_MUSIC),context.getSystemService(PowerManager.class).isInteractive(),context.getSystemService(KeyguardManager.class).isKeyguardLocked(),context.getSystemService(NotificationManager.class).getCurrentInterruptionFilter()==NotificationManager.INTERRUPTION_FILTER_ALL);}
 void play(){
  if(!allowed()){stop();return;}if(player!=null)return;
  try{
   if(audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED)return;
   int next=random.nextInt(CLIPS.length-1);if(next>=last)next++;if(last<0)next=random.nextInt(CLIPS.length);last=next;
   MediaPlayer p=new MediaPlayer();player=p;preparing=true;p.setAudioAttributes(attributes());
   try(AssetFileDescriptor fd=context.getResources().openRawResourceFd(CLIPS[next])){p.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}
   p.setOnPreparedListener(mp->{if(player!=mp)return;if(!allowed()){stop();return;}preparing=false;mp.start();played++;});
   p.setOnCompletionListener(mp->{if(player==mp)stop();});p.setOnErrorListener((mp,what,extra)->{if(player==mp)stop();return true;});p.prepareAsync();guardHandler.postDelayed(guard,150);
  }catch(Exception e){stop();android.util.Log.w("UsagiFloat","Voice unavailable",e);}
 }
 void refresh(){if(!allowed())stop();}
 void stop(){guardHandler.removeCallbacks(guard);MediaPlayer p=player;player=null;preparing=false;if(p!=null){p.setOnPreparedListener(null);p.setOnCompletionListener(null);p.setOnErrorListener(null);p.release();}audio.abandonAudioFocusRequest(focus);}
 void dump(java.io.PrintWriter writer){writer.println("voiceClip="+last);guardHandler.dump(new android.util.PrintWriterPrinter(writer),"voiceHandler ");}
 boolean active(){return player!=null;}
 void close(){stop();if(registered){context.unregisterReceiver(receiver);registered=false;}}
}
