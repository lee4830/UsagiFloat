from pathlib import Path
from PIL import Image,ImageDraw
import json
root=Path(__file__).resolve().parents[2];report=json.loads((Path(__file__).parent/'report.json').read_text());sheet=Image.new('RGB',(960,640),'#505466');draw=ImageDraw.Draw(sheet)
for k,item in enumerate(report):
 im=Image.open(root/'app/src/main/res/raw'/ (item['key']+'.webp'));duration=0;boxes=[];assert im.n_frames==item['frames']
 for i in range(im.n_frames):
  im.seek(i);im.load();duration+=im.info['duration'];assert im.info['duration']==item['durations'][i];a=im.convert('RGBA').getchannel('A');assert a.getextrema()[0]==0
  box=a.point(lambda x:255 if x>24 else 0).getbbox();assert box;assert min(box)>=20 and max(box)<=492;boxes.append(box)
 assert duration==item['duration_ms'],(item['key'],duration,item['duration_ms'])
 w=max(b[2] for b in boxes)-min(b[0] for b in boxes);h=max(b[3] for b in boxes)-min(b[1] for b in boxes);assert 455<=max(w,h)<=465
 im.seek(0);f=im.convert('RGBA').resize((200,200));sheet.paste(f,(k%4*240,k//4*210),f);draw.text((k%4*240,k//4*210+195),item['key'],fill='white')
 print(item['key'],'PASS',im.n_frames,'encoded frames',duration,'ms',w,h)
sheet.save(Path(__file__).parent/'gallery-preview.jpg')
print('12 ASSETS VALIDATED')
