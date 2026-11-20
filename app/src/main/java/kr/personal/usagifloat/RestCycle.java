package kr.personal.usagifloat;
/** Opt-in30s play /30s still cycle. Hidden/held/off states queue no work. */
final class RestCycle {
 interface Environment {long now();void setResting(boolean value);void postDelayed(Runnable r,long ms);void removeCallbacks(Runnable r);}
 private final Environment env;private final Runnable task=this::tick;private boolean enabled,visible,held,resting,stopped;
 RestCycle(Environment env){this.env=env;}
 void configure(boolean value){if(stopped||enabled==value)return;enabled=value;restart();}
 void setVisible(boolean value){if(stopped||visible==value)return;visible=value;restart();}
 void setHeld(boolean value){if(stopped||held==value)return;held=value;restart();}
 private boolean active(){return enabled&&visible&&!held&&!stopped;}
 private void rest(boolean value){if(resting!=value){resting=value;env.setResting(value);}}
 private void restart(){env.removeCallbacks(task);rest(false);if(active())env.postDelayed(task,30000);}
 private void tick(){if(!active())return;rest(!resting);if(active())env.postDelayed(task,30000);}
 void stop(){stopped=true;env.removeCallbacks(task);}
}
