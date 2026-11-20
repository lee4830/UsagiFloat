package kr.personal.usagifloat;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
/** Common timer settings; edits are separate from explicitly applied policy. */
final class TimerSettingsView extends LinearLayout {
 private final Activity host;private final SharedPreferences prefs;private final Runnable changed;
 private final EditText amount;private final Spinner unit;private final TextView status;private final LinearLayout timed;
 private static final String[] UNITS={"분","시간","일"};private boolean ready;
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private TextView label(String text,int size){TextView v=new TextView(host);v.setText(text);v.setTextSize(size);v.setTextColor(Color.rgb(68,54,44));return v;}
 TimerSettingsView(Activity host,SharedPreferences prefs,Runnable changed,Runnable displayChanged){
  super(host);this.host=host;this.prefs=prefs;this.changed=changed;setOrientation(VERTICAL);setPadding(0,dp(16),0,dp(12));
  addView(label("자동 종료",16));RadioGroup modes=new RadioGroup(host);modes.setOrientation(HORIZONTAL);
  RadioButton unlimited=new RadioButton(host);unlimited.setId(View.generateViewId());unlimited.setText("계속 실행");
  RadioButton automatic=new RadioButton(host);automatic.setId(View.generateViewId());automatic.setText("시간 지정");
  modes.addView(unlimited,new RadioGroup.LayoutParams(0,dp(48),1));modes.addView(automatic,new RadioGroup.LayoutParams(0,dp(48),1));modes.check(prefs.getBoolean("timer_enabled",false)?automatic.getId():unlimited.getId());addView(modes);
  timed=new LinearLayout(host);timed.setOrientation(VERTICAL);addView(timed);LinearLayout row=new LinearLayout(host);row.setGravity(Gravity.CENTER_VERTICAL);
  amount=new EditText(host);amount.setSingleLine(true);amount.setSelectAllOnFocus(true);amount.setTextSize(17);amount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);amount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});amount.setContentDescription("자동 종료 시간 값");amount.setText(String.valueOf(prefs.getInt("timer_amount",30)));row.addView(amount,new LayoutParams(0,dp(52),1));
  unit=new Spinner(host);unit.setContentDescription("자동 종료 시간 단위");ArrayAdapter<String> adapter=new ArrayAdapter<>(host,android.R.layout.simple_spinner_item,UNITS);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);unit.setAdapter(adapter);unit.setSelection(savedUnit());row.addView(unit,new LayoutParams(dp(84),dp(52)));
  Button apply=new Button(host);apply.setText("시간 적용");apply.setTextSize(15);apply.setTextColor(Color.rgb(68,54,44));GradientDrawable bg=new GradientDrawable();bg.setColor(0xffffd86c);bg.setCornerRadius(dp(22));apply.setBackground(bg);row.addView(apply,new LayoutParams(dp(112),dp(48)));timed.addView(row);
  Switch caption=new Switch(host);caption.setText("우사기 아래 남은 시간 표시");caption.setTextSize(14);caption.setTextColor(Color.rgb(68,54,44));caption.setContentDescription("우사기 타이머 표시");caption.setChecked(prefs.getBoolean("timer_caption",true));timed.addView(caption,new LayoutParams(-1,dp(48)));caption.setOnCheckedChangeListener((view,on)->{prefs.edit().putBoolean("timer_caption",on).apply();displayChanged.run();});
  timed.addView(label("시작 또는 시간 적용 시점부터 셉니다.\n실행 중 적용하면 남은 시간을 새로 설정해요.\n화면 꺼진 시간도 포함해요. 꺼진 동안 만료되면 화면을 켤 때 종료해요.",12));
  status=label("",13);status.setPadding(0,dp(8),0,0);addView(status);showMode();
  modes.setOnCheckedChangeListener((g,id)->{prefs.edit().putBoolean("timer_enabled",id==automatic.getId()).apply();amount.setError(null);amount.setText(String.valueOf(prefs.getInt("timer_amount",30)));unit.setSelection(savedUnit());showMode();hideKeyboard();changed.run();});
  amount.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int n){}public void onTextChanged(CharSequence s,int a,int b,int c){showStatus();}public void afterTextChanged(Editable e){}});
  unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int i,long id){if(ready)showStatus();}});
  apply.setOnClickListener(v->{long value;try{value=Long.parseLong(amount.getText().toString());}catch(NumberFormatException e){value=0;}if(AutoStop.duration(value,unit.getSelectedItemPosition())==0){amount.setError("1 이상, 최대 365일 이내로 입력해 주세요.");return;}prefs.edit().putInt("timer_amount",(int)value).putInt("timer_unit",unit.getSelectedItemPosition()).apply();amount.setError(null);hideKeyboard();showStatus();changed.run();Toast.makeText(host,value+UNITS[unit.getSelectedItemPosition()]+" 뒤 종료하도록 적용했어요.",Toast.LENGTH_SHORT).show();});
  ready=true;
 }
 private int savedUnit(){return GestureState.clamp(prefs.getInt("timer_unit",0),0,2);}
 private void hideKeyboard(){amount.clearFocus();((InputMethodManager)host.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(),0);}
 private void showMode(){timed.setVisibility(prefs.getBoolean("timer_enabled",false)?VISIBLE:GONE);showStatus();}
 private void showStatus(){if(status==null)return;if(!prefs.getBoolean("timer_enabled",false)){status.setText("직접 종료할 때까지 계속 표시해요.");return;}String saved=prefs.getInt("timer_amount",30)+UNITS[savedUnit()];boolean pending=!amount.getText().toString().equals(String.valueOf(prefs.getInt("timer_amount",30)))||unit.getSelectedItemPosition()!=savedUnit();status.setText(pending?"변경한 시간은 ‘시간 적용’을 눌러 저장해 주세요.\n현재 설정: "+saved+" 뒤 종료":"설정: "+saved+" 뒤 종료");}
}
