"""Random mode checks on the signed app. Explicit dedicated-emulator serial required."""
import subprocess,sys,time,re,xml.etree.ElementTree as E
ADB=[sys.argv[1],'-s',sys.argv[2]];PKG='kr.personal.usagifloat'
def sh(*args):return subprocess.check_output(ADB+['shell',*args],text=True,timeout=45)
def ui():
 sh('uiautomator','dump','/sdcard/usagi-random.xml');return E.fromstring(sh('cat','/sdcard/usagi-random.xml'))
def node(text=None,desc=None):
 for _ in range(5):
  root=ui()
  for n in root.iter('node'):
   if (text is not None and n.get('text')==text) or (desc is not None and n.get('content-desc')==desc):return n
  sh('input','swipe','950','2050','950','750','350')
 raise AssertionError(('Missing UI',text,desc))
def tap(n):
 x,y,r,b=map(int,re.findall(r'\d+',n.get('bounds')));sh('input','tap',str((x+r)//2),str((y+b)//2))
def open_app():
 sh('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity')
 # Scroll to top, without crossing the overlay (which sits near the right edge).
 sh('input','swipe','100','700','100','2100','300')
def set_interval(value):
 tap(node(desc='랜덤 전환 간격 초'));sh('input','keyevent','123');sh('input','keyevent','67','67','67','67');sh('input','text',str(value));sh('input','keyevent','4');tap(node(text='간격 적용'))
def mode(value):
 n=node(desc='랜덤 전환 모드')
 if (n.get('checked')=='true')!=value:tap(n)
def state():
 s=sh('dumpsys','activity','service',PKG+'/.CompanionService')
 return {k:v for k,v in re.findall(r'^\s*(character|random|intervalSeconds|visible|animating)=(\S+)',s,re.M)}
def wait(predicate,seconds=10):
 end=time.monotonic()+seconds
 while time.monotonic()<end:
  if predicate():return
  time.sleep(.15)
 raise AssertionError('Timed out')
def log(s):print('PASS',s,flush=True)
def stop_button():
 # A manual overlay can cover the settings button; drag it clear first.
 windows=sh('dumpsys','window','windows');match=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',windows)
 if match:
  a,b,c,d=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',match[1]).groups())
  sh('input','swipe',str((a+c)//2),str((b+d)//2),'950','350','250')
 tap(node(text='재생 종료'));wait(lambda:not state())
open_app();tap(node(text='수동 모드'));mode(True);set_interval(2)
if len(sys.argv)>3:
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
tap(node(text='시작 · 우사기 띄우기'));wait(lambda:state().get('random')=='true')
assert state()['intervalSeconds']=='2';first=state()['character'];observed=[first];deadline=time.monotonic()+6.8
while time.monotonic()<deadline:
 s=state();assert s['visible']=='true' and s['animating']=='true'
 if s['character']!=observed[-1]:observed.append(s['character'])
 time.sleep(.15)
assert len(observed)>=3,observed
log('2-second random mode switches repeatedly: '+' → '.join(observed))
# While screen is off, the last visible character must be retained.
sh('input','keyevent','223');wait(lambda:state().get('visible')=='false');sleeping=state()['character'];time.sleep(3)
assert state()['character']==sleeping
sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true');wait(lambda:state().get('character')!=sleeping,5)
log('screen off pauses selection; unlock resumes switching')
# Disable returns to saved manual character (default original on this test AVD).
open_app();mode(False);fixed=state()['character'];time.sleep(2.6);assert state()['character']==fixed and state()['random']=='false'
log('mode off restores and holds fixed character')
# Change interval live; invalid input must not silently overwrite last valid value.
mode(True);set_interval(4);assert state()['intervalSeconds']=='4';set_interval(0);assert state()['intervalSeconds']=='4'
log('live interval changes apply; invalid zero is rejected')
# Restore valid text and restart; preferences must persist.
set_interval(4);stop_button();tap(node(text='시작 · 우사기 띄우기'));wait(lambda:state().get('random')=='true');assert state()['intervalSeconds']=='4'
log('mode and interval survive service restart')
# Hold beyond a due time: no random replacement while user is dragging/holding.
s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',s)
a,b,c,d=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]).groups());x=(a+c)//2;y=(b+d)//2
sh('input','motionevent','DOWN',str(x),str(y));held=state()['character']
try:
 time.sleep(4.8);assert state()['character']==held
finally:sh('input','motionevent','UP',str(x),str(y))
wait(lambda:state().get('character')!=held,3)
log('holding defers random change until release')
open_app();stop_button();time.sleep(4.5);assert not state()
log('Stop cancels timer without resurrecting overlay')
print('RANDOM DEVICE CHECKS PASSED',flush=True)
