"""Detect frozen rendered frames after window movement; explicit emulator only.
The test fails if drag leaves the drawable reporting running but pixels stop changing.
Uses Pillow and the real installed APK; never alters application playback internals.
"""
import subprocess,sys,time,re,io,hashlib,xml.etree.ElementTree as E
from PIL import Image
ADB=[sys.argv[1],'-s',sys.argv[2]];PKG='kr.personal.usagifloat'
assert sys.argv[2].startswith('emulator-'), 'Use a dedicated emulator'
def sh(*a):return subprocess.check_output(ADB+['shell',*a],text=True,timeout=45)
def bounds():
 s=sh('dumpsys','window','windows');m=re.search(r'Window #\d+ Window\{[^\n]* Usagi companion\}:([\s\S]*?)(?=  Window #|\Z)',s)
 assert m,'overlay missing'
 return tuple(map(int,re.search(r'mFrame=\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]',m[1]).groups()))
def ui():
 sh('uiautomator','dump','/sdcard/drag.xml');return E.fromstring(sh('cat','/sdcard/drag.xml'))
def tap(n):
 x,y,r,b=map(int,re.findall(r'\d+',n.get('bounds')));sh('input','tap',str((x+r)//2),str((y+b)//2))
def find(text):
 for _ in range(6):
  for n in ui().iter('node'):
   if n.get('text')==text:return n
  sh('input','swipe','100','1800','100','600','250')
 raise AssertionError(text)
def frames(label):
 box=bounds();hashes=[];start=time.monotonic()
 while time.monotonic()-start<2.2 or len(hashes)<6:
  raw=subprocess.check_output(ADB+['exec-out','screencap','-p'],timeout=20)
  im=Image.open(io.BytesIO(raw)).convert('RGB').crop(box)
  hashes.append(hashlib.sha256(im.tobytes()).hexdigest());time.sleep(.07+.031*(len(hashes)%5))
 count=len(set(hashes));print(label,'unique frames',count,'samples',len(hashes),flush=True)
 assert count>=2,(label,'rendered animation frozen',count,sh('dumpsys','activity','service',PKG+'/.CompanionService'))
def open_app():
 sh('am','start','-W','-f','0x14000000','-n',PKG+'/.MainActivity');sh('input','swipe','100','700','100','2100','200')
def main():
 sh('input','keyevent','224');sh('wm','dismiss-keyguard');sh('settings','put','system','screen_off_timeout','600000')
 screen_width,screen_height=map(int,re.findall(r'(\d+)x(\d+)',sh('wm','size'))[-1])
 open_app();tap(find('수동 모드'))
 for n in ui().iter('node'):
  if n.get('content-desc')=='랜덤 전환 모드' and n.get('checked')=='true':tap(n)
 names=['꼬리 흔들기','말랑 우사기','응원 우사기','팔짱 우사기','파란 후드 우사기']
 for i,name in enumerate(names):
  open_app();tap(next(n for n in ui().iter('node') if n.get('content-desc')=='캐릭터 선택 목록'));tap(find(name));tap(find('시작 · 우사기 띄우기'));time.sleep(1)
  if 'sizing' in sys.argv[3:]:
   state=dict(re.findall(r'^\s*(\w+)=(\S+)',sh('dumpsys','activity','service',PKG+'/.CompanionService'),re.M));a,b,c,d=bounds();assert 0<int(state['animationWidth'])<=c-a and 0<int(state['animationHeight'])<=d-b,state;print(name,'decoded',state['animationWidth'],state['animationHeight'],flush=True)
  frames(name+' before')
  for j in range(2):
   a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2
   dest_x=round(screen_width*(.28 if x>screen_width/2 else .72));dest_y=round(screen_height*.30)
   sh('input','swipe',str(x),str(y),str(dest_x),str(dest_y),str(1400 if j==0 else 250));time.sleep(.2)
   moved=bounds();assert abs(moved[0]-a)+abs(moved[1]-b)>20,('gesture did not move overlay',name,j)
   frames(name+f' after drag {j}')
 # Keep a gesture active after movement: frames must continue without ACTION_UP.
 a,b,c,d=bounds();x=(a+c)//2;y=(b+d)//2
 sh('input','motionevent','DOWN',str(x),str(y))
 try:
  sh('input','motionevent','MOVE',str(x-120),str(y+100));time.sleep(.3)
  moved=bounds();assert abs(moved[0]-a)+abs(moved[1]-b)>20,'held gesture did not move'
  frames('finger still down after movement')
 finally:sh('input','motionevent','UP',str(x-120),str(y+100))
 frames('after releasing held drag')
 sh('input','keyevent','223');time.sleep(1.3);sh('input','keyevent','224');sh('wm','dismiss-keyguard');time.sleep(1.3);frames('after unlock')
 print('DRAG ANIMATION PIXEL CHECKS PASSED',flush=True)

if __name__=='__main__':main()
