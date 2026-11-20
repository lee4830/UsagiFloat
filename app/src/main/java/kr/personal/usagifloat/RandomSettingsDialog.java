package kr.personal.usagifloat;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Edits a local draft: only Save changes preferences or the running companion. */
final class RandomSettingsDialog {
 static String summary(SharedPreferences prefs){
  if(prefs.getBoolean("random_all",true))return "전체 "+CharacterCatalog.NAMES.length+"개 사용";
  int n=pool(prefs).length;return "선택한 "+n+"개 사용"+(n==1?" · 한 캐릭터만 재생":"");
 }
 static int[] pool(SharedPreferences prefs){return RandomPool.resolve(prefs.getBoolean("random_all",true),prefs.getString("random_selection",""),CharacterCatalog.NAMES.length);}
 static void show(Context context,SharedPreferences prefs,Runnable changed){
  float density=context.getResources().getDisplayMetrics().density;
  int pad=Math.round(16*density);
  LinearLayout root=new LinearLayout(context);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(pad,pad,pad,pad);
  RadioGroup mode=new RadioGroup(context);
  RadioButton all=new RadioButton(context);all.setId(View.generateViewId());all.setText("전체 이미지 랜덤");all.setContentDescription("전체 이미지 랜덤");
  RadioButton custom=new RadioButton(context);custom.setId(View.generateViewId());custom.setText("선택한 이미지만 랜덤");custom.setContentDescription("선택한 이미지만 랜덤");
  mode.addView(all);mode.addView(custom);root.addView(mode);
  TextView count=new TextView(context);count.setTextSize(13);count.setPadding(0,pad/2,0,pad/2);root.addView(count);
  LinearLayout list=new LinearLayout(context);list.setOrientation(LinearLayout.VERTICAL);root.addView(list);
  int[] saved=RandomPool.resolve(false,prefs.getString("random_selection",""),CharacterCatalog.NAMES.length);
  CheckBox[] boxes=new CheckBox[CharacterCatalog.NAMES.length];
  Runnable refresh=()->{int n=0;for(CheckBox b:boxes)if(b!=null&&b.isChecked())n++;boolean useAll=all.isChecked();list.setVisibility(useAll?View.GONE:View.VISIBLE);count.setText(useAll?"모든 캐릭터가 랜덤으로 나와요.":n+"개 선택됨"+(n==1?" · 이 캐릭터만 계속 움직여요.":" · 최소 1개를 선택해 주세요."));count.setTextColor(n==0&&!useAll?0xffb3261e:0xff75634c);};
  for(int i=0;i<boxes.length;i++){
   LinearLayout row=new LinearLayout(context);row.setGravity(Gravity.CENTER_VERTICAL);
   ImageView thumb=new ImageView(context);thumb.setImageResource(CharacterCatalog.thumbnail(i));thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);thumb.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
   row.addView(thumb,new LinearLayout.LayoutParams(Math.round(64*density),Math.round(64*density)));
   CheckBox box=new CheckBox(context);boxes[i]=box;box.setText(CharacterCatalog.NAMES[i]);box.setTextSize(14);box.setContentDescription("랜덤 선택 · "+CharacterCatalog.NAMES[i]);box.setChecked(RandomPool.contains(saved,i));
   row.addView(box,new LinearLayout.LayoutParams(0,Math.round(64*density),1));list.addView(row);box.setOnCheckedChangeListener((button,on)->refresh.run());thumb.setOnClickListener(v->box.setChecked(!box.isChecked()));
  }
  mode.check(prefs.getBoolean("random_all",true)?all.getId():custom.getId());mode.setOnCheckedChangeListener((group,id)->refresh.run());refresh.run();
  ScrollView scroll=new ScrollView(context);scroll.addView(root);
  AlertDialog dialog=new AlertDialog.Builder(context).setTitle("랜덤 재생 상세 설정").setView(scroll).setNegativeButton("취소",null).setPositiveButton("선택 적용",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   StringBuilder selected=new StringBuilder();for(int i=0;i<boxes.length;i++)if(boxes[i].isChecked()){if(selected.length()>0)selected.append(',');selected.append(i);}
   if(!all.isChecked()&&selected.length()==0){count.setText("최소 1개를 선택해 주세요.");count.setTextColor(0xffb3261e);Toast.makeText(context,"이미지를 1개 이상 선택해 주세요.",Toast.LENGTH_SHORT).show();return;}
   prefs.edit().putBoolean("random_all",all.isChecked()).putString("random_selection",selected.toString()).apply();changed.run();dialog.dismiss();
  }));dialog.show();
 }
}
