from power108_helpers import *
from pathlib import Path
OUT=Path(sys.argv[3]);OUT.mkdir(parents=True,exist_ok=True)
def audio(mode=2,volume=5):
 sh('am','start','-n','kr.personal.usagitest/.AudioMode','--ei','ringer',str(mode),'--ei','volume',str(volume));sh('input','keyevent','3');time.sleep(.2)
def count():return int(state()['voicePlayed'])
def press(duration=0,move=0):
 a,b,c,d=bounds();x=(a+c)//2;y=b+(c-a)//2
 if duration or move:sh('input','swipe',str(x),str(y),str(x+move),str(y),str(duration or 180))
 else:sh('input','tap',str(x),str(y))
def quiet(label):
 n=count();press();time.sleep(.4);assert count()==n and state()['voiceActive']=='false',(label,state());print('PASS quiet:',label,flush=True)
def voice(label):
 wait(lambda:state()['voiceActive']=='false',8);n=count();press();wait(lambda:count()==n+1);print('PASS voice:',label,flush=True)
sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('settings','put','system','screen_off_timeout','600000');sh('cmd','notification','set_dnd','off');audio()
sh('am','force-stop',PKG);settings();tap(by(text='수동 모드'));switch('랜덤 전환 모드',False);switch('배터리 절약 모드',False);switch('우사기 소리 켜기',False);top();tap(by(text='계속 실행'));start();sh('input','keyevent','3');quiet('option off')
settings();switch('우사기 소리 켜기',True);start();sh('input','keyevent','3');wait(lambda:state()['voiceAllowed']=='true');voice('normal mode and option on');wait(lambda:state()['voiceActive']=='false',8)
for duration,move,label in [(150,150,'drag'),(700,0,'long press')]:
 n=count();press(duration,move);time.sleep(.3);assert count()==n and state()['voiceActive']=='false',(label,state());print('PASS quiet:',label,flush=True)
for mode,label in [(1,'vibrate'),(0,'silent')]:
 audio(mode);wait(lambda:state()['voiceAllowed']=='false');quiet(label)
audio(2,0);sh('cmd','notification','set_dnd','off');quiet('media volume zero')
audio();sh('cmd','notification','set_dnd','none');wait(lambda:state()['voiceAllowed']=='false');quiet('DND');sh('cmd','notification','set_dnd','off');audio();voice('restored normal')
# Select a naturally random long clip, then verify live policy changes cut it short.
def long_voice():
 for _ in range(30):
  wait(lambda:state()['voiceActive']=='false',8);n=count();press();wait(lambda:count()==n+1)
  if state()['voiceClip'] in ('0','6'):return
 raise AssertionError('no long clip sampled')
long_voice();before=count();press();assert count()==before,'overlapping voice';sh('cmd','notification','set_dnd','none');wait(lambda:state()['voiceActive']=='false',2);print('PASS live DND stops active long clip',flush=True);sh('cmd','notification','set_dnd','off');audio()
long_voice();audio(1);wait(lambda:state()['voiceActive']=='false',2);print('PASS live vibrate stops active long clip',flush=True);audio();voice('normal after live mute')
sh('input','keyevent','223');wait(lambda:state()['voiceActive']=='false' and state()['visible']=='false');time.sleep(2)
s=sh('dumpsys','activity','service',PKG+'/.CompanionService');assert not any(('appHandler ' in l or 'voiceHandler ' in l) and 'callback=' in l for l in s.splitlines()),s
print('PASS screen-off releases voice and leaves0app callbacks',flush=True);sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state()['visible']=='true');time.sleep(.3);assert state()['voiceActive']=='false';voice('wake followed by new tap')
# Test settings persist through service recreation, then restore default quiet test state.
wait(lambda:state()['voiceActive']=='false',8);sh('am','force-stop',PKG);settings();assert by(desc='우사기 소리 켜기').get('checked')=='true';switch('우사기 소리 켜기',False);start();sh('input','keyevent','3');quiet('disabled after recreation')
settings();switch('우사기 소리 켜기',True);top();tap(by(text='산책 모드'));start();sh('input','keyevent','3');wait(lambda:state()['visible']=='true');voice('stroll tap');wait(lambda:state()['voiceActive']=='false',8)
print('VOICE113 NATIVE CHECKS PASSED',flush=True)
