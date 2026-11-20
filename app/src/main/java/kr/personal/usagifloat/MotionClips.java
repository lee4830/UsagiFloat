package kr.personal.usagifloat;
/** Exact source frame counts, sampled at the original30fps without speed warping. */
final class MotionClips {
 private static final int[] COUNTS={12,30,78,30};
 static int count(int pose){return COUNTS[pose];}
 static long duration(int pose){return count(pose)*1000L/30;}
 static int frame(int pose,long elapsed){return (int)((elapsed*30/1000)%count(pose));}
}
