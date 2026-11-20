from drag_animation_smoke import *
s=dict(re.findall(r'^\s*(\w+)=(\S+)',sh('dumpsys','activity','service',PKG+'/.CompanionService'),re.M));a,b,c,d=bounds()
assert 0<int(s['animationWidth'])<=c-a and 0<int(s['animationHeight'])<=d-b,s
print('PASS displayed animation dimensions do not exceed overlay pixel bounds',s['animationWidth'],s['animationHeight'])
