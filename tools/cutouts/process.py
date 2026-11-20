from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np, cv2,json,os
ROOT=Path(__file__).resolve().parent/'generated'
ROOT.mkdir(parents=True,exist_ok=True)
INPUT=Path(os.environ.get('USAGI_SOURCE_DIR',str(Path.home()/'Downloads')))
SOURCES={'closeup':'f8057a7825ae42459d58ec365b09050a.gif','hood':'a805c318ec76636da281cc625a1cae6b.gif'}
def largest(mask):
 count,labels,stats,_=cv2.connectedComponentsWithStats(mask.astype('uint8'),8)
 if count<2:return mask.astype('uint8')
 return (labels==1+np.argmax(stats[1:,cv2.CC_STAT_AREA])).astype('uint8')
def fill(mask):
 contours,_=cv2.findContours(mask,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
 out=np.zeros_like(mask);cv2.drawContours(out,contours,-1,1,cv2.FILLED);return out
def cut(frame,kind):
 rgb=np.array(frame.convert('RGB'));smooth=cv2.GaussianBlur(rgb,(5,5),1.2);r,g,b=smooth.astype(float).transpose(2,0,1)
 # Character-specific interior colours, not generative reconstruction.
 cream=(r>180)&(g>170)&(r-b>30)&(g-b>25)&(r-g<60)
 blue=(b>155)&(b-r>12)&(g>120)&(r>75)
 pink=(r>160)&(r-g>30)&(r-b>15)
 core=cream|(blue if kind=='hood' else pink)
 core=cv2.morphologyEx(core.astype('uint8'),cv2.MORPH_CLOSE,np.ones((5,5),np.uint8))
 prior=fill(largest(core))
 # Include disconnected costume and face regions separated by thick ink.
 wide=cv2.dilate(core,np.ones((13,13),np.uint8))
 region=fill(largest(wide))
 if kind=='hood':
  # Keep ink connected to the colour silhouette, including hands and the wand.
  # A colour-only classifier must never discard brown facial/contour strokes.
  dark=(smooth.max(axis=2)<150).astype('uint8')
  mask=fill(largest((region|dark).astype('uint8')))
  pink_bg=((r>195)&(r-g>25)&(b-g>-8)).astype('uint8')
  n,labels=cv2.connectedComponents(pink_bg,8)
  edge_labels=np.unique(np.concatenate([labels[0],labels[-1],labels[:,0],labels[:,-1]]))
  edge_labels=edge_labels[edge_labels!=0]
  exterior=np.isin(labels,edge_labels)
  mask[exterior]=0
  # The cropped cheek sometimes touches the canvas edge; it is still part of
  # the face, not exterior pink. Restore the convex cream-face silhouette.
  face=largest(cream.astype('uint8'));face_hull=np.zeros_like(face)
  contours,_=cv2.findContours(face,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
  if contours:cv2.drawContours(face_hull,[cv2.convexHull(max(contours,key=cv2.contourArea))],-1,1,cv2.FILLED)
  mask[face_hull>0]=1
  # Decorative bunting sits to the right of the hood, above the hands.
  # Follow the blue hood edge there, retaining its dark outline margin.
  for y in range(int(rgb.shape[0]*.40)):
   xs=np.flatnonzero(blue[y])
   if len(xs) and not np.any(face_hull[:int(rgb.shape[0]*.40),-3:]):mask[y,min(rgb.shape[1],int(xs[-1])+12):]=0
  green=((g-r>8)&(g-b>8)).astype('uint8')
  flags=cv2.dilate(green,np.ones((11,11),np.uint8))
  protected=cv2.dilate((blue.astype('uint8')|face_hull),np.ones((9,9),np.uint8))
  mask[(flags>0)&(protected==0)]=0
  mask=largest(mask)
  return Image.fromarray(np.dstack([rgb,mask*255]).astype('uint8'))
 seed=core & region
 sure_fg=cv2.erode(seed,np.ones((3,3),np.uint8))>0
 reach=cv2.dilate(region,np.ones((7,7),np.uint8))>0
 m=np.full(core.shape,cv2.GC_PR_BGD,np.uint8)
 m[region>0]=cv2.GC_PR_FGD
 m[~reach]=cv2.GC_BGD
 m[sure_fg]=cv2.GC_FGD
 # Force distant background to remain absent even where flags have foreground-like colour.
 bg_model=np.zeros((1,65),np.float64);fg_model=np.zeros((1,65),np.float64)
 cv2.grabCut(cv2.cvtColor(smooth,cv2.COLOR_RGB2BGR),m,None,bg_model,fg_model,3,cv2.GC_INIT_WITH_MASK)
 raw=((m==cv2.GC_FGD)|(m==cv2.GC_PR_FGD)).astype('uint8')
 mask=fill(largest(cv2.morphologyEx(raw,cv2.MORPH_CLOSE,np.ones((9,9),np.uint8))))
 alpha=mask*255
 out=np.dstack([rgb,alpha]).astype('uint8')
 return Image.fromarray(out)
def main():
 report={}
 for kind,name in SOURCES.items():
  im=Image.open(INPUT/name);frames=[];durations=[]
  for i in range(im.n_frames):
   im.seek(i);frames.append(cut(im,kind));durations.append(im.info['duration'])
   if i%10==0:print(kind,i,flush=True)
  frames[0].save(ROOT/(kind+'.webp'),save_all=True,append_images=frames[1:],duration=durations,loop=0,lossless=True,method=4)
  ids=list(range(0,len(frames),max(1,len(frames)//12)))
  sheet=Image.new('RGB',(1200,((len(ids)+5)//6)*220),'#303040');d=ImageDraw.Draw(sheet)
  for j,i in enumerate(ids):
   fr=frames[i].copy();fr.thumbnail((200,200));x=j%6*200;y=j//6*220
   sheet.paste(fr,(x,y),fr);d.text((x+5,y+200),str(i),fill='white')
  sheet.save(ROOT/(kind+'-cutout-contact.png'))
  report[kind]={'source_frames':len(frames),'duration_ms':sum(durations),'durations':durations}
 (ROOT/'report.json').write_text(json.dumps(report,indent=2))
if __name__=='__main__':main()
