"""Mode-specific settings, random interval disclosure and action ordering."""
from drag_animation_smoke import *
def top():
 for _ in range(3):sh('input','swipe','30','500','30','2000','150')
def collect():
 top();found=[]
 for _ in range(3):
  found.extend(list(ui().iter('node')));sh('input','swipe','30','1900','30','500','150')
 return found
def has(nodes,label):return any(n.get('text')==label or n.get('content-desc')==label for n in nodes)
def random(value):
 top();n=find('랜덤으로 바꾸기')
 if (n.get('checked')=='true')!=value:tap(n)
open_app();tap(find('수동 모드'));random(False);nodes=collect()
assert not has(nodes,'산책 속도'),'manual mode exposes stroll settings'
assert has(nodes,'캐릭터 선택 목록') and has(nodes,'랜덤 전환 모드')
assert not has(nodes,'간격 적용') and not has(nodes,'랜덤 전환 간격 초'),'interval controls should be absent when random is off'
print('PASS manual only; random-off interval controls hidden',flush=True)
random(True);nodes=collect();assert has(nodes,'간격 적용') and has(nodes,'랜덤 전환 간격 초')
assert all(n.get('enabled')=='true' for n in nodes if n.get('text')=='간격 적용' or n.get('content-desc')=='랜덤 전환 간격 초')
print('PASS random-on interval controls visible and enabled',flush=True)
top();tap(find('산책 모드'));nodes=collect();assert has(nodes,'산책 속도')
for label in ['캐릭터 선택 목록','랜덤 전환 모드','간격 적용','랜덤 전환 간격 초']:assert not has(nodes,label),label
print('PASS stroll hides all manual and random controls',flush=True)
# Common controls appear in user-requested traversal order, regardless of scrolling.
labels=[]
for n in nodes:
 t=n.get('text')
 if t in ['시작 · 우사기 띄우기','재생 종료','실행 · 표시 권한 확인'] and t not in labels:labels.append(t)
assert labels==['시작 · 우사기 띄우기','재생 종료','실행 · 표시 권한 확인'],labels
print('PASS Start → Stop → overlay permission order',flush=True)
top();tap(find('수동 모드'));nodes=collect();assert has(nodes,'간격 적용')
random(False);nodes=collect();assert not has(nodes,'간격 적용')
if len(sys.argv)>3:
 top()
 with open(sys.argv[3],'wb') as f:subprocess.run(ADB+['exec-out','screencap','-p'],stdout=f,check=True)
print('SETTINGS106 DEVICE CHECKS PASSED',flush=True)
