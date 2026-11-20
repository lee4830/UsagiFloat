"""Frame-preserving cutouts and fixed per-sequence normalization. No generative repainting."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
import numpy as np, cv2, json, os
ROOT=Path(os.environ.get('USAGI_GALLERY_OUTPUT',str(Path(__file__).parent/'generated')))
INPUT=Path(os.environ.get('USAGI_SOURCE_DIR',str(Path.home()/'Downloads')))
OLD=Path(__file__).resolve().parent/'originals'
ITEMS=[('usagi','usagi.gif',True),('usagi_soft','usagi_soft.gif',True),('usagi_cheer','usagi_cheer.gif',True),('usagi_closeup','usagi_closeup.webp',True),('usagi_hood','usagi_hood.webp',True),('usagi_huh','537-蛤.gif',False),('usagi_puri','537-噗哩.gif',False),('usagi_forest','usagi-chiikawa-2.gif',False),('usagi_tiny','usagi-chiikawa-3.gif',False),('usagi_room','usagi-chiikawa.gif',False),('usagi_ears','usagi-ears-usagi.gif',False),('usagi_dance','usagi.gif',False)]
def largest(m):
 n,l,s,_=cv2.connectedComponentsWithStats(m.astype('uint8'),8)
 return (l==1+np.argmax(s[1:,cv2.CC_STAT_AREA])).astype('uint8') if n>1 else m.astype('uint8')
def fill(m):
 c,_=cv2.findContours(m.astype('uint8'),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE);o=np.zeros(m.shape,np.uint8);cv2.drawContours(o,c,-1,1,cv2.FILLED);return o

def cut(im,key):
 a=np.array(im.convert('RGBA'));rgb=a[:,:,:3];r,g,b=rgb.astype(float).transpose(2,0,1)
 cream=(r>185)&(g>150)&(r-b>35)&(g-b>24)&(r-g>9)
 pink=(r>190)&(r-g>30)&(r-b>25)&(g>110)
 core=(cream|pink if key=='usagi_room' else cream).astype('uint8')
 core=fill(largest(cv2.morphologyEx(core,cv2.MORPH_CLOSE,np.ones((3,3),np.uint8))))
 # Recover the character's thick brown outline without admitting scene colors.
 radius=max(3,round(max(im.size)*(.035 if key=='usagi_room' else .023)));near=cv2.dilate(core,cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(radius*2+1,radius*2+1)))
 ink=(r<185)&(g<160)&(b<155)
 mask=fill(largest(core|((near>0)&ink).astype('uint8')))
 # Slight alpha smoothing only; do not soften or redraw the source RGB.
 alpha=Image.fromarray(mask*255).filter(ImageFilter.GaussianBlur(.45))
 return Image.merge('RGBA',(*im.convert('RGB').split(),alpha))

def main():
 ROOT.mkdir(parents=True,exist_ok=True);report=[]
 for key,name,old in ITEMS:
  if os.environ.get('USAGI_ONLY') and key not in os.environ['USAGI_ONLY'].split(','):continue
  src=(OLD if old else INPUT)/name;im=Image.open(src);frames=[];dur=[]
  for i in range(im.n_frames):
   im.seek(i);f=im.convert('RGBA');dur.append(im.info.get('duration',100))
   if not old and key not in ['usagi_huh','usagi_puri']:f=cut(f,key)
   frames.append(f)
  boxes=[f.getchannel('A').point(lambda p:255 if p>24 else 0).getbbox() for f in frames]
  assert all(boxes),(key,'empty frame')
  box=(min(b[0] for b in boxes),min(b[1] for b in boxes),max(b[2] for b in boxes),max(b[3] for b in boxes))
  w,h=box[2]-box[0],box[3]-box[1];scale=460/max(w,h);size=(round(w*scale),round(h*scale));out=[]
  for f in frames:
   f=f.crop(box).resize(size,Image.Resampling.LANCZOS)
   canvas=Image.new('RGBA',(512,512));canvas.paste(f,((512-size[0])//2,(512-size[1])//2));out.append(canvas)
  out[0].save(ROOT/(key+'.webp'),save_all=True,append_images=out[1:],duration=dur,loop=0,lossless=True,method=4)
  out[0].resize((144,144),Image.Resampling.LANCZOS).save(ROOT/(key+'_thumb.png'))
  ids=list(range(0,len(out),max(1,len(out)//12)));sheet=Image.new('RGB',(960,((len(ids)+5)//6)*180),'#505466');d=ImageDraw.Draw(sheet)
  for j,i in enumerate(ids):
   f=out[i].resize((156,156));sheet.paste(f,(j%6*160,j//6*180),f);d.text((j%6*160,j//6*180+156),str(i),fill='white')
  sheet.save(ROOT/(key+'-contact.jpg'))
  report.append(dict(key=key,source=name,frames=len(frames),duration_ms=sum(dur),durations=dur,crop=box,scale=scale,canvas=[512,512]))
  print(key,len(frames),box,'scale',round(scale,2),flush=True)
 previous=json.loads((ROOT/'report.json').read_text()) if (ROOT/'report.json').exists() else []
 keyed={v['key']:v for v in previous};keyed.update({v['key']:v for v in report})
 (ROOT/'report.json').write_text(json.dumps(list(keyed.values()),ensure_ascii=False,indent=2))
if __name__=='__main__':main()
