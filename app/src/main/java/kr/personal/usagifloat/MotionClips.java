package kr.personal.usagifloat;
/** Offline-smoothed original clips and three-frame bridges, all sampled at30fps. */
final class MotionClips {
 private static final int[] COUNTS={12,30,78,30,3,3,3,3,3,3};
 static int count(int pose){return COUNTS[pose];}
 static long duration(int pose){return count(pose)*1000L/30;}
 static int frame(int pose,long elapsed){return (int)((elapsed*30/1000)%count(pose));}
}
