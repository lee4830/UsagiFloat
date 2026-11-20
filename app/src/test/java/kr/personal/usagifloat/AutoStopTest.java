package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class AutoStopTest {
 static class Device implements AutoStop.Environment {
  long now,due;int expired;Runnable task;
  public long now(){return now;}
  public void postDelayed(Runnable r,long ms){assertNull(task);task=r;due=now+ms;}
  public void removeCallbacks(Runnable r){if(task==r)task=null;}
  public void expire(){expired++;}
  void advance(long ms){now+=ms;if(task!=null&&now>=due){Runnable r=task;task=null;r.run();}}
 }
 @Test public void unlimitedQueuesNoWork(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(0);d.advance(9999999);assertNull(d.task);assertEquals(0,d.expired);}
 @Test public void deadlineExpiresExactlyOnce(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(60000);d.advance(59999);assertEquals(0,d.expired);d.advance(1);assertEquals(1,d.expired);t.check();assertEquals(1,d.expired);assertNull(d.task);}
 @Test public void sleepCountsAndWakeCheckExpiresBeforePlayback(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(60000);d.now=120000;assertFalse(t.check());assertEquals(1,d.expired);assertNull(d.task);}
 @Test public void ordinaryChecksDoNotExtendDeadline(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(60000);d.advance(30000);t.check();assertEquals(30000,t.remaining());d.advance(30000);assertEquals(1,d.expired);}
 @Test public void explicitReapplyStartsNewCountdown(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(60000);d.advance(30000);t.arm(60000);d.advance(30000);assertEquals(0,d.expired);d.advance(30000);assertEquals(1,d.expired);}
 @Test public void disableAndDestroyCancelStaleCallbacks(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(60000);Runnable stale=d.task;t.arm(0);stale.run();assertEquals(0,d.expired);t.arm(60000);stale=d.task;t.stop();d.now=120000;stale.run();assertEquals(0,d.expired);assertNull(d.task);}
 @Test public void unitsUseLongAndRejectInvalidValues(){assertEquals(60000,AutoStop.duration(1,0));assertEquals(7200000,AutoStop.duration(2,1));assertEquals(31_536_000_000L,AutoStop.duration(365,2));assertEquals(0,AutoStop.duration(0,0));assertEquals(0,AutoStop.duration(366,2));assertEquals(0,AutoStop.duration(Long.MAX_VALUE,2));assertEquals(0,AutoStop.duration(1,3));}
 @Test public void screenOffCancelsWorkAndWakeHonorsElapsedDeadline(){Device d=new Device();AutoStop t=new AutoStop(d);t.arm(60000);d.now=10000;t.setPaused(true);assertNull(d.task);d.now=20000;t.setPaused(false);assertEquals(60000,d.due);t.setPaused(true);d.now=70000;t.setPaused(false);assertEquals(1,d.expired);assertNull(d.task);}
 @Test public void armingWhileOffDoesNotScheduleOrResetOnRepeatedPause(){Device d=new Device();AutoStop t=new AutoStop(d);t.setPaused(true);t.arm(60000);assertNull(d.task);d.now=20000;t.setPaused(true);assertEquals(40000,t.remaining());assertNull(d.task);}
}
