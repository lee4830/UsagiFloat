package kr.personal.usagifloat;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public class WanderMotionTest {
 static class Device implements WanderMotion.Environment {
  long now,due;Runnable pending;float x=100,y=200;int calls,pose,lastFrame;boolean[] poses=new boolean[4];
  public long now(){return now;}
  public void frame(float x,float y,int pose,int frame,boolean left){this.x=x;this.y=y;this.pose=pose;this.lastFrame=frame;if(pose<4)poses[pose]=true;calls++;assertTrue(frame>=0&&frame<MotionClips.count(pose));}
  public void postDelayed(Runnable r,long ms){assertNull("only one motion callback",pending);pending=r;due=now+ms;}
  public void removeCallbacks(Runnable r){if(pending==r)pending=null;}
  void advance(long ms){long end=now+ms;while(pending!=null&&due<=end){now=due;Runnable r=pending;pending=null;r.run();}now=end;}
 }
 WanderMotion start(Device d,float speed){WanderMotion w=new WanderMotion(d,new Random(42));w.geometry(100,200,0,50,300,700);w.configure(true,speed);w.setVisible(true);return w;}
 @Test public void accelerationIsBoundedDuringTravel(){
  Device d=new Device();start(d,40);double previous=0;
  for(int i=0;i<60;i++){float x=d.x,y=d.y;d.advance(33);double velocity=Math.hypot(d.x-x,d.y-y)/.033;
   assertTrue("no abrupt velocity jump: "+Math.abs(velocity-previous),Math.abs(velocity-previous)<=40*8*.033+.05);previous=velocity;}
 }
 @Test public void outgoingBridgeFollowsFinalWalkFrame(){
  Device d=new Device();start(d,100);int previousPose=d.pose,previousFrame=d.lastFrame,bridges=0;
  for(int i=0;i<12000;i++){d.advance(33);if(previousPose==0&&d.pose>=4){assertEquals(11,previousFrame);bridges++;}previousPose=d.pose;previousFrame=d.lastFrame;}
  assertTrue(bridges>0);
 }
 @Test public void transitionsPlayAndCanBeSuspended(){
  Device d=new Device();WanderMotion w=start(d,100);boolean transition=false;
  for(int i=0;i<20000;i++){d.advance(33);if(d.pose>=4){transition=true;break;}}
  assertTrue("short bridge between poses",transition);w.setVisible(false);int calls=d.calls;d.advance(10000);assertEquals(calls,d.calls);assertNull(d.pending);
  w.setVisible(true);d.advance(33);assertEquals(calls+1,d.calls);
 }
 @Test public void movesAndRemainsInsideScreen(){Device d=new Device();start(d,40);d.advance(1000);assertTrue(Math.hypot(d.x-100,d.y-200)>5);for(int i=0;i<1000;i++){d.advance(100);assertTrue(d.x>=0&&d.x<=300&&d.y>=50&&d.y<=700);}assertTrue(d.calls>100);}
 @Test public void alternatesWalkingWithAllIdleBehaviors(){Device d=new Device();start(d,100);d.advance(300000);for(boolean seen:d.poses)assertTrue(seen);}
 @Test public void grabPausesAndReleaseWaitsAtDroppedPosition(){Device d=new Device();WanderMotion w=start(d,40);d.advance(500);w.setHeld(true);int before=d.calls;d.advance(10000);assertEquals(before,d.calls);assertNull(d.pending);w.geometry(250,400,0,50,300,700);w.setHeld(false);d.advance(1500);assertEquals(250,d.x,.01);assertEquals(400,d.y,.01);d.advance(3000);assertTrue(Math.hypot(d.x-250,d.y-400)>5);}
 @Test public void hiddenTimeDoesNotCauseTeleport(){Device d=new Device();WanderMotion w=start(d,40);d.advance(500);float x=d.x,y=d.y;w.setVisible(false);int calls=d.calls;d.advance(60000);assertEquals(calls,d.calls);assertNull(d.pending);w.setVisible(true);d.advance(33);assertTrue(Math.hypot(d.x-x,d.y-y)<=4.2);}
 @Test public void disableAndStopCancelStaleWork(){Device d=new Device();WanderMotion w=start(d,40);d.advance(200);Runnable stale=d.pending;w.configure(false,40);int calls=d.calls;stale.run();d.advance(5000);assertEquals(calls,d.calls);assertNull(d.pending);w.configure(true,40);stale=d.pending;w.stop();calls=d.calls;stale.run();w.configure(true,40);w.setVisible(true);d.advance(5000);assertEquals(calls,d.calls);assertNull(d.pending);}
 @Test public void higherSpeedMovesFurtherAlongSameInitialPath(){Device a=new Device(),b=new Device();start(a,20);start(b,60);a.advance(500);b.advance(500);assertTrue(Math.hypot(b.x-100,b.y-200)>Math.hypot(a.x-100,a.y-200)*2);}
 @Test public void resizedBoundsClampPositionAndDestination(){Device d=new Device();WanderMotion w=start(d,40);d.advance(1000);w.geometry(500,900,0,0,80,90);for(int i=0;i<100;i++){d.advance(100);assertTrue(d.x>=0&&d.x<=80&&d.y>=0&&d.y<=90);}}
 @Test public void smallerThanCharacterViewportIsSafe(){Device d=new Device();WanderMotion w=start(d,40);w.geometry(20,30,0,0,-10,-20);d.advance(10000);assertEquals(0,d.x,.01);assertEquals(0,d.y,.01);}
 @Test public void everyScreenRegionIsVisitedEvenWithRepetitiveRandomNumbers(){
  Device d=new Device();WanderMotion w=new WanderMotion(d,new Random(1){@Override public float nextFloat(){return .5f;}@Override public int nextInt(int n){return 0;}});
  w.geometry(150,350,0,0,300,700);w.configure(true,100);w.setVisible(true);boolean[] regions=new boolean[9];
  for(int i=0;i<1800;i++){d.advance(100);int col=Math.min(2,(int)(d.x/100)),row=Math.min(2,(int)(d.y/(700f/3)));regions[row*3+col]=true;}
  for(boolean visited:regions)assertTrue("all9 regions should be explored",visited);
 }
 @Test public void movementHasBurstsInsteadOfConstantSpeed(){
  Device d=new Device();start(d,40);double fastest=0,slowest=Double.MAX_VALUE;
  for(int i=0;i<30;i++){float x=d.x,y=d.y;d.advance(66);if(d.pose==WanderMotion.WALK){double step=Math.hypot(d.x-x,d.y-y);if(step>.01){fastest=Math.max(fastest,step);slowest=Math.min(slowest,step);}}}
  assertTrue("noticeable stop-and-go rhythm",fastest>slowest*1.8);
 }
 @Test public void originalFramesRunAtThirtyFpsIndependentOfTravelSpeed(){
  Device a=new Device(),b=new Device();start(a,20);start(b,60);a.advance(198);b.advance(198);
  assertEquals(5,a.lastFrame);assertEquals(a.lastFrame,b.lastFrame);
 }
 @Test public void danceClipIsNotCutOffBeforeOriginalDuration(){
  Device d=new Device();start(d,100);long run=0,longest=0;
  for(int i=0;i<12000;i++){d.advance(33);if(d.pose==WanderMotion.WIGGLE){run+=33;longest=Math.max(longest,run);}else run=0;}
  assertTrue("2600ms dance must play completely",longest>=2574);
 }
}
