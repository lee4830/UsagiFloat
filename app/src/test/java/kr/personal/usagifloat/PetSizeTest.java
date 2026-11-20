package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class PetSizeTest {
 @Test public void manualReservesEarSpaceWithoutShrinkingBody(){assertEquals(150,PetSize.side(120,1,false,400,800,20));assertEquals(120,PetSize.side(120,1,true,400,800,20));}
 @Test public void largestSizeFitsLandscapeAndCaption(){assertEquals(180,PetSize.side(200,2,false,800,220,40));assertEquals(120,PetSize.side(200,2,false,120,800,40));}
}
