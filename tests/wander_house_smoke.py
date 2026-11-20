"""Dismiss an already-running stroll; wait for visibility after settings closes."""
from drag_animation_smoke import *
def state():return dict(re.findall(r'^\s*(\w+)=(\S+)',sh('dumpsys','activity','service',PKG+'/.CompanionService'),re.M))
end=time.monotonic()+5
while time.monotonic()<end and state().get('visible')!='true':time.sleep(.1)
assert state().get('wander')=='true' and state().get('visible')=='true'
a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2;sh('input','motionevent','DOWN',str(x),str(y))
end=time.monotonic()+3;m=None
while time.monotonic()<end:
 s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi home\}:([\s\S]*?)(?=  Window #|\Z)',s)
 if m:break
 time.sleep(.1)
assert m,'house missing after visible stroll hold'
l,t,r,b=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]).groups());hx=(l+r)//2;hy=round(t+(b-t)*.42)
sh('input','motionevent','MOVE',str(hx),str(hy));time.sleep(.3);sh('input','motionevent','UP',str(hx),str(hy));time.sleep(1.5);assert not state()
print('PASS house dismissal after visible stroll; service stays stopped',flush=True)
