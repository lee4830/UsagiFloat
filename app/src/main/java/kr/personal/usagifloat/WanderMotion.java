package kr.personal.usagifloat;
import java.util.Random;
/** Bounded motion with a single uptime callback; hidden/held companions do no work. */
final class WanderMotion {
 static final int WALK=0,LOOK=1,WIGGLE=2,REST=3;
 interface Environment {long now();void frame(float x,float y,int pose,int frame,boolean left);void postDelayed(Runnable r,long ms);void removeCallbacks(Runnable r);}
 private final Environment env;private final Random random;private final Runnable task=this::tick;
 private boolean enabled,visible,held,stopped,left;private float x,y,minX,minY,maxX,maxY,targetX,targetY,speed=40;
 private final int[] regions={0,1,2,3,4,5,6,7,8},actions={LOOK,WIGGLE,REST};private int regionIndex=9,actionIndex=3,lastRegion=-1;private boolean dropRest;
 private int pose=WALK;private long elapsed,lastTime;private long idleDuration=1800;
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
  pose=WALK;elapsed=0;dropRest=false;
  if(regionIndex==regions.length){shuffle(regions);regionIndex=0;if(regions[0]==lastRegion){int v=regions[0];regions[0]=regions[1];regions[1]=v;}}
  int region=regions[regionIndex++];lastRegion=region;
  // A shuffled3x3 itinerary prevents random destinations clustering in one area.
  float u=.08f+(region%3)*.42f+(random.nextFloat()-.5f)*.06f;
  float v=.08f+(region/3)*.42f+(random.nextFloat()-.5f)*.06f;
  targetX=minX+u*(maxX-minX);targetY=minY+v*(maxY-minY);
  if(Math.abs(targetX-x)>.5)left=targetX<x;
 }
 private float pace(){long beat=elapsed%1300;return beat<150?.18f:beat<650?2.6f:beat<900?1f:beat<1030?.12f:1.8f;}
 private void beginAction(){
  if(actionIndex==actions.length){shuffle(actions);actionIndex=0;}
  pose=actions[actionIndex++];elapsed=0;idleDuration=MotionClips.duration(pose);
 }
 private void tick(){
  if(!active())return;
  long now=env.now(),dt=Math.max(0,Math.min(100,now-lastTime));lastTime=now;elapsed+=dt;
  if(pose==WALK){
   double distance=Math.hypot(targetX-x,targetY-y);float step=speed*pace()*dt/1000f;
   if(distance<=step||distance<.1){x=targetX;y=targetY;beginAction();}
   else {x+=(targetX-x)*step/distance;y+=(targetY-y)*step/distance;}
  }else if(elapsed>=idleDuration)beginWalk();
  int frame=dropRest?MotionClips.count(REST)-1:MotionClips.frame(pose,elapsed);
  env.frame(clamp(x,minX,maxX),clamp(y,minY,maxY),pose,frame,left);
  if(active())env.postDelayed(task,33);
 }
}
