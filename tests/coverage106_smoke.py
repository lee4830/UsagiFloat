"""Verify a real stroll visits all9screen regions on a dedicated1080x2400emulator."""
from drag_animation_smoke import *
open_app();tap(find('산책 모드'));tap(find('시작 · 우사기 띄우기'));time.sleep(1.5)
seen=set();deadline=time.monotonic()+160;xs=[];ys=[]
while time.monotonic()<deadline and len(seen)<9:
 a,b,c,d=bounds();w=c-a;h=d-b
 col=min(2,max(0,int(3*a/(1080-w))));row=min(2,max(0,int(3*(b-63)/(2400-126-h-63))))
 seen.add((col,row));xs.append(a);ys.append(b);time.sleep(.25)
assert len(seen)==9,seen
assert max(xs)-min(xs)>.7*(1080-w),(min(xs),max(xs))
assert max(ys)-min(ys)>.7*(2400-126-h-63),(min(ys),max(ys))
print('PASS all9screen regions; position extents:',min(xs),max(xs),min(ys),max(ys),flush=True)
open_app();time.sleep(1.3);tap(find('재생 종료'))
