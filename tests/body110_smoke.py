from power108_helpers import *
from pathlib import Path
OUT=Path(sys.argv[3]);OUT.mkdir(parents=True,exist_ok=True)
def dump():return sh('dumpsys','activity','service',PKG+'/.CompanionService')
def callbacks():return [l for l in dump().splitlines() if 'appHandler ' in l and 'callback=' in l]
def cpu():
 pid=sh('pidof',PKG).strip();raw=sh('cat','/proc/'+pid+'/stat');parts=raw[raw.rfind(')')+2:].split();return int(parts[11])+int(parts[12])
def off_check(label,seconds=12):
 sh('input','keyevent','223');wait(lambda:state().get('interactive')=='false');time.sleep(2)
 s=state();assert s['visible']=='false' and s['animating']=='false',s;assert not callbacks(),dump()
 sh('dumpsys','gfxinfo',PKG,'reset');before=cpu();time.sleep(seconds);after=cpu();assert not callbacks(),dump();s2=state();assert s2['character']==s['character'] and s2['wanderFrame']==s['wanderFrame'],(s,s2)
 gfx=sh('dumpsys','gfxinfo',PKG);m=re.search(r'Total frames rendered: (\d+)',gfx);assert m and int(m[1])==0,gfx
 (OUT/(label+'-off.txt')).write_text(dump()+'\n'+gfx+'\nCPU ticks delta='+str(after-before))
 print('PASS',label,'screen-off:0app callbacks,0rendered frames, CPU ticks delta',after-before,'over',seconds,'s',flush=True)
 sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true');assert callbacks(),dump();frames(label+' after wake')
def select(name):
 top();tap(by(desc='캐릭터 선택 목록'))
 def scroll(down):
  nodes=[n for n in ui().iter('node') if n.get('scrollable')=='true']
  if not nodes:return
  l,t,r,b=map(int,re.findall(r'\d+',nodes[-1].get('bounds')));x=(l+r)//2;sh('input','swipe',str(x),str(t+60 if down else b-60),str(x),str(b-60 if down else t+60),'150')
 for _ in range(2):scroll(True)
 for _ in range(4):
  for n in ui().iter('node'):
   if n.get('text')==name:tap(n);return
  scroll(False)
 raise AssertionError(name)
def onscreen():
 windows=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',windows)
 return bool(m and 'isOnScreen=true' in m[1])
_start=start
_original_frames=frames
def start():
 _start();sh('input','keyevent','3');wait(onscreen)
def frames(label):
 wait(onscreen);_original_frames(label)
def fresh():
 sh('am','force-stop',PKG);settings();tap(by(text='수동 모드'))
sh('wm','size','reset');time.sleep(1);sh('wm','set-user-rotation','lock','0');time.sleep(1);sh('input','keyevent','3');sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('settings','put','system','screen_off_timeout','600000')
if 'power' not in sys.argv[4:]:
 # Configure with the overlay stopped so it cannot intercept settings controls.
 fresh();switch('랜덤 전환 모드',False);switch('배터리 절약 모드',False);timer(5,'분')
 n=by(desc='캐릭터 크기');l,t,r,b=map(int,re.findall(r'\d+',n.get('bounds')));sh('input','tap',str(r-35),str((t+b)//2));select('팔랑 귀 우사기');start();assert state()['character']=='10' and state()['random']=='false';frames('maximum manual ears')
 # Launcher is portrait-locked and Android Settings suppresses overlays. Use our manual-mode Activity and scroll its animated preview out of view.
 sh('am','start','-n',PKG+'/.MainActivity');sh('settings','put','system','accelerometer_rotation','0');sh('wm','set-user-rotation','lock','1');time.sleep(2);sh('input','swipe','80','950','80','100','150');sh('input','swipe','80','950','80','100','150');a,b,c,d=bounds();assert a>=0 and b>=0 and c<=2400 and d<=1080,(a,b,c,d);frames('landscape bounded ears')
 sh('input','swipe',str((a+c)//2),str(b+(c-a)//2),'2200','850','500');frames('landscape drag')
 try:
  sh('wm','size','500x900');time.sleep(2);a,b,c,d=bounds();assert a>=0 and b>=0 and c<=900 and d<=500,(a,b,c,d);assert c-a<640;frames('compact viewport full canvas and caption')
 finally:
  sh('wm','size','reset');time.sleep(1);sh('wm','set-user-rotation','lock','0');time.sleep(2)
 print('PASS maximum body size,landscape,compact safe bounds and drag',flush=True)
fresh();top();tap(by(text='계속 실행'));start();assert state()['character']=='10' and state()['random']=='false';off_check('manual')
fresh();switch('배터리 절약 모드',True);switch('랜덤 전환 모드',True);timer(5,'분');start();assert state()['random']=='true' and state()['powerSaving']=='true';off_check('random-saving-timer')
fresh();top();tap(by(text='산책 모드'));start();assert state()['wander']=='true' and state()['powerSaving']=='true';off_check('stroll-saving-timer')
fresh();switch('랜덤 전환 모드',False);switch('배터리 절약 모드',False);timer(1,'분');start();sh('input','keyevent','223');wait(lambda:state().get('interactive')=='false');time.sleep(2);assert not callbacks(),dump()
time.sleep(60);assert state().get('visible')=='false' and not callbacks();sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:not state());assert 'Usagi companion' not in sh('dumpsys','window','windows');print('PASS timer elapsed off,0queued callbacks,expired before showing on wake',flush=True)
fresh();top();tap(by(text='계속 실행'))
print('BODY110 NATIVE CHECKS PASSED',flush=True)
