from probe import *
import json,math
SPECS=[('walk','compilation-opening.mp4',28.6,29.0,(480,40,330,610)),('look','compilation-opening.mp4',40.0,41.0,(280,120,370,530)),('wiggle','short.mp4',4.2,6.8,(0,270,608,540)),('hop','short.mp4',3.2,4.2,(0,270,608,540))]
for name,video,start,end,box in SPECS:
 cap=cv2.VideoCapture(str(BASE/video));fps=cap.get(cv2.CAP_PROP_FPS);first=round(start*fps);last=round(end*fps);cap.set(cv2.CAP_PROP_POS_FRAMES,first)
 frames=[];rects=[]
 for idx in range(first,last):
  ok,bgr=cap.read();assert ok,(name,idx);fr=cut(cv2.cvtColor(bgr,cv2.COLOR_BGR2RGB),box);bb=fr.getbbox();assert bb,(name,idx);frames.append(fr);rects.append(bb)
 cap.release()
 # One fixed crop and scale per clip preserve original motion and proportions.
 union=(min(r[0] for r in rects),min(r[1] for r in rects),max(r[2] for r in rects),max(r[3] for r in rects))
 maxw=union[2]-union[0];maxh=union[3]-union[1];scale=224/max(maxw,maxh)
 outputs=[]
 for fr in frames:
  cropped=fr.crop(union);cropped=cropped.resize((round(cropped.width*scale),round(cropped.height*scale)),Image.Resampling.LANCZOS)
  square=Image.new('RGBA',(256,256));square.paste(cropped,((256-cropped.width)//2,240-cropped.height));outputs.append(square)
 duration=[round((i+1)*1000/fps)-round(i*1000/fps) for i in range(len(outputs))]
 outputs[0].save(BASE/'processed'/f'{name}.webp',save_all=True,append_images=outputs[1:],duration=duration,loop=0,lossless=True,method=4)
 atlas=Image.new('RGBA',(2048,math.ceil(len(outputs)/8)*256))
 for i,fr in enumerate(outputs):atlas.paste(fr,(i%8*256,i//8*256))
 atlas.save(BASE/'processed'/f'{name}-atlas.png')
 sheet=Image.new('RGB',(1200,math.ceil(12/6)*220),'#546170');d=ImageDraw.Draw(sheet)
 for j,i in enumerate(np.linspace(0,len(outputs)-1,12,dtype=int)):
  im=outputs[i].resize((200,200));sheet.paste(im,(j%6*200,j//6*220),im);d.text((j%6*200,j//6*220+200),f'{name} {i} / {start+i/fps:.2f}',fill='white')
 sheet.save(BASE/'processed'/f'{name}-contact.jpg')
 (BASE/'processed'/f'{name}.json').write_text(json.dumps({'source':video,'start':start,'end':end,'fps':fps,'count':len(outputs),'duration_ms':sum(duration),'crop':box,'scale':scale,'boxes':rects}))
 print(name,len(outputs),'frames',maxw,maxh,flush=True)
