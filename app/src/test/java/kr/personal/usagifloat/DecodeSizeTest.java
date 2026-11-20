package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class DecodeSizeTest {
 @Test public void largeSourceFitsVisiblePixelsWithoutChangingAspect(){assertArrayEquals(new int[]{300,200},DecodeSize.fit(900,600,300));}
 @Test public void smallOriginalIsNeverUpscaledDuringDecode(){assertArrayEquals(new int[]{258,258},DecodeSize.fit(258,258,420));}
 @Test public void portraitAndTinyTargetsStayPositive(){assertArrayEquals(new int[]{150,300},DecodeSize.fit(480,960,300));assertArrayEquals(new int[]{1,1},DecodeSize.fit(480,960,1));}
}
