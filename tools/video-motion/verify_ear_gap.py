"""Every walk pose must preserve two pink ears and a transparent center gap."""
from PIL import Image
import cv2,numpy as np,sys
atlas=Image.open(sys.argv[1]).convert('RGBA')
indices=[0,3,6,10,11,13,14,16,17] if '--bridges' in sys.argv else range(12)
for i in indices:
 f=np.array(atlas.crop((i%8*256,i//8*256,i%8*256+256,i//8*256+256)));r,g,b,a=f.astype(float).transpose(2,0,1)
 pink=((r>175)&(g>110)&(r-g>25)&(r-b>12)&(b>110)&(a>180)).astype('uint8')
 n,labels,stats,centers=cv2.connectedComponentsWithStats(pink)
 ears=[j for j in range(1,n) if stats[j,3]>25 and stats[j,4]>100];assert len(ears)==2,(i,'ears')
 x=round(sum(centers[j,0] for j in ears)/2)
 for height in [.25,.4,.6]:
  y=round(sum(stats[j,1]+stats[j,3]*height for j in ears)/2)
  assert a[y,x]<=8,(i,'ear gap not transparent',x,y,a[y,x])
 # Head below the ears must remain opaque; only the upper gap is cleared.
 head_y=min(240,max(stats[j,1]+stats[j,3] for j in ears)+20)
 assert a[head_y,x]>=240,(i,'head removed')
print(str(len(indices))+'/'+str(len(indices))+' ear gaps transparent; two pink ears and head preserved')
