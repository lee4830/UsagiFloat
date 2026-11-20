package kr.personal.usagifloat;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public class RandomRotationTest {
 static class Device implements RandomRotation.Environment {
  long now,due;Runnable pending;int current=0,changes;boolean canSwitch=true;
  public long now(){return now;}
  public int currentCharacter(){return current;}
  public boolean canSwitch(){return canSwitch;}
  public void showCharacter(int value){assertNotEquals(current,value);current=value;changes++;}
  public void postDelayed(Runnable task,long ms){assertNull("single scheduled rotation",pending);pending=task;due=now+ms;}
  public void removeCallbacks(Runnable task){if(pending==task)pending=null;}
  void advance(long ms){long target=now+ms;while(pending!=null&&due<=target){now=due;Runnable r=pending;pending=null;r.run();}now=target;}
 }
 private RandomRotation start(Device d,int seconds){RandomRotation r=new RandomRotation(d,new Random(42),5);r.configure(true,seconds);r.setVisible(true);return r;}
 @Test public void changesAtIntervalNotBefore(){Device d=new Device();start(d,5);d.advance(4999);assertEquals(0,d.changes);d.advance(1);assertEquals(1,d.changes);d.advance(10000);assertEquals(3,d.changes);}
 @Test public void pausePreservesRemainingTime(){Device d=new Device();RandomRotation r=start(d,10);d.advance(4000);r.setVisible(false);d.advance(60000);assertEquals(0,d.changes);r.setVisible(true);d.advance(5999);assertEquals(0,d.changes);d.advance(1);assertEquals(1,d.changes);}
 @Test public void intervalEditReschedulesAndIdenticalUpdateDoesNotReset(){Device d=new Device();RandomRotation r=start(d,10);d.advance(2000);r.configure(true,5);d.advance(2000);r.configure(true,5);d.advance(3000);assertEquals(1,d.changes);}
 @Test public void disabledAndStoppedCallbacksCannotSwitch(){Device d=new Device();RandomRotation r=start(d,1);Runnable stale=d.pending;r.configure(false,1);stale.run();d.advance(5000);assertEquals(0,d.changes);r.configure(true,1);stale=d.pending;r.stop();stale.run();r.setVisible(true);d.advance(5000);assertEquals(0,d.changes);assertNull(d.pending);}
 @Test public void holdingOrDraggingDefersSwitch(){Device d=new Device();start(d,1);d.canSwitch=false;d.advance(3000);assertEquals(0,d.changes);d.canSwitch=true;d.advance(250);assertEquals(1,d.changes);}
 @Test public void allOtherCharactersAreEligibleAndNeverRepeat(){Device d=new Device();start(d,1);boolean[] seen=new boolean[5];for(int i=0;i<100;i++){int old=d.current;d.advance(1000);assertNotEquals(old,d.current);seen[d.current]=true;}for(boolean value:seen)assertTrue(value);}
 @Test public void invalidIntervalsFallBackToDefault(){assertEquals(30,RandomRotation.validSeconds(0));assertEquals(30,RandomRotation.validSeconds(3601));assertEquals(1,RandomRotation.validSeconds(1));assertEquals(3600,RandomRotation.validSeconds(3600));}
 @Test public void manualSelectionRestartsFullInterval(){Device d=new Device();RandomRotation r=start(d,5);d.advance(4000);r.restartInterval();d.advance(1000);assertEquals(0,d.changes);d.advance(4000);assertEquals(1,d.changes);}
 @Test public void subsetNeverShowsExcludedCharacters(){Device d=new Device();RandomRotation r=start(d,1);r.configure(true,1,new int[]{1,4});for(int i=0;i<50;i++){d.advance(1000);assertTrue(d.current==1||d.current==4);}}
 @Test public void singletonHasNoRotationTask(){Device d=new Device();RandomRotation r=start(d,1);r.configure(true,1,new int[]{3});assertNull(d.pending);d.advance(10000);assertEquals(0,d.changes);}
 @Test public void unchangedPoolDoesNotResetDeadline(){Device d=new Device();RandomRotation r=start(d,5);r.configure(true,5,new int[]{1,4});d.advance(4000);r.configure(true,5,new int[]{1,4});d.advance(1000);assertEquals(1,d.changes);}
}
