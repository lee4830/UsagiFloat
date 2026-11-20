from power108_helpers import *
if state():stop()
settings();tap(by(text='수동 모드'));switch('배터리 절약 모드',False);timer(1,'분')
if len(sys.argv)>3:
 time.sleep(2.5)
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
start();began=time.monotonic()
assert state()['timerEnabled']=='true' and state()['hardwareAccelerated']=='true'
# Native rendering probe while the final signed APK's countdown is running.
assert 0<int(state()['animationWidth'])<=bounds()[2]-bounds()[0]
frames('final APK manual GIF during timer')
wait(lambda:not state(),65);elapsed=time.monotonic()-began;assert 58<=elapsed<=68,elapsed
print('PASS final signed APK auto-stops after',round(elapsed,2),'seconds',flush=True)
settings();top();tap(by(text='계속 실행'))
print('TIMER108 FINAL APK CHECK PASSED',flush=True)
