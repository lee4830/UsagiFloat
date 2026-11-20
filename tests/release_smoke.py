"""ADB smoke tests for the debug app on a dedicated/unlocked emulator, API 31+.
Usage: python3 tests/device_smoke.py /absolute/path/to/adb [serial]
Touches only this app, plus temporary rotation (restored in finally).
"""
import subprocess,sys,time,re,json
ADB=[sys.argv[1]]+(['-s',sys.argv[2]] if len(sys.argv)>2 else [])
PKG='kr.personal.usagifloat'
def adb(*args): return subprocess.check_output(ADB+list(args),text=True,timeout=45)
def shell(*args): return adb('shell',*args)
def window(title='Usagi companion'):
 s=shell('dumpsys','window','windows')
 for b in re.split(r'(?=  Window #\d+)',s):
  if re.search(r'Window #\d+ Window\{[^\n]* '+re.escape(title)+r'\}',b):
   m=re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',b)
   if m:
    coords=tuple(map(int,m.groups()))
    if coords[2]>coords[0] and coords[3]>coords[1]:return coords
 return None
def wait_window(exists=True):
 for _ in range(15):
  p=window()
  if bool(p)==exists:return p
  time.sleep(.2)
 raise AssertionError(f'overlay exists != {exists}')
def record(name):print('PASS',name,flush=True)
def move(x,y):shell('input','motionevent','MOVE',str(x),str(y))
def down(x,y):shell('input','motionevent','DOWN',str(x),str(y))
def up(x,y):shell('input','motionevent','UP',str(x),str(y))
def center(p):return ((p[0]+p[2])//2,(p[1]+p[3])//2)
p=wait_window();record('service creates overlay')
# normal drag must not show trash, even when held after the drag
x,y=center(p);down(x,y);move(x-300,y+300);time.sleep(.7)
assert window('Usagi home') is None
up(x-300,y+300);time.sleep(.2)
q=window();assert q[0]==max(0,p[0]-300) and q[1]==p[1]+300,(p,q)
record('drag moves and stays; moving cancels long press')
# Long press outside target should keep service alive.
x,y=center(q);down(x,y);time.sleep(.7);assert window('Usagi home')
up(x,y);time.sleep(.2);assert window() and window('Usagi home') is None
record('hold shows bottom trash; release outside keeps companion')
# Delete using centre of rendered target.
x,y=center(q);down(x,y);time.sleep(.7);t=window('Usagi home');assert t
cx=(t[0]+t[2])//2;cy=round(t[1]+(t[3]-t[1])*.42)
move(cx,cy);time.sleep(.2);saved=window();up(cx,cy);wait_window(False)
services=shell('dumpsys','activity','services',PKG)
assert 'ServiceRecord{' not in services
record('drop in trash removes both overlay and foreground service')
shell('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity');time.sleep(1)
shell('input','tap','500','1600');p=wait_window();assert p==saved,(p,saved)
record('signed release restart restores drop position')
shell('input','keyevent','223')
for _ in range(40):
 w=shell('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',w)
 if m and 'mViewVisibility=0x4' in m[1]:break
 time.sleep(.2)
assert m and 'mViewVisibility=0x4' in m[1]
shell('input','keyevent','224');shell('wm','dismiss-keyguard')
for _ in range(40):
 w=shell('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',w)
 if m and 'mViewVisibility=0x0' in m[1]:break
 time.sleep(.2)
assert m and 'mViewVisibility=0x0' in m[1]
record('signed release screen off hides; unlock shows again')
shell('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity');time.sleep(1)
shell('input','tap','500','1770');wait_window(False)
record('signed release stop button removes overlay')
print('SIGNED RELEASE SMOKE PASSED',flush=True)
