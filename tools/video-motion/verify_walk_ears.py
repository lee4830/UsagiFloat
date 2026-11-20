"""Check both pink ear interiors survive every packaged walking frame."""
from PIL import Image
import cv2,numpy as np,sys
atlas=Image.open(sys.argv[1]).convert('RGBA')
for i in range(12):
 f=np.array(atlas.crop((i%8*256,i//8*256,i%8*256+256,i//8*256+256)));r,g,b,a=f.astype(float).transpose(2,0,1)
 pink=((r>175)&(g>110)&(r-g>25)&(r-b>12)&(b>110)&(a>180)).astype('uint8')
 n,labels,stats,_=cv2.connectedComponentsWithStats(pink)
 ears=[j for j in range(1,n) if stats[j,3]>25 and stats[j,4]>100]
 assert len(ears)==2,(i,'expected two intact ears',[(stats[j]).tolist() for j in range(1,n)])
print('12/12 packaged walking frames have two intact pink ear interiors')
