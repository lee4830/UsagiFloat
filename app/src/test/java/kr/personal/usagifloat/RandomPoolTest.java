package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class RandomPoolTest {
 @Test public void allIncludesNewItemsWithoutMigratingOldPreferences(){assertArrayEquals(new int[]{0,1,2,3},RandomPool.resolve(true,"0,1",4));}
 @Test public void customSanitizesDuplicatesAndInvalidIds(){assertArrayEquals(new int[]{1,3},RandomPool.resolve(false,"3,1,1,-1,99,no",4));}
 @Test public void emptyOrCorruptFallsBackToAll(){assertArrayEquals(new int[]{0,1,2},RandomPool.resolve(false,"oops",3));assertArrayEquals(new int[]{0,1,2},RandomPool.resolve(false,"",3));}
 @Test public void singleSelectionIsValid(){assertArrayEquals(new int[]{2},RandomPool.resolve(false,"2",4));}
}
