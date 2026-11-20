package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class GestureStateTest {
 @Test public void cancelledPointerCannotContinueMoving() {
  GestureState g=new GestureState();g.down(10,10);assertTrue(g.canMove());
  g.cancel();assertFalse(g.canMove());g.move(200,900,8);assertFalse(g.canMove());
  g.down(30,30);assertTrue(g.canMove());
 }
 @Test public void draggingBeforeHoldNeverArmsTrash() {
  GestureState g=new GestureState(); g.down(10,10); g.move(35,10,8);
  assertFalse(g.canLongPress()); assertFalse(g.release(true));
 }
 @Test public void heldImageOnlyStopsWhenReleasedInsideTrash() {
  GestureState g=new GestureState(); g.down(10,10); g.move(12,12,8);
  assertTrue(g.canLongPress()); g.longPress(); g.move(200,800,8); assertTrue(g.release(true));
  g.down(10,10); g.longPress(); assertFalse(g.release(false));
 }
 @Test public void cancelClearsDeletionState() {
  GestureState g=new GestureState(); g.down(10,10); g.longPress(); g.cancel();
  assertFalse(g.canLongPress()); assertFalse(g.release(true));
 }
 @Test public void boundsKeepImageReachableAfterRotation() {
  assertEquals(240,GestureState.clamp(800,0,240));
  assertEquals(0,GestureState.clamp(-20,0,240));
  assertEquals(100,GestureState.clamp(100,0,240));
  assertEquals(0,GestureState.clamp(100,0,-10));
 }
 @Test public void trashUsesCircleInsteadOfItsBoundingSquare() {
  assertTrue(GestureState.insideCircle(500,900,500,900,48));
  assertTrue(GestureState.insideCircle(548,900,500,900,48));
  assertFalse(GestureState.insideCircle(545,945,500,900,48));
 }
}
