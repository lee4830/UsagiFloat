package kr.personal.usagifloat;
/** Manual artwork reserves 640/512 canvas space around a calibrated body. */
final class PetSize {
 static int side(int setting,float density,boolean wander,int safeWidth,int safeHeight,int caption){
  int requested=Math.round(Math.max(80,Math.min(200,setting))*density*(wander?1f:1.25f));
  return Math.max(1,Math.min(requested,Math.min(safeWidth,safeHeight-caption)));
 }
}
