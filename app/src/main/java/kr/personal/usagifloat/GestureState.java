package kr.personal.usagifloat;
/** Touch state independent of Android callbacks; a normal drag cannot become a delete gesture. */
final class GestureState {
 private boolean active, moved, held;
 private float startX,startY;
 void down(float x,float y) { active=true; moved=false; held=false; startX=x; startY=y; }
 void move(float x,float y,int slop) { if (Math.hypot(x-startX,y-startY)>slop) moved=true; }
 boolean canLongPress(){return active && !moved && !held;}
 void longPress(){if(canLongPress()) held=true;}
 boolean canMove(){return active;}
 boolean isHeld(){return active && held;}
 boolean release(boolean inside){boolean remove=active && held && inside; cancel(); return remove;}
 void cancel(){active=false; held=false; moved=false;}
 static int clamp(int v,int min,int max){return Math.max(min,Math.min(v,Math.max(min,max)));}
 static boolean insideCircle(float x,float y,float cx,float cy,float radius){return Math.hypot(x-cx,y-cy)<=radius;}
}
