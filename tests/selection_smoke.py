"""Test the signed release's real selection UI on an unlocked emulator."""
import subprocess,sys,time,re,xml.etree.ElementTree as E
from pathlib import Path
ADB=sys.argv[1];PKG='kr.personal.usagifloat';names=['꼬리 흔들기','말랑 우사기','응원 우사기','팔짱 우사기','파란 후드 우사기']
def sh(*args):return subprocess.check_output([ADB,'shell',*args],text=True,timeout=60)
def ui():
 sh('uiautomator','dump','/sdcard/usagi-selection.xml')
 return E.fromstring(sh('cat','/sdcard/usagi-selection.xml'))
def tap(node):
 x,y,r,b=map(int,re.findall(r'\d+',node.attrib['bounds']));sh('input','tap',str((x+r)//2),str((y+b)//2))
def choose(name):
 root=ui();spinner=next(n for n in root.iter('node') if n.get('content-desc')=='캐릭터 선택 목록');tap(spinner)
 root=ui();tap(next(n for n in root.iter('node') if n.get('text')==name))
def open_app():sh('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity')
def start():
 root=ui();tap(next(n for n in root.iter('node') if n.get('text','').startswith('시작 ·')))
 for _ in range(30):
  s=sh('dumpsys','window','windows')
  if re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}',s):return
  time.sleep(.2)
 raise AssertionError('No overlay')
def shot(name):
 with open(Path(sys.argv[2])/name,'wb') as f:subprocess.run([ADB,'exec-out','screencap','-p'],stdout=f,check=True)
sh('input','keyevent','224');sh('wm','dismiss-keyguard')
for i,name in enumerate(names):
 open_app();choose(name)
 root=ui();assert any(n.get('text')==name for n in root.iter('node'))
 if i==4:shot('selection-ui.png')
 start();time.sleep(.4)
 # uiautomator excludes non-focusable overlay windows; inspect WindowManager and capture pixels instead.
 s=sh('dumpsys','window','windows');assert len(re.findall(r'Window #\d+ Window\{[^\n]* Usagi companion\}',s))==1
 shot(f'overlay-{i}.png');print('PASS selected and displayed:',name,flush=True)
# Move the overlay clear of the settings buttons before exercising Stop.
s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',s)
a,b,c,d=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]).groups())
sh('input','swipe',str((a+c)//2),str((b+d)//2),'850','700','250')
# Reopen confirms stored selection, then stop/start to confirm service persistence.
open_app();root=ui();assert any(n.get('text')==names[4] for n in root.iter('node'))
tap(next(n for n in root.iter('node') if n.get('text')=='재생 종료'))
for _ in range(30):
 if not re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}',sh('dumpsys','window','windows')):break
 time.sleep(.2)
assert 'ServiceRecord{' not in sh('dumpsys','activity','services',PKG)
start();shot('persisted-selection.png')
print('PASS selection persisted through Activity and Service restart',flush=True)
# Screen on/off remains functional with animated WebP character.
sh('input','keyevent','223');time.sleep(.5);sh('input','keyevent','224');sh('wm','dismiss-keyguard')
for _ in range(30):
 s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',s)
 if m and 'mViewVisibility=0x0' in m[1]:break
 time.sleep(.2)
assert m and 'mViewVisibility=0x0' in m[1]
print('PASS selected WebP character restores after wake',flush=True)
open_app();root=ui();tap(next(n for n in root.iter('node') if n.get('text')=='재생 종료'))
print('SELECTION DEVICE CHECKS PASSED',flush=True)
