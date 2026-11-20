from power108_helpers import *
from pathlib import Path
sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('settings','put','system','screen_off_timeout','600000')
settings();tap(by(text='수동 모드'));switch('배터리 절약 모드',False);top();tap(by(text='계속 실행'));switch('랜덤 전환 모드',True)
def details():
 top();tap(by(desc='랜덤 재생 상세 설정'));assert any(n.get('text')=='랜덤 재생 상세 설정' for n in ui().iter('node'))
def dialogscroll(down):
 nodes=[n for n in ui().iter('node') if n.get('class')=='android.widget.ScrollView' and n.get('scrollable')=='true']
 if not nodes:return
 l,t,r,b=map(int,re.findall(r'\d+',nodes[-1].get('bounds')));x=(l+r)//2
 sh('input','swipe',str(x),str(t+80 if down else b-80),str(x),str(b-80 if down else t+80),'180')
def dialogtop():
 for _ in range(3):
  if any(n.get('content-desc')=='선택한 이미지만 랜덤' for n in ui().iter('node')):return
  dialogscroll(True)
def draft(chosen):
 dialogtop();tap(by(desc='선택한 이미지만 랜덤'));seen=set()
 for _ in range(8):
  nodes=[n for n in ui().iter('node') if n.get('content-desc','').startswith('랜덤 선택 · ')]
  for n in nodes:
   name=n.get('content-desc').split(' · ',1)[1];seen.add(name)
   if (n.get('checked')=='true')!=(name in chosen):tap(n)
  if len(seen)==12:break
  dialogscroll(False)
 assert len(seen)==12,seen

details();draft(set());tap(by(text='선택 적용'));assert any(n.get('text')=='선택 적용' for n in ui().iter('node'));tap(by(text='취소'));assert by(text='전체 12개 사용') is not None
print('PASS empty selection blocked; cancel leaves all mode unchanged',flush=True)
details();draft({'갸웃 우사기','뿌릿 우사기'});tap(by(text='선택 적용'));assert by(text='선택한 2개 사용') is not None
top();n=by(desc='랜덤 전환 간격 초');tap(n);sh('input','keyevent','123');sh('input','keyevent','67','67','67','67');sh('input','text','1');sh('input','keyevent','4');tap(by(text='간격 적용'))
timer(5,'분');start();seen=set()
for _ in range(16):
 s=state();seen.add(int(s['character']));time.sleep(.25)
assert seen=={5,6},seen
print('PASS selected pool only, both eligible animations appear',flush=True)
settings();before=int(state()['timerRemainingMillis']);began=time.monotonic();details();draft({'두리번 우사기'});tap(by(text='선택 적용'));s=state();assert s['character']=='9';assert abs(int(s['timerRemainingMillis'])-(before-(time.monotonic()-began)*1000))<2200
sh('input','keyevent','3');time.sleep(2.5);assert state()['character']=='9';frames('singleton continues animating');a,b,c,d=bounds();sh('input','swipe',str((a+c)//2),str(b+(c-a)//2),'400','1200','600');frames('new cutout after drag')
print('PASS singleton replaces excluded character immediately; countdown not reset',flush=True)
sh('input','keyevent','223');wait(lambda:state().get('visible')=='false');sh('input','keyevent','224');sh('wm','dismiss-keyguard');wait(lambda:state().get('visible')=='true');frames('new cutout after unlock')
settings();details();dialogtop();tap(by(desc='전체 이미지 랜덤'));tap(by(text='선택 적용'));assert by(text='전체 12개 사용') is not None;sh('input','keyevent','3');seen=set()
for _ in range(100):
 seen.add(int(state()['character']));time.sleep(.2)
assert len(seen)>5 and any(i>=5 for i in seen),seen
print('PASS all mode includes old and new catalog:',sorted(seen),flush=True)
settings();details();dialogtop();tap(by(desc='선택한 이미지만 랜덤'));assert by(text='1개 선택됨 · 이 캐릭터만 계속 움직여요.') is not None;tap(by(text='취소'))
print('PASS all mode retains custom draft on reopen',flush=True)
print('GALLERY109 SELECTION CHECKS PASSED',flush=True)
