from power108_helpers import *
sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('settings','put','system','screen_off_timeout','600000')
settings();tap(by(text='수동 모드'));switch('랜덤 전환 모드',False);switch('배터리 절약 모드',False);top();tap(by(text='계속 실행'))
top();tap(by(desc='캐릭터 선택 목록'));tap(by(text='파란 후드 우사기'));start()
s=state();assert s['timerEnabled']=='false' and s['hardwareAccelerated']=='true',s
assert 0<int(s['animationWidth'])<=bounds()[2]-bounds()[0]<480,s
print('PASS unlimited default; hardware acceleration;480px animation decodes to',s['animationWidth'],flush=True)
settings();timer(2,'시간');s=state();assert 7_180_000<int(s['timerRemainingMillis'])<=7_200_000,s
print('PASS hours accepted and live countdown applied',flush=True)
timer(1,'일');s=state();assert 86_380_000<int(s['timerRemainingMillis'])<=86_400_000,s
before=int(s['timerRemainingMillis']);amount(0);tap(by(text='시간 적용'));assert before-25000<int(state()['timerRemainingMillis'])<=before
print('PASS days use long duration; invalid0does not overwrite/restart',flush=True)
timer(1,'분')
if len(sys.argv)>3:
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
start();began=time.monotonic();assert 58_000<int(state()['timerRemainingMillis'])<=60_000
settings();top();tap(by(desc='캐릭터 선택 목록'));tap(by(text='꼬리 흔들기'))
remaining=int(state()['timerRemainingMillis']);elapsed=(time.monotonic()-began)*1000;assert abs(remaining-(60000-elapsed))<2000,(remaining,elapsed)
sh('input','keyevent','3');wait(lambda:not state(),max(1,remaining/1000+8));actual=time.monotonic()-began;assert 58<=actual<=68,actual
print('PASS1minute automatic service/overlay shutdown; unrelated character update did not reset:',round(actual,2),'seconds',flush=True)
settings();start();sh('input','keyevent','223');time.sleep(64);sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:not state(),5);time.sleep(2);assert not state()
print('PASS screen-off time counts; expired overlay does not return on wake',flush=True)
settings();top();tap(by(text='계속 실행'));start();assert state()['timerEnabled']=='false';time.sleep(2);assert state();stop()
print('PASS unlimited cancels saved timer; manual Stop works',flush=True)
print('TIMER108 DEVICE CHECKS PASSED',flush=True)
