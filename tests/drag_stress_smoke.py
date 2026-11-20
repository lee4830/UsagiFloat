"""Repeated drag, delayed playback, hold/cancel and house dismissal on emulator."""
from drag_animation_smoke import *
open_app();tap(next(n for n in ui().iter('node') if n.get('content-desc')=='캐릭터 선택 목록'));tap(find('꼬리 흔들기'));tap(find('시작 · 우사기 띄우기'));time.sleep(1)
for i in range(10):
 a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2;dest=300 if x>540 else 770
 sh('input','swipe',str(x),str(y),str(dest),'720',str(200 if i%2 else 900));time.sleep(.1)
 moved=bounds();assert abs(moved[0]-a)>20
# Separate late observation windows exclude one last frame being mistaken for continued playback.
for i in range(3):
 time.sleep(3);frames(f'delayed playback window {i}')
a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2
sh('input','motionevent','DOWN',str(x),str(y));time.sleep(.8)
s=sh('dumpsys','window','windows');assert 'Usagi home' in s,'long press did not show house'
frames('while holding with house visible')
sh('input','motionevent','UP',str(x),str(y));time.sleep(.5);assert 'Usagi home' not in sh('dumpsys','window','windows');frames('after releasing outside house')
# Drop at actual home target circle centre. Touch starts at character centre.
sh('input','motionevent','DOWN',str(x),str(y));time.sleep(.8)
s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi home\}:([\s\S]*?)(?=  Window #|\Z)',s)
l,t,r,b=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]).groups());hx=(l+r)//2;hy=round(t+(b-t)*.42)
sh('input','motionevent','MOVE',str(hx),str(hy));time.sleep(.3);sh('input','motionevent','UP',str(hx),str(hy));time.sleep(1)
assert 'Usagi companion' not in sh('dumpsys','window','windows')
assert 'Client:' not in sh('dumpsys','activity','service',PKG+'/.CompanionService')
print('DRAG STRESS AND HOUSE CHECKS PASSED',flush=True)
