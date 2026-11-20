from drag_animation_smoke import *
def top():
 for _ in range(3):sh('input','swipe','30','500','30','2000','120')
def by(text=None,desc=None):
 for _ in range(7):
  for n in ui().iter('node'):
   if (text is not None and n.get('text')==text) or (desc is not None and n.get('content-desc')==desc):return n
  sh('input','swipe','30','1950','30','600','150')
 raise AssertionError(('missing',text,desc))
def state():return dict(re.findall(r'^\s*(\w+)=(\S+)',sh('dumpsys','activity','service',PKG+'/.CompanionService'),re.M))
def wait(predicate,seconds=12):
 end=time.monotonic()+seconds
 while time.monotonic()<end:
  if predicate():return
  time.sleep(.2)
 raise AssertionError(('timeout',state()))
def switch(desc,value):
 top();n=by(desc=desc)
 if (n.get('checked')=='true')!=value:tap(n)
def settings():
 if state().get('wander')=='false':
  a,b,c,d=bounds();sh('input','swipe',str((a+c)//2),str((b+d)//2),'950','260','200')
 open_app();top()
def amount(value):
 tap(by(desc='자동 종료 시간 값'));sh('input','keyevent','123');sh('input','keyevent','67','67','67','67','67','67');sh('input','text',str(value));sh('input','keyevent','4')
def timer(value,unit):
 top();tap(by(text='시간 지정'));tap(by(desc='자동 종료 시간 단위'));tap(by(text=unit));amount(value);tap(by(text='시간 적용'))
def start():tap(by(text='시작 · 우사기 띄우기'));wait(lambda:state().get('visible')=='true')
def stop():settings();tap(by(text='재생 종료'));wait(lambda:not state())
