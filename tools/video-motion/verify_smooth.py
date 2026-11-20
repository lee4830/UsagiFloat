"""Validate every baked frame, alpha margin and packed atlas index."""
from pathlib import Path
from PIL import Image
import numpy as np
import sys
root=Path(sys.argv[1])
for name,n in [('walk',12),('look',30),('wiggle',78),('bow',30),('bridges',18)]:
 im=Image.open(root/f'motion_{name}.png');assert im.mode=='RGBA' and im.width==2048
 boxes=[]
 for i in range(n):
  a=np.array(im.crop((i%8*256,i//8*256,i%8*256+256,i//8*256+256)))[:,:,3]
  y,x=np.where(a>32);assert len(x)>3000,(name,i)
  box=(int(x.min()),int(y.min()),int(x.max()),int(y.max()));assert min(box[:2])>=4 and max(box[2:])<=251,(name,i,box);boxes.append(box)
 print(name,n,'frames: nonempty transparent margins and atlas indices OK')
print('168 SMOOTH FRAMES VALIDATED')
