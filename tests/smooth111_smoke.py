from power108_helpers import *
from pathlib import Path
OUT=Path(sys.argv[3]);OUT.mkdir(parents=True,exist_ok=True)
def dump():return sh('dumpsys','activity','service',PKG+'/.CompanionService')
def callbacks():return [l for l in dump().splitlines() if 'appHandler ' in l and 'callback=' in l]
sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('settings','put','system','screen_off_timeout','600000')
sh('am','force-stop',PKG);settings();tap(by(text='산책 모드'));switch('배터리 절약 모드',False);top();tap(by(text='계속 실행'));start();sh('input','keyevent','3');time.sleep(1)
assert state()['wander']=='true';frames('smooth stroll')
seen=set();end=time.monotonic()+35
while time.monotonic()<end:
 s=state();seen.add(int(s['wanderPose']));a,b,c,d=bounds();assert a>=0 and b>=0 and c<=1080 and d<=2400,(a,b,c,d);time.sleep(.05)
print('Observed poses',sorted(seen),flush=True);assert 0 in seen and any(i>=4 for i in seen),seen
a,b,c,d=bounds();x=(a+c)//2;y=b+(c-a)//2
sh('input','motionevent','DOWN',str(x),str(y))
try:
 sh('input','motionevent','MOVE','500','600');time.sleep(.25)
finally:sh('input','motionevent','UP','500','600')
time.sleep(2.3);frames('stroll after drag');assert state()['wander']=='true'
sh('input','keyevent','223');wait(lambda:state().get('interactive')=='false');time.sleep(2)
s=state();assert s['visible']=='false' and not callbacks(),dump();sh('dumpsys','gfxinfo',PKG,'reset');time.sleep(12)
s2=state();assert s2['wanderFrame']==s['wanderFrame'] and s2['wanderPose']==s['wanderPose'];assert not callbacks(),dump()
gfx=sh('dumpsys','gfxinfo',PKG);assert re.search(r'Total frames rendered: 0\b',gfx),gfx
(OUT/'off.txt').write_text(dump()+'\n'+gfx);print('PASS screen off:0app callbacks,0frames, unchanged pose for12s',flush=True)
sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true');frames('stroll wake')
print('SMOOTH111 NATIVE CHECKS PASSED',flush=True)
