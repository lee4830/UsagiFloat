package kr.personal.usagifloat;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
/** A little home to return the companion to when the user wants to stop. */
final class HomeTargetView extends View {
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final Path roof=new Path();
 private boolean active;
 HomeTargetView(Context context){super(context);setContentDescription("집에 놓으면 우사기 종료");}
 void setActive(boolean value){if(active!=value){active=value;invalidate();}}
 @Override protected void onDraw(Canvas canvas){
  super.onDraw(canvas);
  float w=getWidth(),h=getHeight(),cx=w/2,cy=h*.42f;
  paint.setStyle(Paint.Style.FILL);paint.setColor(active?0xff74a88a:0xf05b7665);
  canvas.drawCircle(cx,cy,w*.34f,paint);
  paint.setColor(0xfffff8e6);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(w*.025f);
  paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
  roof.reset();roof.moveTo(cx-w*.19f,cy-w*.025f);roof.lineTo(cx,cy-w*.19f);roof.lineTo(cx+w*.19f,cy-w*.025f);
  canvas.drawPath(roof,paint);
  roof.reset();roof.moveTo(cx-w*.14f,cy-w*.02f);roof.lineTo(cx-w*.14f,cy+w*.17f);
  roof.lineTo(cx+w*.14f,cy+w*.17f);roof.lineTo(cx+w*.14f,cy-w*.02f);canvas.drawPath(roof,paint);
  roof.reset();roof.moveTo(cx-w*.045f,cy+w*.17f);roof.lineTo(cx-w*.045f,cy+w*.045f);
  roof.lineTo(cx+w*.045f,cy+w*.045f);roof.lineTo(cx+w*.045f,cy+w*.17f);canvas.drawPath(roof,paint);
  paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(w*.10f);
  canvas.drawText(active?"놓으면 쉬어요":"집으로 보내요",cx,h*.91f,paint);
 }
}
