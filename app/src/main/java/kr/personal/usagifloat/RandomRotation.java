package kr.personal.usagifloat;

import java.util.Random;

/** One cancellable uptime-based timer. Does not wake a sleeping device. */
final class RandomRotation {
 interface Environment {
  long now();
  int currentCharacter();
  boolean canSwitch();
  void showCharacter(int value);
  void postDelayed(Runnable task,long ms);
  void removeCallbacks(Runnable task);
 }
 private final Environment env;
 private final Random random;
 private final int count;
 private final Runnable task=this::tick;
 private boolean enabled,visible,scheduled,stopped;
 private long interval=30_000,remaining=30_000,due;
 RandomRotation(Environment env,Random random,int count){this.env=env;this.random=random;this.count=count;}
 static int validSeconds(int value){return value>=1&&value<=3600?value:30;}
 void configure(boolean enabled,int seconds){
  if(stopped)return;
  long next=validSeconds(seconds)*1000L;
  if(this.enabled==enabled&&interval==next)return;
  cancel();this.enabled=enabled;interval=next;remaining=next;schedule();
 }
 void setVisible(boolean value){
  if(stopped||visible==value)return;
  if(!value&&scheduled)remaining=Math.max(0,due-env.now());
  visible=value;cancel();schedule();
 }
 void restartInterval(){if(stopped)return;cancel();remaining=interval;schedule();}
 void stop(){stopped=true;enabled=false;cancel();}
 private void cancel(){env.removeCallbacks(task);scheduled=false;}
 private void schedule(){
  if(stopped||!enabled||!visible||count<2||scheduled)return;
  scheduled=true;due=env.now()+remaining;env.postDelayed(task,remaining);
 }
 private void tick(){
  scheduled=false;
  if(stopped||!enabled||!visible)return;
  if(!env.canSwitch()){remaining=250;schedule();return;}
  int current=env.currentCharacter();
  int next;
  if(current>=0&&current<count){next=random.nextInt(count-1);if(next>=current)next++;}
  else next=random.nextInt(count);
  env.showCharacter(next);
  remaining=interval;schedule();
 }
}
