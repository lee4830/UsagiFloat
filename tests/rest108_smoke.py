from power108_helpers import *
def move_clear():
 a,b,c,d=bounds();sh('input','swipe',str((a+c)//2),str((b+d)//2),'650','950','250')
def rendered():
 s=sh('dumpsys','gfxinfo',PKG);return sum(map(int,re.findall(r'Total frames rendered:\s*(\d+)',s)))
def pixels():
 box=bounds();raw=subprocess.check_output(ADB+['exec-out','screencap','-p']);return hashlib.sha256(Image.open(io.BytesIO(raw)).convert('RGB').crop(box).tobytes()).hexdigest()
settings();tap(by(text='수동 모드'));switch('랜덤 전환 모드',False);switch('배터리 절약 모드',True);top();tap(by(text='계속 실행'));start();move_clear()
assert state()['powerSaving']=='true' and state()['animating']=='true'
assert 0<int(state()['animationWidth'])<=bounds()[2]-bounds()[0]
print('PASS raw-source decoding avoids density upscale:',state()['animationWidth'],flush=True)
sh('dumpsys','gfxinfo',PKG,'reset');time.sleep(5);active=rendered();assert active>10,active
wait(lambda:state().get('resting')=='true',35);time.sleep(.6);assert state()['visible']=='true' and state()['animating']=='false'
sh('dumpsys','gfxinfo',PKG,'reset');sample={pixels() for _ in range(5)};time.sleep(5);sample.add(pixels());paused=rendered();assert len(sample)==1 and paused<=2,(len(sample),paused)
print('PASS resting keeps character visible; GIF pixels frozen; rendered frames5s active/rest:',active,paused,flush=True)
wait(lambda:state().get('resting')=='false' and state().get('animating')=='true',35)
print('PASS30second rest resumes animation automatically',flush=True)
wait(lambda:state().get('resting')=='true',35);move_clear();assert state()['resting']=='false' and state()['animating']=='true';frames('manual GIF after touching rested character')
print('PASS touching during rest resumes immediately and preserves drag animation',flush=True)
sh('input','keyevent','223');wait(lambda:state().get('visible')=='false');assert state()['animating']=='false';sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true' and state().get('animating')=='true')
print('PASS screen off stops animation; wake starts a fresh play period',flush=True)
settings();tap(by(text='산책 모드'));start();wait(lambda:state().get('wander')=='true');assert state()['wanderAtlasRetained']=='true'
wait(lambda:state().get('resting')=='true',40);position=bounds();frame=state()['wanderFrame'];time.sleep(3);assert bounds()==position and state()['wanderFrame']==frame
print('PASS stroll also stops movement/frame work while resting',flush=True)
settings();tap(by(text='수동 모드'));wait(lambda:state().get('wander')=='false');assert state()['wanderAtlasRetained']=='false'
switch('배터리 절약 모드',False);start();assert state()['animating']=='true' and state()['resting']=='false';stop()
print('PASS manual releases stroll atlas; disabling saving restores continuous GIF',flush=True)
print('REST108 DEVICE CHECKS PASSED',flush=True)
