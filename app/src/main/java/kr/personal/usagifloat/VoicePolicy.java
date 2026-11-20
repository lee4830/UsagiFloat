package kr.personal.usagifloat;
final class VoicePolicy {
 static boolean allowed(boolean enabled,boolean inSilent,int ringer,int volume,boolean interactive,boolean locked,boolean interruptionsAllowed){return enabled&&(ringer==2||(inSilent&&(ringer==0||ringer==1)))&&volume>0&&interactive&&!locked&&interruptionsAllowed;}
 static boolean tap(boolean unmovedNotHeld,long duration){return unmovedNotHeld&&duration>=0&&duration<350;}
}
