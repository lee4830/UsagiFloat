package kr.personal.usagifloat;
import java.util.Random;
/** Bounded motion with a single uptime callback; hidden/held companions do no work. */
final class WanderMotion {
 static final int WALK=0,LOOK=1,WIGGLE=2,REST=3;
 interface Environment {long now();void frame(float x,float y,int pose,int frame,boolean left);void postDelayed(Runnable r,long ms);void removeCallbacks(Runnable r);}
 private final Environment env;private final Random random;private final Runnable task=this::tick;
 private boolean enabled,visible,held,stopped,left;private float x,y,minX,minY,maxX,maxY,targetX,targetY,speed=40,velocity;
 private final int[] regions={0,1,2,3,4,5,6,7,8},actions={LOOK,WIGGLE,REST};private int regionIndex=9,actionIndex=3,lastRegion=-1;private boolean dropRest;
 private int pose=WALK,nextPose;private boolean arrived;private long elapsed,lastTime;private long idleDuration=1800;
 WanderMotion(Environment env,Random random){this.env=env;this.random=random;}
 void geometry(float x,float y,float minX,float minY,float maxX,float maxY){
  this.minX=minX;this.minY=minY;this.maxX=Math.max(minX,maxX);this.maxY=Math.max(minY,maxY);
  this.x=clamp(x,minX,this.maxX);this.y=clamp(y,minY,this.maxY);
  targetX=clamp(targetX,minX,this.maxX);targetY=clamp(targetY,minY,this.maxY);
 }
 void configure(boolean value,float speed){
  if(stopped)return;this.speed=Float.isFinite(speed)&&speed>0?speed:40;
  if(enabled==value)return;enabled=value;cancel();
  if(enabled){beginWalk();resume();}
 }
 void setVisible(boolean value){if(stopped||visible==value)return;visible=value;cancel();resume();}
 void setHeld(boolean value){
  if(stopped||held==value)return;held=value;cancel();
  if(!held){pose=REST;dropRest=true;elapsed=0;idleDuration=2000;resume();}
 }
 void stop(){stopped=true;enabled=false;cancel();}
 private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
 private boolean active(){return enabled&&visible&&!held&&!stopped;}
 private void cancel(){env.removeCallbacks(task);}
 private void resume(){if(active()){lastTime=env.now();env.postDelayed(task,33);}}
 private void shuffle(int[] values){for(int i=values.length-1;i>0;i--){int j=random.nextInt(i+1),v=values[i];values[i]=values[j];values[j]=v;}}
 private void beginWalk(){
  pose=WALK;elapsed=0;dropRest=false;arrived=false;velocity=0;
  if(regionIndex==regions.length){shuffle(regions);regionIndex=0;if(regions[0]==lastRegion){int v=regions[0];regions[0]=regions[1];regions[1]=v;}}
  int region=regions[regionIndex++];lastRegion=region;
  // A shuffled3x3 itinerary prevents random destinations clustering in one area.
  float u=.08f+(region%3)*.42f+(random.nextFloat()-.5f)*.06f;
  float v=.08f+(region/3)*.42f+(random.nextFloat()-.5f)*.06f;
  targetX=minX+u*(maxX-minX);targetY=minY+v*(maxY-minY);
  if(Math.abs(targetX-x)>.5)left=targetX<x;
 }
 private float pace(){
  // Continuous periodic rhythm: eager dash, settle, then another small dash.
  float t=(elapsed%1300)/1300f;
  return .18f+2.42f*(float)Math.pow(Math.sin(Math.PI*t),2);
 }
 private void beginAction(){
  if(actionIndex==actions.length){shuffle(actions);actionIndex=0;}
  nextPose=actions[actionIndex++];pose=3+nextPose;elapsed=0;velocity=0;
 }
 private void finishBridge(){
  if(nextPose==WALK)beginWalk();
  else {pose=nextPose;elapsed=0;idleDuration=MotionClips.duration(pose);}
 }
 private void tick(){
  if(!active())return;
  long now=env.now(),dt=Math.max(0,Math.min(100,now-lastTime));lastTime=now;elapsed+=dt;
  if(pose==WALK){
   double distance=Math.hypot(targetX-x,targetY-y);
   float acceleration=speed*8,seconds=dt/1000f;
   float desired=Math.min(speed*pace(),(float)Math.sqrt(2*acceleration*distance));
   velocity+=clamp(desired-velocity,-acceleration*seconds,acceleration*seconds);
   float step=Math.min((float)distance,velocity*seconds);
   if(distance<=step||distance<.1){x=targetX;y=targetY;arrived=true;velocity=0;}
   else {x+=(targetX-x)*step/distance;y+=(targetY-y)*step/distance;}
   // End on the last source pose, so the precomputed bridge matches its input.
   if(arrived&&elapsed/MotionClips.duration(WALK)>(elapsed-dt)/MotionClips.duration(WALK))beginAction();
  }else if(pose>=4){if(elapsed>=MotionClips.duration(pose))finishBridge();}
  else if(elapsed>=idleDuration){nextPose=WALK;pose=6+pose;elapsed=0;dropRest=false;}
  int frame=dropRest?MotionClips.count(REST)-1:MotionClips.frame(pose,elapsed);
  env.frame(clamp(x,minX,maxX),clamp(y,minY,maxY),pose,frame,left);
  if(active())env.postDelayed(task,33);
 }
}
