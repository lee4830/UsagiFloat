"""Offline 30fps interpolation; no optical-flow work on the phone.
Run with original build_clips.py output directory and drawable-nodpi output directory.
"""
from pathlib import Path
import sys, math, json
import cv2
import numpy as np
from PIL import Image, ImageDraw
src,out=map(Path,sys.argv[1:3]);out.mkdir(parents=True,exist_ok=True)
names=['walk','look','wiggle','hop'];counts=[12,30,78,30]
# Main rounded body reference widths (exclude ears, hands and motion marks).
widths=[115,150,151,153];target=138
xx,yy=np.meshgrid(np.arange(256,dtype=np.float32),np.arange(256,dtype=np.float32))
def affine_rgba(im,matrix):
 # Warp premultiplied colors to avoid dark fringes at transparent edges.
 p=im.astype(np.float32)/255;p[:,:,:3]*=p[:,:,3:]
 p=cv2.warpAffine(p,matrix,(256,256),flags=cv2.INTER_CUBIC)
 p=np.clip(p,0,1);p[:,:,:3]/=np.maximum(p[:,:,3:],1/255)
 return np.uint8(np.clip(p*255,0,255))
def tween(a,b,t):
 # Preserve ears, facial lines and original drawing; interpolate only translation.
 # Dense flow on these small hand-drawn frames caused broken/ghosted ears.
 def center(im):
  alpha=im[:,:,3].astype(float);return np.array([(xx*alpha).sum(),(yy*alpha).sum()])/alpha.sum()
 ca,cb=center(a),center(b);source=a if t<.5 else b;origin=ca if t<.5 else cb
 shift=ca*(1-t)+cb*t-origin
 return affine_rgba(source,np.float32([[1,0,shift[0]],[0,1,shift[1]]]))
def atlas(frames,path):
 sheet=Image.new('RGBA',(2048,math.ceil(len(frames)/8)*256))
 for i,f in enumerate(frames):
  f=f.copy();alpha=f[:,:,3]
  # Subpixel outer-edge reduction only; eyes/mouth/internal stroke RGB untouched.
  f[:,:,3]=np.uint8(.65*alpha+.35*cv2.erode(alpha,np.ones((3,3),np.uint8)))
  sheet.paste(Image.fromarray(f),(i%8*256,i//8*256))
 sheet.save(path)
clips=[];report=[]
for name,n,width in zip(names,counts,widths):
 sheet=Image.open(src/f'{name}-atlas.png').convert('RGBA')
 raw=[sheet.crop((i%8*256,i//8*256,i%8*256+256,i//8*256+256)) for i in range(n)]
 boxes=[im.getbbox() for im in raw];ground=np.median([b[3] for b in boxes]);cx=np.median([(b[0]+b[2])/2 for b in boxes]);scale=target/width
 frames=[]
 for im,b in zip(raw,boxes):
  # Remove scene vertical travel, retain a gentle 25% bounce in walking.
  dy=(ground-b[3])*.75 if name=='walk' else 0
  mat=np.float32([[scale,0,128-cx*scale],[0,scale,233-ground*scale+dy*scale]])
  frames.append(affine_rgba(np.array(im),mat))
 # Cluster source holds; avoid treating compression noise as a new pose.
 keys=[0]
 for i in range(1,n):
  delta=np.mean(np.abs(frames[i].astype(float)-frames[keys[-1]].astype(float)))
  if delta>3.8:keys.append(i)
 result=[f.copy() for f in frames]
 # Smooth only the last two held frames before a new pose, preserving long pauses.
 pairs=list(zip(keys,keys[1:]))
 if name=='walk':pairs.append((keys[-1],n))
 for a,b in pairs:
  start=max(a,b-3);last=frames[b%n]
  for i in range(start+1,b):
   t=(i-start)/(b-start);t=t*t*(3-2*t)
   result[i]=tween(frames[start],last,t)
 clips.append(result)
 atlas(result,out/f'motion_{"bow" if name=="hop" else name}.png')
 report.append({'clip':name,'body_reference_width':width,'scale':scale,'target_body_width':target,'key_frames':keys,'count':n})
bridges=[]
for a,b in [(0,1),(0,2),(0,3),(1,0),(2,0),(3,0)]:
 # Different clips include different outfits/facial drawings: do not morph or
 # double-expose these. A short settle preserves one original drawing per frame,
 # with continuous body-center translation and a subtle uniform landing scale.
 first,last=clips[a][-1],clips[b][0]
 def anchor(im):
  mask=im[:,:,3].astype(float);return np.array([(xx*mask).sum(),(yy*mask).sum()])/mask.sum()
 ca,cb=anchor(first),anchor(last)
 for t in [.25,.5,.75]:
  source=first if t<.5 else last;origin=ca if t<.5 else cb
  center=ca*(1-t)+cb*t;scale=1-.025*math.sin(math.pi*t)
  matrix=np.float32([[scale,0,center[0]-origin[0]*scale],[0,scale,center[1]-origin[1]*scale]])
  bridges.append(affine_rgba(source,matrix))
atlas(bridges,out/'motion_bridges.png')
(Path(__file__).parent/'smooth-motion.json').write_text(json.dumps(report,indent=2))
# Inspect endpoints, halfway poses, and every transition on a neutral backdrop.
preview=Image.new('RGB',(6*256,5*280),'#546170');d=ImageDraw.Draw(preview)
for row,fs in enumerate(clips):
 for col,i in enumerate(np.linspace(0,len(fs)-1,6,dtype=int)):
  im=Image.fromarray(fs[i]);preview.paste(im,(col*256,row*280),im);d.text((col*256+5,row*280+256),f'{names[row]} {i}',fill='white')
for col in range(6):
 im=Image.fromarray(bridges[col*3+1]);preview.paste(im,(col*256,4*280),im);d.text((col*256+5,4*280+256),f'bridge {col}',fill='white')
preview.save(src/'smooth-preview.jpg')
print('Prepared 150 normalized/interpolated frames + 18 bridge frames at30fps')
