package kr.personal.usagifloat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
 static boolean showing;
 private final int ink=Color.rgb(68,54,44), muted=Color.rgb(131,113,91);
 private TextView status,sizeLabel; private Button start; private AnimatedImageDrawable preview;
 private SharedPreferences prefs; private boolean resumed;
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private GradientDrawable background(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
 private void gap(LinearLayout l,int height){Space s=new Space(this);l.addView(s,new LinearLayout.LayoutParams(1,dp(height)));}
 private Button button(String label,int color){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(16);b.setTextColor(ink);b.setTypeface(null,Typeface.BOLD);b.setBackground(background(color,18));b.setElevation(0);b.setMinHeight(dp(54));return b;}
 @Override public void onCreate(Bundle state){
  super.onCreate(state);prefs=getSharedPreferences("companion",MODE_PRIVATE);
  getWindow().setStatusBarColor(0xfffffbf2);getWindow().setNavigationBarColor(0xfffffbf2);
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xfffffbf2);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(28),dp(24),dp(28),dp(28));
  scroll.addView(root);setContentView(scroll);
  root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(28),dp(24)+insets.getSystemWindowInsetTop(),dp(28),dp(28)+insets.getSystemWindowInsetBottom());return insets;});
  TextView tag=text("작은 친구, 어디서나",12,muted);tag.setLetterSpacing(.12f);root.addView(tag);gap(root,10);
  TextView title=text("우사기 둥둥",32,ink);title.setTypeface(null,Typeface.BOLD);root.addView(title);gap(root,8);
  root.addView(text("좋아하는 화면 위에\n우사기를 살짝 띄워 보세요.",16,muted));gap(root,18);
  LinearLayout card=new LinearLayout(this);card.setGravity(Gravity.CENTER);card.setOrientation(LinearLayout.VERTICAL);card.setBackground(background(0xffffefc2,28));card.setPadding(0,dp(12),0,dp(14));
  ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setContentDescription("우사기 애니메이션 미리보기");card.addView(image,new LinearLayout.LayoutParams(dp(192.5f),dp(192.5f)));
  try{preview=Art.load(this,image);}catch(Exception e){image.setImageResource(R.drawable.usagi_face);}
  TextView caption=text("꾹 누르고, 쏙 옮겨요",13,muted);caption.setGravity(Gravity.CENTER);card.addView(caption);root.addView(card);gap(root,20);
  root.addView(text("움직임 모드",14,ink));
  RadioGroup movement=new RadioGroup(this);movement.setOrientation(RadioGroup.HORIZONTAL);
  RadioButton manual=new RadioButton(this);manual.setId(View.generateViewId());manual.setText("수동 모드");manual.setContentDescription("수동 모드");
  RadioButton wander=new RadioButton(this);wander.setId(View.generateViewId());wander.setText("산책 모드");wander.setContentDescription("산책 모드");
  movement.addView(manual,new RadioGroup.LayoutParams(0,dp(48),1));movement.addView(wander,new RadioGroup.LayoutParams(0,dp(48),1));
  movement.check(prefs.getBoolean("wander_enabled",false)?wander.getId():manual.getId());root.addView(movement);
  LinearLayout wanderSettings=new LinearLayout(this);wanderSettings.setOrientation(LinearLayout.VERTICAL);root.addView(wanderSettings);
  wanderSettings.addView(text("산책 모드에서는 영상 속 우사기가 뛰고 춤춰요.\n잡으면 멈추고, 놓으면 2초 쉬었다 다시 출발해요.",12,muted));gap(wanderSettings,8);
  wanderSettings.addView(text("산책 속도",14,ink));Spinner walkSpeed=new Spinner(this);walkSpeed.setContentDescription("산책 속도");
  ArrayAdapter<String> speeds=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,new String[]{"느긋하게","보통","활발하게"});speeds.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);walkSpeed.setAdapter(speeds);
  walkSpeed.setSelection(GestureState.clamp(prefs.getInt("wander_speed",1),0,2));wanderSettings.addView(walkSpeed,new LinearLayout.LayoutParams(-1,dp(48)));gap(wanderSettings,12);
  walkSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int index,long id){if(prefs.getInt("wander_speed",1)!=index){prefs.edit().putInt("wander_speed",index).apply();updateRunningCompanion();}}});
  LinearLayout manualSettings=new LinearLayout(this);manualSettings.setOrientation(LinearLayout.VERTICAL);root.addView(manualSettings);
  manualSettings.addView(text("수동 모드 · 캐릭터 선택",14,ink));
  Spinner characters=new Spinner(this);characters.setContentDescription("캐릭터 선택 목록");
  ArrayAdapter<String> choices=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,CharacterCatalog.NAMES);
  choices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);characters.setAdapter(choices);
  characters.setSelection(CharacterCatalog.normalize(prefs.getInt("character",0)));
  manualSettings.addView(characters,new LinearLayout.LayoutParams(-1,dp(50)));gap(manualSettings,14);
  characters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
   public void onNothingSelected(AdapterView<?> parent){}
   public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
    int old=CharacterCatalog.normalize(prefs.getInt("character",0));
    if(old==position)return;
    try {
     AnimatedImageDrawable next=Art.load(MainActivity.this,image,position);
     if(preview!=null)preview.stop();preview=next;
     prefs.edit().putInt("character",position).apply();
     if(resumed)preview.start();
     if(CompanionService.running)startService(new Intent(MainActivity.this,CompanionService.class).setAction(CompanionService.UPDATE));
    }catch(Exception e){Toast.makeText(MainActivity.this,"캐릭터를 불러오지 못했어요.",Toast.LENGTH_SHORT).show();characters.setSelection(old);}
   }
  });
  Switch randomMode=new Switch(this);randomMode.setText("랜덤으로 바꾸기");randomMode.setTextSize(16);randomMode.setTextColor(ink);
  randomMode.setChecked(prefs.getBoolean("random_enabled",false));randomMode.setContentDescription("랜덤 전환 모드");
  manualSettings.addView(randomMode,new LinearLayout.LayoutParams(-1,dp(48)));
  LinearLayout intervalSettings=new LinearLayout(this);intervalSettings.setOrientation(LinearLayout.VERTICAL);manualSettings.addView(intervalSettings);
  LinearLayout intervalRow=new LinearLayout(this);intervalRow.setGravity(Gravity.CENTER_VERTICAL);
  EditText seconds=new EditText(this);seconds.setSingleLine(true);seconds.setTextSize(17);seconds.setTextColor(ink);
  seconds.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);seconds.setSelectAllOnFocus(true);
  seconds.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4)});
  seconds.setContentDescription("랜덤 전환 간격 초");seconds.setText(String.valueOf(RandomRotation.validSeconds(prefs.getInt("random_seconds",30))));
  intervalRow.addView(seconds,new LinearLayout.LayoutParams(0,dp(52),1));
  TextView unit=text("초마다  ",14,ink);intervalRow.addView(unit);
  Button apply=button("간격 적용",0xffffd86c);intervalRow.addView(apply,new LinearLayout.LayoutParams(dp(112),dp(48)));
  intervalSettings.addView(intervalRow);gap(intervalSettings,6);
  intervalSettings.addView(text("1~3,600초 · 2개 이상 선택하면 연속 반복을 피합니다.\n화면을 끄면 전환도 잠시 쉬어요.",12,muted));gap(manualSettings,16);
  Button detail=button("랜덤 재생 상세 설정",0xfff0eadd);detail.setContentDescription("랜덤 재생 상세 설정");intervalSettings.addView(detail,new LinearLayout.LayoutParams(-1,dp(48)));
  TextView poolSummary=text(RandomSettingsDialog.summary(prefs),13,muted);intervalSettings.addView(poolSummary);gap(intervalSettings,8);
  detail.setOnClickListener(v->RandomSettingsDialog.show(this,prefs,()->{poolSummary.setText(RandomSettingsDialog.summary(prefs));updateRunningCompanion();}));
  TextView intervalStatus=text("적용 중: "+RandomRotation.validSeconds(prefs.getInt("random_seconds",30))+"초마다 변경",13,muted);intervalSettings.addView(intervalStatus);
  seconds.addTextChangedListener(new android.text.TextWatcher(){
   public void beforeTextChanged(CharSequence s,int start,int count,int after){}
   public void onTextChanged(CharSequence s,int start,int before,int count){
    String saved=String.valueOf(RandomRotation.validSeconds(prefs.getInt("random_seconds",30)));
    intervalStatus.setText(s.toString().equals(saved)?"적용 중: "+saved+"초마다 변경":"변경한 시간은 ‘간격 적용’을 눌러 저장해 주세요.");
   }
   public void afterTextChanged(android.text.Editable e){}
  });
  intervalSettings.setVisibility(randomMode.isChecked()?View.VISIBLE:View.GONE);
  randomMode.setOnCheckedChangeListener((view,checked)->{
   prefs.edit().putBoolean("random_enabled",checked).apply();intervalSettings.setVisibility(checked?View.VISIBLE:View.GONE);
   seconds.setError(null);seconds.setText(String.valueOf(RandomRotation.validSeconds(prefs.getInt("random_seconds",30))));
   if(!checked){seconds.clearFocus();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(seconds.getWindowToken(),0);}
   updateRunningCompanion();
  });
  apply.setOnClickListener(view->{
   int value;
   try{value=Integer.parseInt(seconds.getText().toString().trim());}catch(NumberFormatException e){value=0;}
   if(value<1||value>3600){seconds.setError("1~3,600초를 입력해 주세요.");return;}
   prefs.edit().putInt("random_seconds",value).apply();intervalStatus.setText("적용 중: "+value+"초마다 변경");seconds.setError(null);seconds.clearFocus();
   ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(seconds.getWindowToken(),0);
   updateRunningCompanion();Toast.makeText(this,value+"초마다 바뀌도록 적용했어요.",Toast.LENGTH_SHORT).show();
  });
  Runnable modeControls=()->{
   boolean strolling=prefs.getBoolean("wander_enabled",false);
   int previewSide=dp(strolling?154:192.5f);image.setLayoutParams(new LinearLayout.LayoutParams(previewSide,previewSide));
   wanderSettings.setVisibility(strolling?View.VISIBLE:View.GONE);manualSettings.setVisibility(strolling?View.GONE:View.VISIBLE);
   intervalSettings.setVisibility(!strolling&&randomMode.isChecked()?View.VISIBLE:View.GONE);
   if(strolling){seconds.clearFocus();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(seconds.getWindowToken(),0);}
   if(strolling){if(preview!=null)preview.stop();image.setImageDrawable(new WanderDrawable(getResources()));}
   else {image.setImageDrawable(preview);if(resumed&&preview!=null)preview.start();}
   caption.setText(strolling?"원본 영상의 동작을 재생해요":"꾹 누르고, 쏙 옮겨요");
  };
  modeControls.run();movement.setOnCheckedChangeListener((group,id)->{prefs.edit().putBoolean("wander_enabled",id==wander.getId()).apply();modeControls.run();updateRunningCompanion();});
  status=text("",14,muted);
  Button permission=button("실행 · 표시 권한 확인",0xfff0eadd);
  permission.setOnClickListener(v->{if(Settings.canDrawOverlays(this)){Toast.makeText(this,"준비됐어요. 시작을 눌러 주세요.",Toast.LENGTH_SHORT).show();}else{try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}catch(Exception e){Toast.makeText(this,"설정에서 ‘다른 앱 위에 표시’를 허용해 주세요.",Toast.LENGTH_LONG).show();}}});
  gap(root,18);sizeLabel=text("",14,ink);root.addView(sizeLabel);
  SeekBar size=new SeekBar(this);size.setMax(120);size.setProgress(prefs.getInt("size",120)-80);size.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffb28c43));size.setThumbTintList(android.content.res.ColorStateList.valueOf(0xffb28c43));size.setContentDescription("캐릭터 크기");root.addView(size,new LinearLayout.LayoutParams(-1,dp(44)));updateSize(size.getProgress()+80);
  size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean user){updateSize(p+80);if(user)prefs.edit().putInt("size",p+80).apply();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){if(CompanionService.running)startService(new Intent(MainActivity.this,CompanionService.class).setAction(CompanionService.UPDATE));}});
  gap(root,12);Switch saving=new Switch(this);saving.setText("배터리 절약");saving.setTextSize(16);saving.setTextColor(ink);saving.setContentDescription("배터리 절약 모드");saving.setChecked(prefs.getBoolean("power_saving",false));root.addView(saving,new LinearLayout.LayoutParams(-1,dp(48)));
  root.addView(text("켜면 30초 움직이고 30초 정지해요.\n캐릭터는 계속 보이며, 만지면 바로 다시 움직여요.",12,muted));saving.setOnCheckedChangeListener((v,on)->{prefs.edit().putBoolean("power_saving",on).apply();updateRunningCompanion();});
  root.addView(new TimerSettingsView(this,prefs,()->{if(CompanionService.running)startService(new Intent(this,CompanionService.class).setAction(CompanionService.TIMER));},this::updateRunningCompanion));
  gap(root,10);start=button("시작 · 우사기 띄우기",0xffffd86c);root.addView(start,new LinearLayout.LayoutParams(-1,dp(58)));
  start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"아래 ‘표시 권한 확인’ 버튼으로 먼저 권한을 허용해 주세요.",Toast.LENGTH_LONG).show();return;}if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);else launch();});
  gap(root,14);Button stop=button("재생 종료",0xfff0eadd);root.addView(stop,new LinearLayout.LayoutParams(-1,dp(48)));stop.setOnClickListener(v->{stopService(new Intent(this,CompanionService.class));Toast.makeText(this,"우사기를 종료했어요.",Toast.LENGTH_SHORT).show();});
  gap(root,20);root.addView(status);gap(root,8);root.addView(permission,new LinearLayout.LayoutParams(-1,dp(54)));
  gap(root,20);root.addView(text("이동  손가락으로 끌어 원하는 곳에 놓기\n종료  꾹 누른 뒤 아래 집에 놓기",13,muted));
 }
 private void updateRunningCompanion(){
  if(CompanionService.running)startService(new Intent(this,CompanionService.class).setAction(CompanionService.UPDATE));
 }
 private void updateSize(int size){sizeLabel.setText("몸통 기준 크기  ·  "+size);}
 private void launch(){try{startForegroundService(new Intent(this,CompanionService.class).setAction(CompanionService.START));finishAndRemoveTask();}catch(RuntimeException e){Toast.makeText(this,"시작하지 못했어요. 표시 권한을 확인해 주세요.",Toast.LENGTH_LONG).show();}}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] grants){super.onRequestPermissionsResult(r,p,grants);if(r==7)launch();}
 @Override protected void onResume(){super.onResume();showing=true;resumed=true;boolean granted=Settings.canDrawOverlays(this);status.setText(granted?"●  표시 권한 허용됨 · 시작할 수 있어요":"○  먼저 다른 앱 위 표시를 허용해 주세요");start.setAlpha(granted?1f:.55f);if(preview!=null&&!prefs.getBoolean("wander_enabled",false))preview.start();}
 @Override protected void onPause(){showing=false;resumed=false;if(preview!=null)preview.stop();super.onPause();}
}
