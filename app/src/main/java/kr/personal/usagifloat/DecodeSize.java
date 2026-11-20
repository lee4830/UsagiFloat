package kr.personal.usagifloat;
final class DecodeSize {
 static int[] fit(int width,int height,int target){float scale=Math.min(1f,Math.max(1,target)/(float)Math.max(width,height));return new int[]{Math.max(1,Math.round(width*scale)),Math.max(1,Math.round(height*scale))};}
}
