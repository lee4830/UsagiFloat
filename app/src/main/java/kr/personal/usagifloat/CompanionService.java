package kr.personal.usagifloat;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class CompanionService extends Service {
 static final String START="kr.personal.usagifloat.START", STOP="kr.personal.usagifloat.STOP", UPDATE="kr.personal.usagifloat.UPDATE", TIMER="kr.personal.usagifloat.TIMER";
 static boolean running;
 private static final String CHANNEL="companion";
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final GestureState gesture=new GestureState();
 private WindowManager wm; private FrameLayout petRoot;private TextView timerCaption;private ImageView pet; private HomeTargetView homeTarget;
 private WindowManager.LayoutParams petParams,homeTargetParams;
 private int currentCharacter=-1,lastManualCharacter=-1,decodedTarget;
 private RandomRotation randomRotation;
 private TapVoice voice;
 private AutoStop autoStop;private boolean timerInitialized;
 private RestCycle restCycle;private boolean resting,petVisible;
 private WanderMotion wanderMotion;private WanderDrawable wanderDrawable;private boolean wanderEnabled;private int wanderPose=3,wanderFrame;
 private OverlayVisibilityLoop visibilityLoop;
 private AnimatedImageDrawable animation; private SharedPreferences prefs;
 private Rect safe=new Rect(); private int screenWidth,screenHeight;
 private float downX,downY; private int originalX,originalY,slop; private boolean receiverRegistered;
 private AppOpsManager ops;
 private final AppOpsManager.OnOpChangedListener permissionListener=(op,pkg)->handler.post(()->{if(!Settings.canDrawOverlays(this))stopSelf();});
 private final BroadcastReceiver screenReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
  autoStop.setPaused(!getSystemService(PowerManager.class).isInteractive());
  if(!autoStop.check())return;
  cancelGesture();
  if(visibilityLoop!=null)visibilityLoop.refresh();
 }};
 private final Runnable hold=()->{
  if(pet!=null && gesture.canLongPress()){
   gesture.longPress();pet.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);showHomeTarget();
  }
 };
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(){super.onCreate();wm=getSystemService(WindowManager.class);prefs=getSharedPreferences("companion",MODE_PRIVATE);slop=ViewConfiguration.get(this).getScaledTouchSlop();
  voice=new TapVoice(this,prefs);
  autoStop=new AutoStop(new AutoStop.Environment(){public long now(){return SystemClock.elapsedRealtime();}public void postDelayed(Runnable r,long ms){handler.postDelayed(r,ms);}public void removeCallbacks(Runnable r){handler.removeCallbacks(r);}public void expire(){stopSelf();}});
  wanderMotion=new WanderMotion(new WanderMotion.Environment(){
   public long now(){return SystemClock.uptimeMillis();}
   public void postDelayed(Runnable task,long ms){handler.postDelayed(task,ms);}
   public void removeCallbacks(Runnable task){handler.removeCallbacks(task);}
   public void frame(float x,float y,int pose,int frame,boolean left){
    if(!wanderEnabled||pet==null||wanderDrawable==null)return;
    wanderPose=pose;wanderFrame=frame;wanderDrawable.setFrame(pose,frame,left);
    int nx=Math.round(x),ny=Math.round(y);
    if(nx!=petParams.x||ny!=petParams.y){petParams.x=nx;petParams.y=ny;updatePet();}
   }
  },new java.util.Random());
  randomRotation=new RandomRotation(new RandomRotation.Environment(){
   public long now(){return SystemClock.uptimeMillis();}
   public int currentCharacter(){return currentCharacter;}
   public boolean canSwitch(){return pet!=null&&petRoot.getVisibility()==View.VISIBLE&&!gesture.canMove()
     &&getSystemService(PowerManager.class).isInteractive()&&!getSystemService(KeyguardManager.class).isKeyguardLocked();}
   public void showCharacter(int value){
    try {displayCharacter(value);if(animation!=null&&petVisible&&!resting)animation.start();}
    catch(java.io.IOException e){android.util.Log.e("UsagiFloat","Cannot switch character",e);randomRotation.stop();stopSelf();}
   }
   public void postDelayed(Runnable task,long ms){handler.postDelayed(task,ms);}
   public void removeCallbacks(Runnable task){handler.removeCallbacks(task);}
  },new java.util.Random(),CharacterCatalog.NAMES.length);
  visibilityLoop=new OverlayVisibilityLoop(new OverlayVisibilityLoop.Environment(){
   public boolean isInteractive(){return getSystemService(PowerManager.class).isInteractive();}
   public boolean canShow(){return (!wanderEnabled||!MainActivity.showing)&&getSystemService(PowerManager.class).isInteractive()&&!getSystemService(KeyguardManager.class).isKeyguardLocked();}
   public void applyVisibility(boolean show){applyPetVisibility(show);}
   public void postDelayed(Runnable task,long delay){handler.postDelayed(task,delay);}
   public void removeCallbacks(Runnable task){handler.removeCallbacks(task);}
  });
  restCycle=new RestCycle(new RestCycle.Environment(){public long now(){return SystemClock.uptimeMillis();}public void setResting(boolean value){resting=value;syncPlayback();if(running)getSystemService(NotificationManager.class).notify(1,notification());}public void postDelayed(Runnable r,long ms){handler.postDelayed(r,ms);}public void removeCallbacks(Runnable r){handler.removeCallbacks(r);}});
  NotificationManager nm=getSystemService(NotificationManager.class);
  NotificationChannel ch=new NotificationChannel(CHANNEL,"우사기 재생",NotificationManager.IMPORTANCE_LOW);ch.setDescription("떠 있는 우사기 실행 및 종료");ch.setShowBadge(false);nm.createNotificationChannel(ch);
 }
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent!=null && STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
  // Establish foreground state even if permission was revoked between Activity and Service.
  Notification notification=notification();
  if(Build.VERSION.SDK_INT>=34)startForeground(1,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);else startForeground(1,notification);
  if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
  try{
   String action=intent==null?null:intent.getAction();
   if(!timerInitialized||START.equals(action)||TIMER.equals(action)){autoStop.arm(timerDuration());timerInitialized=true;}else if(!autoStop.check())return START_NOT_STICKY;
   voice.refresh();
   restCycle.configure(prefs.getBoolean("power_saving",false));
   if(pet==null)createPet();else resize();
   applyMode();visibilityLoop.start();
   randomRotation.configure(!wanderEnabled&&prefs.getBoolean("random_enabled",false),prefs.getInt("random_seconds",30),RandomSettingsDialog.pool(prefs));
  }catch(Exception e){android.util.Log.e("UsagiFloat","Cannot create overlay",e);Toast.makeText(this,"우사기를 표시하지 못했어요. 표시 권한을 확인해 주세요.",Toast.LENGTH_LONG).show();stopSelf();}
  return START_NOT_STICKY;
 }
 private long timerDuration(){if(!prefs.getBoolean("timer_enabled",false))return 0;long value=AutoStop.duration(prefs.getInt("timer_amount",30),prefs.getInt("timer_unit",0));return value>0?value:1_800_000;}
 private Notification notification(){
  PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,CompanionService.class).setAction(STOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_notification).setContentTitle(resting?"우사기가 절약 모드로 쉬고 있어요":"우사기가 둥둥 떠 있어요").setContentText(prefs.getBoolean("timer_enabled",false)?"자동 종료 설정 · "+prefs.getInt("timer_amount",30)+new String[]{"분","시간","일"}[GestureState.clamp(prefs.getInt("timer_unit",0),0,2)]:"끌어서 이동 · 꾹 눌러 집으로 종료").setContentIntent(open).setOngoing(true).setCategory(Notification.CATEGORY_SERVICE).addAction(new Notification.Action.Builder(null,"종료",stop).build()).build();
 }
 private WindowManager.LayoutParams params(int width,int height,boolean touchable){
  int flags=WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
  if(!touchable)flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(width,height,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,flags,PixelFormat.TRANSLUCENT);
  p.gravity=Gravity.TOP|Gravity.LEFT;
  p.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
  if(Build.VERSION.SDK_INT>=30)p.setFitInsetsTypes(0);
  return p;
 }
 private void updateBounds(){
  if(Build.VERSION.SDK_INT>=30){WindowMetrics metrics=wm.getMaximumWindowMetrics();Rect b=metrics.getBounds();screenWidth=b.width();screenHeight=b.height();
   Insets insets=metrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
   safe.set(insets.left,insets.top,screenWidth-insets.right,screenHeight-insets.bottom);
  }else{android.util.DisplayMetrics m=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(m);screenWidth=m.widthPixels;screenHeight=m.heightPixels;
   int status=getResources().getIdentifier("status_bar_height","dimen","android"),nav=getResources().getIdentifier("navigation_bar_height","dimen","android");
   safe.set(0,status==0?dp(24):getResources().getDimensionPixelSize(status),screenWidth,screenHeight-(nav==0?dp(48):getResources().getDimensionPixelSize(nav)));
  }
 }
 private void createPet() throws java.io.IOException {
  updateBounds();int size=petSide();
  petParams=params(size,size+captionHeight(),true);petParams.setTitle("Usagi companion");
  petParams.x=prefs.getInt("x",safe.right-size-dp(16));petParams.y=prefs.getInt("y",safe.top+dp(160));clampPosition();
  petRoot=new FrameLayout(this);
  pet=new ImageView(this);pet.setContentDescription("우사기 · 드래그로 이동, 길게 눌러 종료");pet.setScaleType(ImageView.ScaleType.FIT_CENTER);
  petRoot.addView(pet,new FrameLayout.LayoutParams(size,size));
  timerCaption=new TextView(this);timerCaption.setTextSize(10);timerCaption.setTextColor(0xfff9f6ef);timerCaption.setGravity(Gravity.CENTER);timerCaption.setIncludeFontPadding(false);timerCaption.setSingleLine(true);timerCaption.setEllipsize(android.text.TextUtils.TruncateAt.END);timerCaption.setPadding(dp(5),0,dp(5),0);timerCaption.setMaxWidth(size-dp(8));timerCaption.setAlpha(.82f);
  android.graphics.drawable.GradientDrawable pill=new android.graphics.drawable.GradientDrawable();pill.setColor(0x993b3329);pill.setCornerRadius(dp(9));timerCaption.setBackground(pill);
  petRoot.addView(timerCaption,new FrameLayout.LayoutParams(-2,dp(18),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL));
  updateCaption();updateCharacter();pet.setOnTouchListener(this::touch);timerCaption.setOnTouchListener(this::touch);
  wm.addView(petRoot,petParams);running=true;
  IntentFilter filter=new IntentFilter();filter.addAction(Intent.ACTION_SCREEN_OFF);filter.addAction(Intent.ACTION_SCREEN_ON);filter.addAction(Intent.ACTION_USER_PRESENT);
  if(Build.VERSION.SDK_INT>=33)registerReceiver(screenReceiver,filter,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(screenReceiver,filter);receiverRegistered=true;
  ops=getSystemService(AppOpsManager.class);ops.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,getPackageName(),permissionListener);
  visibilityLoop.start();
 }
 private boolean captionWanted(){return autoStop!=null&&autoStop.enabled()&&prefs.getBoolean("timer_caption",true);}
 private int captionHeight(){return captionWanted()?dp(20):0;}
 private void updateCaption(){if(timerCaption==null)return;boolean show=captionWanted();timerCaption.setVisibility(show?View.VISIBLE:View.GONE);if(show){String text=CountdownText.format(autoStop.remaining());if(!text.contentEquals(timerCaption.getText())){timerCaption.setText(text);timerCaption.setContentDescription("자동 종료까지 "+text);}}}
 private void syncWanderPosition(){if(petParams!=null&&wanderMotion!=null)wanderMotion.geometry(petParams.x,petParams.y,safe.left,safe.top,safe.right-petParams.width,safe.bottom-petParams.height);}
 private void applyMode() throws java.io.IOException {
  boolean enabled=prefs.getBoolean("wander_enabled",false);
  if(enabled!=wanderEnabled){
   wanderEnabled=enabled;
   if(enabled){
    if(animation!=null){animation.stop();animation=null;}
    if(wanderDrawable==null){wanderDrawable=new WanderDrawable(getResources());wanderDrawable.prepareForPlayback();}
    pet.setImageDrawable(wanderDrawable);pet.setContentDescription("산책하는 우사기 · 잡아서 이동, 길게 눌러 집으로 보내기");
   }else {wanderMotion.configure(false,dp(40));wanderDrawable=null;currentCharacter=-1;}
  }
  syncWanderPosition();
  int speed=prefs.getInt("wander_speed",1);int[] speeds={32,56,88};
  wanderMotion.configure(enabled,dp(speeds[GestureState.clamp(speed,0,2)]));
  if(!enabled)updateCharacter();
 }
 private void updateCharacter() throws java.io.IOException {
  if(wanderEnabled)return;
  int selected=CharacterCatalog.normalize(prefs.getInt("character",0));
  boolean manualChanged=lastManualCharacter!=selected;
  // Random changes never overwrite the user's fixed-character choice.
  if(prefs.getBoolean("random_enabled",false)){
   int[] pool=RandomSettingsDialog.pool(prefs);
   if(currentCharacter<0||!RandomPool.contains(pool,currentCharacter))displayCharacter(pool[new java.util.Random().nextInt(pool.length)]);
   else if(manualChanged&&RandomPool.contains(pool,selected))displayCharacter(selected);
   else if(decodedTarget!=petParams.width)displayCharacter(currentCharacter);
  }else if(currentCharacter<0||manualChanged||currentCharacter!=selected)displayCharacter(selected);
  else if(decodedTarget!=petParams.width)displayCharacter(currentCharacter);
  lastManualCharacter=selected;
  if(manualChanged&&randomRotation!=null)randomRotation.restartInterval();
 }
 private void displayCharacter(int selected) throws java.io.IOException {
  if(selected==currentCharacter&&decodedTarget==petParams.width)return;
  AnimatedImageDrawable next=Art.load(this,pet,selected,petParams.width);
  decodedTarget=petParams.width;
  if(animation!=null)animation.stop();
  animation=next;currentCharacter=selected;
  pet.setContentDescription(CharacterCatalog.NAMES[selected]+" · 드래그로 이동, 길게 눌러 집으로 보내기");
 }
 private void syncPlayback(){
  boolean play=petVisible&&!resting;
  if(randomRotation!=null)randomRotation.setVisible(play);
  if(wanderMotion!=null)wanderMotion.setVisible(play);
  if(animation!=null){if(play&&!animation.isRunning())animation.start();else if(!play&&animation.isRunning())animation.stop();}
 }
 private void applyPetVisibility(boolean show){
  autoStop.setPaused(!getSystemService(PowerManager.class).isInteractive());
  if(pet==null||!autoStop.check())return;
  petVisible=show;
  if(!show){voice.stop();restCycle.setVisible(false);syncPlayback();cancelGesture();if(petRoot.getVisibility()!=View.INVISIBLE)petRoot.setVisibility(View.INVISIBLE);return;}
  if(petRoot.getVisibility()!=View.VISIBLE){
   updateBounds();clampPosition();syncWanderPosition();if(!updatePet())return;
   petRoot.setVisibility(View.VISIBLE);pet.requestLayout();pet.invalidate();
  }
  restCycle.setVisible(true);syncPlayback();updateCaption();
 }
 private void clampPosition(){petParams.x=GestureState.clamp(petParams.x,safe.left,safe.right-petParams.width);petParams.y=GestureState.clamp(petParams.y,safe.top,safe.bottom-petParams.height);}
 private void savePosition(){if(petParams!=null)prefs.edit().putInt("x",petParams.x).putInt("y",petParams.y).apply();}
 private boolean updatePet(){
  try{
   if(pet!=null && pet.isAttachedToWindow()){
    wm.updateViewLayout(petRoot,petParams);
    // Relayout can leave AnimatedImageDrawable running without another draw.
    // Schedule on the next display frame; restarting would reset the animation.
    pet.postInvalidateOnAnimation();
   }
   return true;
  }catch(RuntimeException e){stopSelf();return false;}
 }
 private boolean touch(View view,MotionEvent e){
  switch(e.getActionMasked()){
   case MotionEvent.ACTION_DOWN:
    downX=e.getRawX();downY=e.getRawY();originalX=petParams.x;originalY=petParams.y;
    gesture.down(downX,downY);restCycle.setHeld(true);wanderMotion.setHeld(true);handler.postDelayed(hold,500);return true;
   case MotionEvent.ACTION_MOVE:
    if(!gesture.canMove())return true;
    gesture.move(e.getRawX(),e.getRawY(),slop);if(!gesture.canLongPress())handler.removeCallbacks(hold);
    petParams.x=originalX+Math.round(e.getRawX()-downX);petParams.y=originalY+Math.round(e.getRawY()-downY);clampPosition();updatePet();
    if(homeTarget!=null)homeTarget.setActive(overHomeTarget());return true;
   case MotionEvent.ACTION_UP:
    gesture.move(e.getRawX(),e.getRawY(),slop);boolean tap=VoicePolicy.tap(gesture.canLongPress(),e.getEventTime()-e.getDownTime())&&view==pet;
    handler.removeCallbacks(hold);boolean remove=gesture.release(overHomeTarget());hideHomeTarget();savePosition();
    if(remove)stopSelf();else {syncWanderPosition();restCycle.setHeld(false);wanderMotion.setHeld(false);view.postInvalidateOnAnimation();view.performClick();if(tap&&petVisible)voice.play();}return true;
   case MotionEvent.ACTION_CANCEL:
   case MotionEvent.ACTION_POINTER_DOWN:
    cancelGesture();savePosition();return true;
   default:return true;
  }
 }
 private void showHomeTarget(){
  if(homeTarget!=null)return;int size=dp(112);homeTargetParams=params(size,size,false);homeTargetParams.setTitle("Usagi home");homeTargetParams.x=(screenWidth-size)/2;homeTargetParams.y=safe.bottom-size-dp(16);
  homeTarget=new HomeTargetView(this);try{wm.addView(homeTarget,homeTargetParams);}catch(RuntimeException e){homeTarget=null;stopSelf();}
 }
 private boolean overHomeTarget(){if(homeTarget==null || !gesture.isHeld())return false;
  // The character centre, not the finger offset, determines the drop target.
  int[] location=new int[2];homeTarget.getLocationOnScreen(location);
  return GestureState.insideCircle(petParams.x+petParams.width/2f,petParams.y+petParams.width/2f,location[0]+homeTarget.getWidth()/2f,location[1]+homeTarget.getHeight()*.42f,dp(54));
 }
 private void hideHomeTarget(){if(homeTarget!=null){try{if(homeTarget.isAttachedToWindow())wm.removeView(homeTarget);}catch(IllegalArgumentException ignored){}homeTarget=null;}}
 private void cancelGesture(){if(restCycle!=null)restCycle.setHeld(false);handler.removeCallbacks(hold);gesture.cancel();hideHomeTarget();syncWanderPosition();if(wanderMotion!=null)wanderMotion.setHeld(false);}
 private int petSide(){return PetSize.side(prefs.getInt("size",120),getResources().getDisplayMetrics().density,prefs.getBoolean("wander_enabled",false),safe.width(),safe.height(),captionHeight());}
 private void resize(){cancelGesture();updateBounds();int size=petSide();petParams.width=size;petParams.height=size+captionHeight();if(pet.getLayoutParams().width!=size)pet.setLayoutParams(new FrameLayout.LayoutParams(size,size));timerCaption.setMaxWidth(size-dp(8));updateCaption();clampPosition();syncWanderPosition();updatePet();savePosition();}
 @Override public void onConfigurationChanged(Configuration config){super.onConfigurationChanged(config);if(pet!=null)resize();}
 @Override public void onDestroy(){running=false;if(voice!=null)voice.close();if(restCycle!=null)restCycle.stop();if(autoStop!=null)autoStop.stop();if(wanderMotion!=null)wanderMotion.stop();if(randomRotation!=null)randomRotation.stop();if(visibilityLoop!=null)visibilityLoop.stop();handler.removeCallbacksAndMessages(null);gesture.cancel();hideHomeTarget();savePosition();
  if(animation!=null){animation.stop();animation=null;}
  if(pet!=null){pet.setOnTouchListener(null);pet.setImageDrawable(null);try{if(petRoot.isAttachedToWindow())wm.removeView(petRoot);}catch(IllegalArgumentException ignored){}pet=null;petRoot=null;timerCaption=null;}
  if(receiverRegistered)unregisterReceiver(screenReceiver);if(ops!=null)ops.stopWatchingMode(permissionListener);
  stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();
 }
 @Override protected void dump(java.io.FileDescriptor fd,java.io.PrintWriter writer,String[] args){
  if(voice!=null)voice.dump(writer);
  writer.println("voiceInSilent="+prefs.getBoolean("voice_in_silent",false));writer.println("voiceEnabled="+prefs.getBoolean("tap_voice",false));writer.println("voiceAllowed="+(voice!=null&&voice.allowed()));writer.println("voiceActive="+(voice!=null&&voice.active()));writer.println("voicePlayed="+(voice==null?0:voice.played));
  handler.dump(new android.util.PrintWriterPrinter(writer),"appHandler ");
  writer.println("interactive="+getSystemService(PowerManager.class).isInteractive());
  writer.println("timerCaptionVisible="+(timerCaption!=null&&petVisible&&timerCaption.getVisibility()==View.VISIBLE));
  writer.println("timerCaptionText="+(timerCaption==null?"":timerCaption.getText()));
  writer.println("powerSaving="+prefs.getBoolean("power_saving",false));
  writer.println("resting="+resting);
  writer.println("hardwareAccelerated="+(pet!=null&&pet.isHardwareAccelerated()));
  writer.println("animationWidth="+(animation==null?0:animation.getIntrinsicWidth()));
  writer.println("animationHeight="+(animation==null?0:animation.getIntrinsicHeight()));
  writer.println("wanderAtlasRetained="+(wanderDrawable!=null));
  writer.println("timerEnabled="+autoStop.enabled());
  writer.println("timerRemainingMillis="+autoStop.remaining());
  writer.println("character="+currentCharacter);
  writer.println("random="+(!wanderEnabled&&prefs.getBoolean("random_enabled",false)));
  writer.println("wander="+wanderEnabled);
  writer.println("wanderPose="+wanderPose);
  writer.println("wanderFrame="+wanderFrame);
  writer.println("intervalSeconds="+RandomRotation.validSeconds(prefs.getInt("random_seconds",30)));
  writer.println("visible="+(pet!=null&&petRoot.getVisibility()==View.VISIBLE));
  writer.println("animating="+(animation!=null&&animation.isRunning()));
 }
 @Override public IBinder onBind(Intent intent){return null;}
}
