from power108_helpers import *
settings();tap(by(text='수동 모드'));switch('배터리 절약 모드',False);top();tap(by(desc='캐릭터 선택 목록'));tap(by(text='꼬리 흔들기'))
# Restore normal120dp size for visual QA (420dpi testAVD).
top();n=by(desc='캐릭터 크기');l,t,r,b=map(int,re.findall(r'\d+',n.get('bounds')));sh('input','tap',str(round(l+42+(r-l-84)/3)),str((t+b)//2))
timer(5,'분');n=by(desc='우사기 타이머 표시');assert n.get('checked')=='true','caption defaults on';start();s=state();assert s['timerCaptionVisible']=='true';a,b,c,d=bounds();assert abs((d-b)-(c-a)-53)<=1
print('PASS default caption enabled; extra strip below square image',flush=True)
settings();before=int(state()['timerRemainingMillis']);began=time.monotonic();switch('우사기 타이머 표시',False);s=state();assert s['timerCaptionVisible']=='false';a,b,c,d=bounds();assert d-b==c-a;assert abs(int(s['timerRemainingMillis'])-(before-(time.monotonic()-began)*1000))<1800
print('PASS hiding caption removes strip without resetting countdown',flush=True)
before=int(s['timerRemainingMillis']);began=time.monotonic();switch('우사기 타이머 표시',True);s=state();assert s['timerCaptionVisible']=='true';assert abs(int(s['timerRemainingMillis'])-(before-(time.monotonic()-began)*1000))<1800
sh('input','keyevent','3');a,b,c,d=bounds();sh('input','swipe',str((a+c)//2),str(b+(c-a)//2),'1050','2350','400');time.sleep(.3);a,b,c,d=bounds();assert d<=2274,(a,b,c,d)
frames('GIF with caption after bottom-edge drag')
# Move into clear wallpaper so the caption can be visually inspected.
sh('input','swipe',str((a+c)//2),str(b+(c-a)//2),'650','1100','350');time.sleep(.3)
if len(sys.argv)>3:
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
print('PASS caption follows drag, stays in safe bounds and preserves GIF playback',flush=True)
sh('input','keyevent','223');wait(lambda:state().get('visible')=='false');assert state()['timerCaptionVisible']=='false';sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true');assert state()['timerCaptionVisible']=='true'
print('PASS image and caption hide/wake together',flush=True)
settings();timer(1,'분');start();began=time.monotonic();wait(lambda:state().get('timerCaptionText','').endswith('초'));first=state()['timerCaptionText'];time.sleep(2.2);assert state()['timerCaptionText']!=first
wait(lambda:not state(),65);assert 'Usagi companion' not in sh('dumpsys','window','windows');print('PASS final-minute seconds update; expiry removes entire overlay in',round(time.monotonic()-began,2),'seconds',flush=True)
settings();top();tap(by(text='계속 실행'));start();assert state()['timerCaptionVisible']=='false';a,b,c,d=bounds();assert d-b==c-a;stop()
print('PASS unlimited mode has no caption',flush=True)
print('CAPTION108 DEVICE CHECKS PASSED',flush=True)
