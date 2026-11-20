from power108_helpers import *
from pathlib import Path
OUT=Path(sys.argv[3]);OUT.mkdir(parents=True,exist_ok=True)
def count():return int(state()['voicePlayed'])
def press():
 a,b,c,d=bounds();sh('input','tap',str((a+c)//2),str(b+(c-a)//2))
def volume(value):sh('am','start','-n','kr.personal.usagitest/.AudioMode','--ei','volume',str(value));time.sleep(.3);sh('input','keyevent','3')
def mode(value):
 # Dedicated1080x2400 API30 emulator volume-panel bell observed at985,850.
 # Public setRingerMode(SILENT) also enablesDND; the real SystemUI bell doesn't.
 for _ in range(4):
  d=sh('dumpsys','audio');current=re.search(r'mode \(external\) = (\w+)',d).group(1)
  if current==value:break
  sh('input','keyevent','24');sh('input','tap','985','850');time.sleep(.15)
 else:raise AssertionError(('ringer',value,current))
 sh('input','keyevent','4');assert sh('settings','get','global','zen_mode').strip()=='0';print('SystemUI mode',value,'DNDoff',flush=True)
def quiet(label):
 n=count();press();time.sleep(.35);assert count()==n and state()['voiceActive']=='false',(label,state());print('PASS quiet',label,flush=True)
def play(label):
 wait(lambda:state()['voiceActive']=='false',8);n=count();press();wait(lambda:count()==n+1);print('PASS voice',label,flush=True);wait(lambda:state()['voiceActive']=='false',8)
def start_home():start();sh('input','keyevent','3');time.sleep(.3)
def override(value):
 settings();switch('무음·진동에서도 재생',value);start_home()
sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('cmd','notification','set_dnd','off');sh('settings','put','system','screen_off_timeout','600000');volume(5)
sh('am','force-stop',PKG);settings();tap(by(text='수동 모드'));switch('우사기 소리 켜기',True);assert by(desc='무음·진동에서도 재생').get('checked')=='false';start_home()
# Keep pet clear of system-volume bell.
a,b,c,d=bounds();sh('input','swipe',str((a+c)//2),str(b+(c-a)//2),'250','400','200')
mode('SILENT');quiet('silent defaultoff');mode('VIBRATE');quiet('vibrate defaultoff')
override(True);assert state()['voiceInSilent']=='true';mode('VIBRATE');play('vibrate overrideon');mode('SILENT');play('silent overrideon')
volume(0);quiet('override does not override mediazero');volume(5);sh('cmd','notification','set_dnd','none');wait(lambda:state()['voiceAllowed']=='false');quiet('override does not overrideDND');sh('cmd','notification','set_dnd','off');mode('SILENT')
settings();switch('우사기 소리 켜기',False);assert not any(n.get('content-desc')=='무음·진동에서도 재생' for n in ui().iter('node'));start_home();assert state()['voiceInSilent']=='true';quiet('masteroff wins and childhidden')
settings();switch('우사기 소리 켜기',True);assert by(desc='무음·진동에서도 재생').get('checked')=='true';start_home();play('masteron restores retained override')
override(False);quiet('overrideoff blocks silent again')
override(True);sh('am','force-stop',PKG);settings();assert by(desc='무음·진동에서도 재생').get('checked')=='true';top();tap(by(text='산책 모드'));start_home();play('persisted override in stroll silent')
sh('input','keyevent','223');wait(lambda:state()['voiceActive']=='false');time.sleep(2);d=sh('dumpsys','activity','service',PKG+'/.CompanionService');assert not any(('appHandler ' in l or 'voiceHandler ' in l) and 'callback=' in l for l in d.splitlines());print('PASS screenoff0callbacks',flush=True)
sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('cmd','notification','set_dnd','off');mode('NORMAL');volume(5)
print('VOICE114 NATIVE CHECKS PASSED',flush=True)
