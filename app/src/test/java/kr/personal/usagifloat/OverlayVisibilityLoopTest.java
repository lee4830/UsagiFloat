package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class OverlayVisibilityLoopTest {
 static class Device implements OverlayVisibilityLoop.Environment {
  boolean awake=true,locked=false,visible; Runnable pending;
  public boolean canShow(){return awake&&!locked;}
  public void applyVisibility(boolean show){visible=show;}
  public void postDelayed(Runnable task,long delay){assertNull("Only one callback may be queued",pending);pending=task;}
  public void removeCallbacks(Runnable task){if(pending==task)pending=null;}
  void elapse(){Runnable task=pending;pending=null;if(task!=null)task.run();}
 }
 @Test public void missingUnlockBroadcastStillRestoresCharacter(){
  Device d=new Device();OverlayVisibilityLoop loop=new OverlayVisibilityLoop(d);loop.start();assertTrue(d.visible);
  d.awake=false;d.locked=true;loop.refresh();assertFalse(d.visible);
  d.awake=true;loop.refresh();assertFalse(d.visible);
  // Device unlocks later, without any further broadcast to the app.
  d.locked=false;d.elapse();assertTrue(d.visible);
 }
 @Test public void repeatedStartImmediatelyRepairsHiddenOverlay(){
  Device d=new Device();OverlayVisibilityLoop loop=new OverlayVisibilityLoop(d);loop.start();
  d.visible=false;loop.start();assertTrue(d.visible);
 }
 @Test public void stoppedLoopCannotResurrectDismissedCharacter(){
  Device d=new Device();OverlayVisibilityLoop loop=new OverlayVisibilityLoop(d);loop.start();Runnable old=d.pending;
  loop.stop();d.visible=false;assertNull(d.pending);if(old!=null)old.run();assertFalse(d.visible);
 }
 @Test public void repeatedEventsKeepOnlyOnePendingCheck(){
  Device d=new Device();OverlayVisibilityLoop loop=new OverlayVisibilityLoop(d);loop.start();loop.refresh();loop.refresh();
  assertNotNull(d.pending);d.elapse();assertNotNull(d.pending);
 }
}
