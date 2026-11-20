"""Stroll must restore a running GIF immediately when manual random mode resumes."""
from drag_animation_smoke import *
def state():return dict(re.findall(r'^\s*(\w+)=(\S+)',sh('dumpsys','activity','service',PKG+'/.CompanionService'),re.M))
def choose_mode(text):
 open_app();tap(find(text))
def mode_random(value):
 n=find('랜덤으로 바꾸기')
 if (n.get('checked')=='true')!=value:tap(n)
choose_mode('수동 모드');mode_random(True)
choose_mode('산책 모드');tap(find('시작 · 우사기 띄우기'));time.sleep(1)
assert state().get('wander')=='true'
open_app();time.sleep(1.3);assert state().get('visible')=='false','stroll must leave settings unobstructed'
tap(find('수동 모드'));time.sleep(.3)
s=state();assert s.get('wander')=='false' and s.get('animating')=='true' and int(s['character'])>=0,s
print('STROLL TO MANUAL RANDOM RESTORES GIF IMMEDIATELY',flush=True)
