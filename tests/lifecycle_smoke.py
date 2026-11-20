"""Run after device_smoke.py; emulator API31 at 1080x2400, density420."""
import subprocess,sys,time,re
ADB=[sys.argv[1]];PKG='kr.personal.usagifloat'
def sh(*a):return subprocess.check_output(ADB+['shell',*a],text=True,timeout=45)
def windows():return sh('dumpsys','window','windows')
def pet():
 s=windows();m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',s)
 if not m:return None
 r=re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]);return tuple(map(int,r.groups())) if r else None
def launch():
 sh('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity');time.sleep(.6);sh('input','tap','500','1600')
 for i in range(20):
  if pet():return
  time.sleep(.15)
 raise AssertionError('launch failed')
def log(s):print('PASS',s,flush=True)
launch();p=pet();prefs=sh('run-as',PKG,'cat','shared_prefs/companion.xml')
assert f'name="x" value="{p[0]}"' in prefs;log('restart restores last position')
launch();assert len(re.findall(r'Window #\d+ Window\{[^\n]* Usagi companion\}',windows()))==1;log('repeated start keeps one overlay')
auto=sh('settings','get','system','accelerometer_rotation').strip();rotation=sh('settings','get','system','user_rotation').strip()
try:
 sh('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity')
 sh('settings','put','system','accelerometer_rotation','0');sh('settings','put','system','user_rotation','1')
 for _ in range(30):
  display=sh('dumpsys','window','displays');p=pet()
  if 'mRotation=ROTATION_90' in display and p and p[2]<=2400 and p[3]<=1080:break
  time.sleep(.3)
 assert 'mRotation=ROTATION_90' in display and p and p[0]>=0 and p[1]>=0 and p[2]<=2400 and p[3]<=1080,p
 log('rotation clamps character within landscape screen')
finally:
 sh('settings','put','system','user_rotation',rotation);sh('settings','put','system','accelerometer_rotation',auto);time.sleep(.7)
sh('input','keyevent','223')
for _ in range(30):
 s=windows();m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',s)
 if m and 'mViewVisibility=0x4' in m[1]:break
 time.sleep(.2)
assert m and 'mViewVisibility=0x4' in m[1];log('screen off hides overlay')
sh('input','keyevent','224');sh('wm','dismiss-keyguard')
for _ in range(30):
 m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',windows())
 if m and 'mViewVisibility=0x0' in m[1]:break
 time.sleep(.2)
assert m and 'mViewVisibility=0x0' in m[1];log('unlock restores visible overlay')
sh('appops','set',PKG,'SYSTEM_ALERT_WINDOW','deny')
for _ in range(30):
 if pet() is None and 'ServiceRecord{' not in sh('dumpsys','activity','services',PKG):break
 time.sleep(.2)
assert pet() is None and 'ServiceRecord{' not in sh('dumpsys','activity','services',PKG);log('permission revocation removes service and overlay')
sh('appops','set',PKG,'SYSTEM_ALERT_WINDOW','allow')
print('LIFECYCLE SMOKE PASSED',flush=True)
