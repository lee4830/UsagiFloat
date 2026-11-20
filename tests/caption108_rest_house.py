from power108_helpers import *
settings();tap(by(text='산책 모드'));switch('배터리 절약 모드',True);timer(5,'분')
if len(sys.argv)>3:
 time.sleep(2.5)
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
start();wait(lambda:state().get('wander')=='true' and state().get('visible')=='true');assert state()['timerCaptionVisible']=='true';first=bounds();wait(lambda:bounds()!=first)
print('PASS stroll and caption move as one bounded window',flush=True)
# Enter manual mode and position the overlay away from timer controls before waiting.
settings();tap(by(text='수동 모드'));top();tap(by(desc='캐릭터 선택 목록'));tap(by(text='파란 후드 우사기'));settings();n=by(desc='우사기 타이머 표시')
wait(lambda:state().get('resting')=='true',40);assert state()['animating']=='false';before=int(state()['timerRemainingMillis']);tap(n);s=state();assert s['resting']=='true' and s['animating']=='false' and s['timerCaptionVisible']=='false';tap(n);s=state();assert s['resting']=='true' and s['animating']=='false' and s['timerCaptionVisible']=='true';assert int(s['timerRemainingMillis'])<=before
print('PASS caption toggles preserve sleep period and countdown',flush=True)
sh('input','keyevent','3');a,b,c,d=bounds();x=(a+c)//2;y=b+(c-a)//2;sh('input','motionevent','DOWN',str(x),str(y));time.sleep(.1);sh('input','motionevent','MOVE','650','1000');sh('input','motionevent','UP','650','1000');frames('WebP resumes and drags inside caption container')
a,b,c,d=bounds();sh('input','motionevent','DOWN',str((a+c)//2),str(b+(c-a)//2));time.sleep(.8)
s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi home\}:([\s\S]*?)(?=  Window #|\Z)',s);assert m
l,t,r,b=map(int,re.search(r'mFrame=\[(\d+),(\d+)\]\[(\d+),(\d+)\]',m[1]).groups());hx=(l+r)//2;hy=round(t+(b-t)*.42)
sh('input','motionevent','MOVE',str(hx),str(hy));time.sleep(.2);sh('input','motionevent','UP',str(hx),str(hy));wait(lambda:not state());assert 'Usagi companion' not in sh('dumpsys','window','windows')
print('PASS house dismissal with visible caption removes both image and label',flush=True)
settings();switch('배터리 절약 모드',False);top();tap(by(text='계속 실행'))
print('CAPTION108 REST/HOUSE CHECKS PASSED',flush=True)
