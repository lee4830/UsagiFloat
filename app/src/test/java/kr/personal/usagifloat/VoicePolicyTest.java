package kr.personal.usagifloat;
import org.junit.Test;import static org.junit.Assert.*;
public class VoicePolicyTest {
 @Test public void requiresEveryPermissionCondition(){assertTrue(VoicePolicy.allowed(true,2,5,true,false,true));assertFalse(VoicePolicy.allowed(false,2,5,true,false,true));assertFalse(VoicePolicy.allowed(true,0,5,true,false,true));assertFalse(VoicePolicy.allowed(true,1,5,true,false,true));assertFalse(VoicePolicy.allowed(true,2,0,true,false,true));assertFalse(VoicePolicy.allowed(true,2,5,false,false,true));assertFalse(VoicePolicy.allowed(true,2,5,true,true,true));assertFalse(VoicePolicy.allowed(true,2,5,true,false,false));}
 @Test public void tapIsShortUnmovedAndNotHeld(){assertTrue(VoicePolicy.tap(true,120));assertFalse(VoicePolicy.tap(false,120));assertFalse(VoicePolicy.tap(true,350));assertFalse(VoicePolicy.tap(true,-1));}
 @Test public void dragReturningToOriginIsNotTap(){GestureState g=new GestureState();g.down(10,10);g.move(50,10,8);g.move(10,10,8);assertFalse(VoicePolicy.tap(g.canLongPress(),100));}
 @Test public void longPressCancelAndMultiTouchCannotTap(){GestureState g=new GestureState();g.down(10,10);g.longPress();assertFalse(VoicePolicy.tap(g.canLongPress(),100));g.cancel();assertFalse(VoicePolicy.tap(g.canLongPress(),100));}
}
