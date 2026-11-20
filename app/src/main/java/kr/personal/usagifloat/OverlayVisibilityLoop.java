package kr.personal.usagifloat;
/** Reconciles actual screen/lock state even when a device misses an unlock broadcast.
 * Handler delays use uptime: this loop does not wake a sleeping device. */
final class OverlayVisibilityLoop {
 interface Environment {
  boolean isInteractive();
  boolean canShow();
  void applyVisibility(boolean show);
  void postDelayed(Runnable task,long delay);
  void removeCallbacks(Runnable task);
 }
 private final Environment env;
 private boolean active;
 private final Runnable check=this::refresh;
 OverlayVisibilityLoop(Environment environment){env=environment;}
 void start(){active=true;refresh();}
 void refresh(){
  env.removeCallbacks(check);
  if(!active)return;
  env.applyVisibility(env.canShow());
  if(active&&env.isInteractive())env.postDelayed(check,1000);
 }
 void stop(){active=false;env.removeCallbacks(check);}
}
