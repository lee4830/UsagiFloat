package kr.personal.usagifloat;
import org.junit.Test;
import static org.junit.Assert.*;
public class CharacterCatalogTest {
 @Test public void savedSelectionResolvesToItsOwnAnimation(){
  assertEquals(R.raw.usagi_soft,CharacterCatalog.resource(1));
  assertEquals(R.raw.usagi_cheer,CharacterCatalog.resource(2));
  assertEquals(R.raw.usagi_closeup,CharacterCatalog.resource(3));
  assertEquals(R.raw.usagi_hood,CharacterCatalog.resource(4));
 }
 @Test public void corruptOrFutureSavedSelectionFallsBackSafely(){
  assertEquals(R.raw.usagi,CharacterCatalog.resource(-1));
  assertEquals(R.raw.usagi,CharacterCatalog.resource(999));
 }
}
