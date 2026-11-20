from power108_helpers import *
def popup_scroll(down):
 nodes=[n for n in ui().iter('node') if n.get('scrollable')=='true']
 if not nodes:return False
 l,t,r,b=map(int,re.findall(r'\d+',nodes[-1].get('bounds')));x=(l+r)//2
 sh('input','swipe',str(x),str(t+60 if down else b-60),str(x),str(b-60 if down else t+60),'180');return True
def choose(name):
 for _ in range(3):popup_scroll(True)
 for _ in range(5):
  for n in ui().iter('node'):
   if n.get('text')==name:tap(n);return
  popup_scroll(False)
 raise AssertionError(('character missing',name))
sh('input','keyevent','224');sh('wm','dismiss-keyguard');settings();tap(by(text='수동 모드'))
# UI only: switch fixed character and inspect the actual animated pixels for all12.
switch('랜덤 전환 모드',False);top();tap(by(text='계속 실행'));stop()
names=['꼬리 흔들기','말랑 우사기','응원 우사기','팔짱 우사기','파란 후드 우사기','갸웃 우사기','뿌릿 우사기','숲속 우사기','꼬마 우사기','두리번 우사기','팔랑 귀 우사기','춤추는 우사기']
for i,name in enumerate(names):
 settings();top();tap(by(desc='캐릭터 선택 목록'));choose(name);start();assert state()['character']==str(i);frames(name)
 s=state();assert s['animationWidth']==s['animationHeight'];a,b,c,d=bounds();assert int(s['animationWidth'])<=c-a
 settings();tap(by(text='재생 종료'));wait(lambda:not state())
print('GALLERY109 DEVICE CHECKS PASSED',flush=True)
