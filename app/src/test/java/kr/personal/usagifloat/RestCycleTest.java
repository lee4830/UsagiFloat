package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class RestCycleTest {
 static class Device implements RestCycle.Environment {long now,due;Runnable task;boolean rest;int changes;
 public long now(){return now;}public void setResting(boolean value){rest=value;changes++;}public void postDelayed(Runnable r,long ms){assertNull(task);task=r;due=now+ms;}public void removeCallbacks(Runnable r){if(task==r)task=null;}
 void advance(long ms){now+=ms;if(task!=null&&now>=due){Runnable r=task;task=null;r.run();}}
 }
 @Test public void disabledSchedulesNothing(){Device d=new Device();RestCycle c=new RestCycle(d);c.configure(false);c.setVisible(true);assertNull(d.task);assertFalse(d.rest);}
 @Test public void thirtySecondsPlayThenThirtySecondsRest(){Device d=new Device();RestCycle c=new RestCycle(d);c.configure(true);c.setVisible(true);d.advance(29999);assertFalse(d.rest);d.advance(1);assertTrue(d.rest);d.advance(30000);assertFalse(d.rest);}
 @Test public void touchingImmediatelyWakesAndDefersRestUntilRelease(){Device d=new Device();RestCycle c=new RestCycle(d);c.configure(true);c.setVisible(true);d.advance(30000);c.setHeld(true);assertFalse(d.rest);assertNull(d.task);d.advance(60000);assertFalse(d.rest);c.setHeld(false);d.advance(29999);assertFalse(d.rest);d.advance(1);assertTrue(d.rest);}
 @Test public void hiddenAndStoppedDoNotScheduleOrResurrect(){Device d=new Device();RestCycle c=new RestCycle(d);c.configure(true);c.setVisible(true);Runnable stale=d.task;c.setVisible(false);assertNull(d.task);stale.run();assertNull(d.task);c.setVisible(true);assertFalse(d.rest);stale=d.task;c.stop();stale.run();assertNull(d.task);}
 @Test public void ordinarySettingsDoNotRestartCycle(){Device d=new Device();RestCycle c=new RestCycle(d);c.configure(true);c.setVisible(true);d.advance(20000);c.configure(true);c.setVisible(true);d.advance(10000);assertTrue(d.rest);}
}
