"""Signed 1.0.1 update regression; API31,1080x2400/420dpi dedicated emulator."""
import subprocess,sys,time,re,pathlib
ADB=sys.argv[1];PKG='kr.personal.usagifloat'
def sh(*args):return subprocess.check_output([ADB,'shell',*args],text=True,timeout=45)
def block(title='Usagi companion'):
 s=sh('dumpsys','window','windows')
 m=re.search(r'Window #\d+ Window\{[^\n]* '+re.escape(title)+r'\}:([\s\S]*?)(?=  Window #|\Z)',s)
 return m[1] if m else ''
def rect(title='Usagi companion'):
 m=re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',block(title))
 if m:
  p=tuple(map(int,m.groups()))
  if p[2]>p[0] and p[3]>p[1]:return p
 return None
def wait(check):
 for _ in range(60):
  if check():return
  time.sleep(.2)
 raise AssertionError('Timed out waiting for state')
def visible():return 'mViewVisibility=0x0' in block() and rect() is not None
def centre(p):return (p[0]+p[2])//2,(p[1]+p[3])//2
def start():
 sh('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity');time.sleep(.5)
 sh('input','tap','500','1600');wait(visible)
def log(s):print('PASS',s,flush=True)
sh('input','keyevent','224');sh('wm','dismiss-keyguard');start()
log('signed update starts without uninstall or renewed overlay permission')
# Check repeated screen off / unlock without relaunching Activity.
for cycle in range(3):
 sh('input','keyevent','223');wait(lambda:'mViewVisibility=0x4' in block())
 assert 'ServiceRecord{' in sh('dumpsys','activity','services',PKG)
 sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(visible)
 log(f'screen off/on + unlock cycle {cycle+1}: visible with same service')
start()
s=sh('dumpsys','window','windows');assert len(re.findall(r'Window #\d+ Window\{[^\n]* Usagi companion\}',s))==1
log('Start on running service refreshes display without duplicate window')
x,y=centre(rect());sh('input','swipe',str(x),str(y),'800','700','250');time.sleep(.3)
x,y=centre(rect());sh('input','motionevent','DOWN',str(x),str(y))
try:
 wait(lambda:rect('Usagi home') is not None)
 if len(sys.argv)>2:
  with open(sys.argv[2],'wb') as f:subprocess.run([ADB,'exec-out','screencap','-p'],stdout=f,check=True,timeout=45)
 t=rect('Usagi home');cx=(t[0]+t[2])//2;cy=round(t[1]+(t[3]-t[1])*.42)
 sh('input','motionevent','MOVE',str(cx),str(cy));time.sleep(.3)
finally:
 sh('input','motionevent','UP',str(cx if 'cx' in locals() else x),str(cy if 'cy' in locals() else y))
wait(lambda:not block())
wait(lambda:'ServiceRecord{' not in sh('dumpsys','activity','services',PKG))
log('long press shows home; dropping there stops overlay/service')
# Timers must not bring dismissed pet back after wake.
sh('input','keyevent','223');sh('input','keyevent','224');sh('wm','dismiss-keyguard');time.sleep(2)
assert not block() and 'ServiceRecord{' not in sh('dumpsys','activity','services',PKG)
log('home dismissal remains stopped after screen off/on')
print('UPDATE 1.0.1 DEVICE CHECKS PASSED',flush=True)
