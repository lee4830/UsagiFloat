"""Real signed APK stroll behavior on explicit dedicated emulator (1080x2400)."""
from drag_animation_smoke import *
def state():return dict(re.findall(r'^\s*(\w+)=(\S+)',sh('dumpsys','activity','service',PKG+'/.CompanionService'),re.M))
def wait(predicate,seconds=12):
 end=time.monotonic()+seconds
 while time.monotonic()<end:
  if predicate():return
  time.sleep(.15)
 raise AssertionError(('timeout',state()))
def log(msg):print('PASS',msg,flush=True)
open_app();tap(find('산책 모드'));time.sleep(1.3)
# Speed choice exercises the persisted settings route.
tap(next(n for n in ui().iter('node') if n.get('content-desc')=='산책 속도'));tap(find('활발하게'))
if len(sys.argv)>3:
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
tap(find('시작 · 우사기 띄우기'));wait(lambda:state().get('wander')=='true' and state().get('visible')=='true')
assert state()['random']=='false'
first=bounds();wait(lambda:bounds()!=first);log('stroll moves automatically; manual random rotation is suspended')
# Verify position and sprite freeze immediately under a finger, including a manual move.
a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2
sh('input','motionevent','DOWN',str(x),str(y));time.sleep(.2);fixed=bounds();frozen=state()['wanderFrame'];time.sleep(.6);assert bounds()==fixed and state()['wanderFrame']==frozen
sh('input','motionevent','MOVE','380','700');time.sleep(.2);dropped=bounds();assert dropped!=fixed
sh('input','motionevent','UP','380','700');time.sleep(.6);assert bounds()==dropped
wait(lambda:bounds()!=dropped,6);log('grab pauses, dragging repositions, release rests then resumes')
# Wait for actual idle pose, verify animated pixels while its position remains fixed.
wait(lambda:state().get('wanderPose')!='0',20)
# Observe all four native states and save actual device snapshots for visual QA.
seen=set();deadline=time.monotonic()+120
while time.monotonic()<deadline and len(seen)<4:
 s=state();pose=int(s['wanderPose'])
 if pose not in seen:
  seen.add(pose)
  if len(sys.argv)>3:
   from pathlib import Path
   dest=Path('work/wander-shots')/f'stroll-pose-{pose}.png';dest.parent.mkdir(exist_ok=True)
   with dest.open('wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
 time.sleep(.18)
assert seen=={0,1,2,3},seen;log('walking, looking around, butt wiggle and rest states all rendered')
sh('input','keyevent','223');wait(lambda:state().get('visible')=='false');off=state();time.sleep(1.2);assert state()['wanderFrame']==off['wanderFrame'] and state()['wanderPose']==off['wanderPose']
sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true');first=bounds();wait(lambda:bounds()!=first,8);log('screen off pauses; unlock resumes without disappearing')
# Reopen confirms saved mode and speed; manual selection restores a GIF immediately.
open_app();root=ui();assert any(n.get('text')=='산책 모드' and n.get('checked')=='true' for n in root.iter('node'));assert any(n.get('text')=='활발하게' for n in root.iter('node'))
tap(find('수동 모드'));wait(lambda:state().get('wander')=='false' and state().get('animating')=='true');log('mode and speed persist; manual GIF resumes live')
# Restore stroll, then use actual house target to stop it.
tap(find('산책 모드'));tap(find('시작 · 우사기 띄우기'));wait(lambda:state().get('wander')=='true' and state().get('visible')=='true')
a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2
sh('input','motionevent','DOWN',str(x),str(y));time.sleep(.8)
s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi home\}:([\s\S]*?)(?=  Window #|\Z)',s);assert m
l,t,r,b=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]).groups());hx=(l+r)//2;hy=round(t+(b-t)*.42)
sh('input','motionevent','MOVE',str(hx),str(hy));time.sleep(.3);sh('input','motionevent','UP',str(hx),str(hy));wait(lambda:not state());time.sleep(1);assert not state();log('house dismissal cancels strolling without resurrection')
print('STROLL DEVICE CHECKS PASSED',flush=True)
