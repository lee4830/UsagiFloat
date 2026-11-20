from pathlib import Path
import cv2,numpy as np
from PIL import Image,ImageDraw
BASE=Path('work/video-reference')
def get(video,t):
 c=cv2.VideoCapture(str(BASE/video));c.set(cv2.CAP_PROP_POS_MSEC,t*1000);ok,bgr=c.read();c.release();assert ok;return cv2.cvtColor(bgr,cv2.COLOR_BGR2RGB)
def cut(rgb,box):
 fullwidth=rgb.shape[1];x,y,w,h=box;rgb=rgb[y:y+h,x:x+w];sm=cv2.GaussianBlur(rgb,(3,3),.7);r,g,b=sm.astype(float).transpose(2,0,1)
 cream=(r>175)&(g>155)&(r-b>33)&(g-b>24)&(r-g<65)
 pink=(r>175)&(g>110)&(r-g>25)&(r-b>12)&(b>110)
 seed=(cream|pink).astype('uint8');seed=cv2.morphologyEx(seed,cv2.MORPH_CLOSE,np.ones((7,7),np.uint8))
 n,l,st,_=cv2.connectedComponentsWithStats(seed,8);seed=(l==1+np.argmax(st[1:,cv2.CC_STAT_AREA])).astype('uint8')
 cs,_=cv2.findContours(seed,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE);region=np.zeros_like(seed);cv2.drawContours(region,cs,-1,1,-1)
 reach=cv2.dilate(region,np.ones((29,29) if fullwidth>1000 else (17,17),np.uint8));m=np.full(seed.shape,cv2.GC_PR_BGD,np.uint8);m[region>0]=cv2.GC_PR_FGD;m[reach==0]=cv2.GC_BGD;m[cv2.erode(seed,np.ones((3,3),np.uint8))>0]=cv2.GC_FGD
 cv2.grabCut(rgb,m,None,np.zeros((1,65)),np.zeros((1,65)),4,cv2.GC_INIT_WITH_MASK)
 a=((m==cv2.GC_FGD)|(m==cv2.GC_PR_FGD)).astype('uint8');a[((r<155)&(g<135)&(b<130)&((r>g*1.08) if fullwidth<1000 else True))&(reach>0)]=1;cs,_=cv2.findContours(a,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE);a[:]=0;cv2.drawContours(a,[max(cs,key=cv2.contourArea)],-1,255,-1)
 return Image.fromarray(np.dstack([rgb,a]))
if __name__=='__main__':
 samples=[('dance','short.mp4',4.5,(130,340,350,440)),('walk','compilation-opening.mp4',40,(280,120,370,530)),('hop','compilation-opening.mp4',30,(480,30,330,420)),('look','compilation-opening.mp4',28,(450,70,350,520))]
 for name,v,t,box in samples:
  fr=cut(get(v,t),box);fr.save(BASE/'processed'/f'{name}.png')
  bg=Image.new('RGB',fr.size,'#546170');bg.paste(fr,mask=fr.getchannel('A'));bg.save(BASE/'processed'/f'{name}-preview.png')
