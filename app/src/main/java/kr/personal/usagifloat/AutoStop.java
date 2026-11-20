package kr.personal.usagifloat;
/** Elapsed-time deadline; Handler never wakes the phone. Check before resuming visuals. */
final class AutoStop {
 interface Environment {long now();void postDelayed(Runnable r,long ms);void removeCallbacks(Runnable r);void expire();}
 private final Environment env;private final Runnable task=this::tick;private long deadline;private boolean stopped,expired,paused;
 AutoStop(Environment env){this.env=env;}
 static long duration(long amount,int unit){long[] factors={60_000L,3_600_000L,86_400_000L};if(unit<0||unit>=factors.length||amount<1||amount>31_536_000_000L/factors[unit])return 0;return amount*factors[unit];}
 void arm(long duration){if(stopped)return;env.removeCallbacks(task);expired=false;deadline=duration>0?env.now()+duration:0;if(deadline>0&&!paused)env.postDelayed(task,duration);}
 boolean check(){if(stopped||expired)return false;if(deadline>0&&env.now()>=deadline){expired=true;deadline=0;env.removeCallbacks(task);env.expire();return false;}return true;}
 long remaining(){return deadline>0?Math.max(0,deadline-env.now()):0;}
 boolean enabled(){return deadline>0;}
 void setPaused(boolean value){if(stopped||paused==value)return;paused=value;env.removeCallbacks(task);if(!paused)tick();}
 private void tick(){if(paused||!check())return;env.removeCallbacks(task);if(deadline>0)env.postDelayed(task,Math.max(1,remaining()));}
 void stop(){stopped=true;deadline=0;env.removeCallbacks(task);}
}
