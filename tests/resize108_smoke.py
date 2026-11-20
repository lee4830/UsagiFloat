from power108_helpers import *
settings();top();tap(by(desc='캐릭터 선택 목록'));tap(by(text='파란 후드 우사기'))
for side,expected in [('left',80),('right',200)]:
 top();n=by(desc='캐릭터 크기');l,t,r,b=map(int,re.findall(r'\d+',n.get('bounds')));sh('input','tap',str(l+2 if side=='left' else r-2),str((t+b)//2));time.sleep(.4)
 s=state();a,b,c,d=bounds();assert int(s['animationWidth'])<=c-a,s
 expected_pixels=round(expected*420/160);assert abs(c-a-expected_pixels)<=1,(side,c-a,expected_pixels)
 assert int(s['animationWidth'])==min(480,expected_pixels),s
 print('PASS resize',expected,'dp -> decoded',s['animationWidth'],'px',flush=True)
sh('input','keyevent','3');frames('GIF after size changes')
print('RESIZE108 DEVICE CHECKS PASSED',flush=True)
