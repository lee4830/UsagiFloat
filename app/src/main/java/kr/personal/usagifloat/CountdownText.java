package kr.personal.usagifloat;
/** Rounded-up remaining units: minute resolution until the final60seconds. */
final class CountdownText {
 static String format(long ms){
  if(ms<=0)return "";
  if(ms<=60000)return ((ms+999)/1000)+"초 남음";
  long minutes=(ms+59999)/60000;
  if(minutes<60)return minutes+"분 남음";
  if(minutes<1440){long hours=minutes/60,rest=minutes%60;return hours+"시간"+(rest==0?" 남음":" "+rest+"분");}
  long days=minutes/1440,hours=minutes%1440/60,rest=minutes%60;
  return days+"일 "+(hours<10?"0":"")+hours+":"+(rest<10?"0":"")+rest;
 }
}
