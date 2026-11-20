package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class CountdownTextTest {
 @Test public void expiredIsEmpty(){assertEquals("",CountdownText.format(0));assertEquals("",CountdownText.format(-1));}
 @Test public void finalMinuteShowsCeilingSeconds(){assertEquals("60초 남음",CountdownText.format(60000));assertEquals("2초 남음",CountdownText.format(1001));assertEquals("1초 남음",CountdownText.format(1));}
 @Test public void longerTimersOnlyChangeAtMinuteBoundaries(){assertEquals("2분 남음",CountdownText.format(60001));assertEquals(CountdownText.format(119000),CountdownText.format(61000));assertEquals("1시간 남음",CountdownText.format(3600000));assertEquals("1시간 1분",CountdownText.format(3600001));}
 @Test public void daysStayCompactAndLongSafe(){assertEquals("1일 00:00",CountdownText.format(86400000));assertEquals("365일 00:00",CountdownText.format(31536000000L));}
}
