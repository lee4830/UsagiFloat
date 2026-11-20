"""Repair the source walk mask without changing source RGB or stroke drawing."""
from pathlib import Path
import cv2,numpy as np
from PIL import Image
BASE=Path('work/video-reference');OUT=BASE/'processed'
def mask(rgb):
 r,g,b=rgb.astype(float).transpose(2,0,1)
 line=((r<160)&(g<140)&(b<135)&(r>g*1.08)).astype('uint8')*255
 # Original linework has intentional gaps. Close only the mask (never RGB).
 closed=cv2.morphologyEx(line,cv2.MORPH_CLOSE,np.ones((11,11),np.uint8))
 contours,_=cv2.findContours(closed,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
 alpha=np.zeros_like(line);cv2.drawContours(alpha,[max(contours,key=cv2.contourArea)],-1,255,-1)
 # Closing the line gaps also bridged the narrow space BETWEEN the ears.
 # Recover only top-connected background inside the two pink-ear centers.
 pink=((r>175)&(g>110)&(r-g>25)&(r-b>12)&(b>110)).astype('uint8')
 n,labels,stats,centers=cv2.connectedComponentsWithStats(pink)
 ears=sorted([j for j in range(1,n) if stats[j,3]>40 and stats[j,4]>200],key=lambda j:centers[j,0])
 if len(ears)!=2:raise ValueError('Expected two original pink ears')
 left,right=ears;x0=int(centers[left,0]);x1=int(centers[right,0])+1
 bottom=min(rgb.shape[0],max(stats[j,1]+stats[j,3] for j in ears)+12)
 free=(line[:bottom,x0:x1]==0).astype('uint8')
 regions,ids,_,_=cv2.connectedComponentsWithStats(free,connectivity=4)
 exterior=np.unique(ids[0]);exterior=exterior[exterior!=0]
 gap=np.isin(ids,exterior);alpha[:bottom,x0:x1][gap]=0
 # Fail closed if a mask/threshold change leaks into either pink ear interior.
 for j in ears:
  if np.mean(alpha[labels==j]>0)<.99:raise ValueError('Ear interior damaged')
 return alpha
if __name__=='__main__':
 cap=cv2.VideoCapture(str(BASE/'compilation-opening.mp4'));frames=[]
 for i in range(12):
  cap.set(cv2.CAP_PROP_POS_FRAMES,858+i);ok,bgr=cap.read();assert ok
  rgb=cv2.cvtColor(bgr,cv2.COLOR_BGR2RGB)[40:650,480:810];alpha=mask(rgb)
  # Two tall pink ear interiors must survive on every frame; cheeks are shorter.
  r,g,b=rgb.astype(float).transpose(2,0,1)
  pink=((r>175)&(g>110)&(r-g>25)&(r-b>12)&(b>110)).astype('uint8')
  n,labels,stats,_=cv2.connectedComponentsWithStats(pink)
  ears=[j for j in range(1,n) if stats[j,3]>40 and stats[j,4]>200]
  assert len(ears)==2,(i,ears)
  for j in ears:assert np.mean(alpha[labels==j]>0)>.99,(i,j)
  frames.append(Image.fromarray(np.dstack([rgb,alpha])))
 cap.release();boxes=[f.getbbox() for f in frames]
 union=(min(b[0] for b in boxes),min(b[1] for b in boxes),max(b[2] for b in boxes),max(b[3] for b in boxes));scale=224/max(union[2]-union[0],union[3]-union[1]);atlas=Image.new('RGBA',(2048,512))
 for i,f in enumerate(frames):
  f=f.crop(union).convert('RGBa');f=f.resize((round(f.width*scale),round(f.height*scale)),Image.Resampling.LANCZOS).convert('RGBA');canvas=Image.new('RGBA',(256,256));canvas.paste(f,((256-f.width)//2,240-f.height));atlas.paste(canvas,(i%8*256,i//8*256))
 atlas.save(OUT/'walk-atlas.png');print('12/12 frames: both original ear interiors retained >99%; source RGB unmodified')
