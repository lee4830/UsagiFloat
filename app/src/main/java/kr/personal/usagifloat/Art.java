package kr.personal.usagifloat;
import android.content.Context;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import java.io.IOException;
final class Art {
 static AnimatedImageDrawable load(Context context,ImageView image) throws IOException {
  return load(context,image,context.getSharedPreferences("companion",Context.MODE_PRIVATE).getInt("character",0));
 }
 static AnimatedImageDrawable load(Context context,ImageView image,int selected) throws IOException {
  int target=image.getLayoutParams()!=null&&image.getLayoutParams().width>0?image.getLayoutParams().width:Math.round(154*context.getResources().getDisplayMetrics().density);
  return load(context,image,selected,target);
 }
 static AnimatedImageDrawable load(Context context,ImageView image,int selected,int target) throws IOException {
  Drawable d=ImageDecoder.decodeDrawable(source(context,selected),(decoder,info,source)->{int[] size=DecodeSize.fit(info.getSize().getWidth(),info.getSize().getHeight(),target);decoder.setTargetSize(size[0],size[1]);});
  if(!(d instanceof AnimatedImageDrawable)) throw new IOException("Animation unavailable");
  AnimatedImageDrawable animation=(AnimatedImageDrawable)d;
  image.setImageDrawable(d);
  animation.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
  return animation;
 }
 // Raw bytes avoid Resources density scaling (e.g.258px GIF becoming677px at420dpi).
 private static ImageDecoder.Source source(Context context,int selected) throws IOException {
  try(java.io.InputStream input=context.getResources().openRawResource(CharacterCatalog.resource(selected));java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream()){
   byte[] buffer=new byte[8192];int read;while((read=input.read(buffer))!=-1)output.write(buffer,0,read);
   return ImageDecoder.createSource(java.nio.ByteBuffer.wrap(output.toByteArray()));
  }
 }
}
