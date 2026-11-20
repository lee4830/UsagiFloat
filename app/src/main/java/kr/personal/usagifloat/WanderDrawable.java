package kr.personal.usagifloat;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Original video silhouettes; no generated poses, rotation, stretching or mirroring. */
final class WanderDrawable extends Drawable {
 private static final int[] IDS={R.drawable.motion_walk,R.drawable.motion_look,R.drawable.motion_wiggle,R.drawable.motion_bow,R.drawable.motion_bridges};
 private final Bitmap[] atlases=new Bitmap[5];private final Resources resources;
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final Rect source=new Rect();private int pose=WanderMotion.REST,frame=29;
 WanderDrawable(Resources resources){this.resources=resources;atlas(pose);}
 private Bitmap atlas(int pose){
  pose=Math.min(pose,4);
  if(atlases[pose]==null){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;atlases[pose]=BitmapFactory.decodeResource(resources,IDS[pose],options);if(atlases[pose]==null)throw new IllegalStateException("Missing original motion");}
  return atlases[pose];
 }
 void prepareForPlayback(){for(int i=0;i<atlases.length;i++)atlas(i);}
 void setFrame(int pose,int frame,boolean left){if(this.pose==pose&&this.frame==frame)return;atlas(pose);this.pose=pose;this.frame=frame;invalidateSelf();}
 @Override public void draw(Canvas canvas){
  int cell=pose>=4?(pose-4)*3+frame:frame;
  source.set(cell%8*256,cell/8*256,(cell%8+1)*256,(cell/8+1)*256);
  canvas.drawBitmap(atlas(pose),source,getBounds(),paint);
 }
 @Override public int getIntrinsicWidth(){return 256;}
 @Override public int getIntrinsicHeight(){return 256;}
 @Override public void setAlpha(int value){paint.setAlpha(value);invalidateSelf();}
 @Override public void setColorFilter(ColorFilter value){paint.setColorFilter(value);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
