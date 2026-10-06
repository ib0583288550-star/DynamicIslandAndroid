package com.dynamicisland.android;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;

public class MainActivity extends Activity {
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float z){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(z);t.setPadding(dp(16),dp(12),dp(16),dp(12));return t;}
 GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}

 public void onCreate(Bundle b){
  super.onCreate(b);
  ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
  LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(18),dp(22),dp(18),dp(24));r.setBackgroundColor(Color.rgb(7,7,12));
  TextView title=tv("אי דינמי ✦",30);title.setTypeface(null,1);r.addView(title);
  TextView sub=tv("רק מה שחשוב: התראה אחרונה, או מוזיקה כשיש מוזיקה.",15);sub.setTextColor(Color.LTGRAY);r.addView(sub);
  TextView p=tv("          ●  אי דינמי          ",19);p.setGravity(17);p.setBackground(bg(Color.BLACK,60));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(68));pp.setMargins(0,dp(20),0,dp(16));r.addView(p,pp);

  Button start=new Button(this);start.setText("🚀  הפעל אי דינמי");start.setTextColor(Color.WHITE);start.setBackground(bg(Color.rgb(104,55,190),34));r.addView(start,new LinearLayout.LayoutParams(-1,dp(58)));
  Button notifAccess=new Button(this);notifAccess.setText("🔔  הפעל גישה להתראות");notifAccess.setTextColor(Color.WHITE);notifAccess.setBackground(bg(Color.rgb(30,24,48),34));LinearLayout.LayoutParams nap=new LinearLayout.LayoutParams(-1,dp(54));nap.setMargins(0,dp(10),0,0);r.addView(notifAccess,nap);
  Button demo=new Button(this);demo.setText("✨  הדגמת אנימציה צבעונית");demo.setTextColor(Color.WHITE);demo.setBackground(bg(Color.rgb(30,24,48),34));LinearLayout.LayoutParams d=new LinearLayout.LayoutParams(-1,dp(54));d.setMargins(0,dp(12),0,0);r.addView(demo,d);

  Button size=new Button(this);size.setText("📐  גודל וגובה האי");size.setTextColor(Color.WHITE);size.setBackground(bg(Color.rgb(30,24,48),34));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54));sp.setMargins(0,dp(12),0,0);r.addView(size,sp);
  Button timeout=new Button(this);timeout.setText("⏳  זמן הצגת התראות");timeout.setTextColor(Color.WHITE);timeout.setBackground(bg(Color.rgb(30,24,48),34));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,dp(54));tp.setMargins(0,dp(12),0,0);r.addView(timeout,tp);

  Switch onlyIsland=new Switch(this);onlyIsland.setText("🔔  להציג התראות רק באי הדינמי");onlyIsland.setTextColor(Color.WHITE);onlyIsland.setTextSize(15);onlyIsland.setPadding(dp(8),dp(10),dp(8),dp(10));
  SharedPreferences pref=getSharedPreferences("island_settings",MODE_PRIVATE);
  onlyIsland.setChecked(pref.getBoolean("only_island_notifications",false));r.addView(onlyIsland,new LinearLayout.LayoutParams(-1,dp(58)));
  onlyIsland.setOnCheckedChangeListener((v,checked)->pref.edit().putBoolean("only_island_notifications",checked).apply());

  TextView info=tv("
✨ כולל
• שליטה בגודל וברוחב
• שליטה בגובה האי
• זמן תצוגת התראה לבחירה
• אפשרות לקרוא התראות דרך האי בלבד",16);info.setTextColor(Color.rgb(220,215,235));r.addView(info);
  setContentView(r);

  start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));return;} startService(new Intent(this,IslandService.class));});
  notifAccess.setOnClickListener(v->startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
  demo.setOnClickListener(v->{if(!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));else startService(new Intent(this,IslandService.class).putExtra("demo",true));});
  size.setOnClickListener(v->showSizeDialog(pref));
  timeout.setOnClickListener(v->showTimeoutDialog(pref));
 }

 void showSizeDialog(SharedPreferences pref){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(8),0,dp(8),0);
  TextView w=tv("רוחב האי",16);box.addView(w);
  SeekBar width=new SeekBar(this);width.setMax(480);width.setProgress(Math.max(0,Math.min(480,pref.getInt("width_dp",218)-120)));box.addView(width);
  TextView h=tv("גובה האי",16);box.addView(h);
  SeekBar height=new SeekBar(this);height.setMax(116);height.setProgress(Math.max(0,Math.min(116,pref.getInt("height_dp",42)-24)));box.addView(height);
  TextView y=tv("מרחק מלמעלה",16);box.addView(y);
  SeekBar top=new SeekBar(this);top.setMax(120);top.setProgress(Math.max(0,Math.min(120,pref.getInt("top_dp",6))));box.addView(top);
  TextView values=tv("",14);box.addView(values);
  Runnable update=()->{int ww=120+width.getProgress(),hh=24+height.getProgress(),yy=top.getProgress();values.setText("רוחב: "+ww+"dp   •   גובה: "+hh+"dp   •   מרחק מלמעלה: "+yy+"dp"); pref.edit().putInt("width_dp",ww).putInt("height_dp",hh).putInt("top_dp",yy).apply(); IslandService.applySettings();};
  width.setOnSeekBarChangeListener(simple(update));height.setOnSeekBarChangeListener(simple(update));top.setOnSeekBarChangeListener(simple(update));update.run();
  new AlertDialog.Builder(this).setTitle("התאמת האי").setView(box).setPositiveButton("סיום",null).setNegativeButton("ביטול",null).show();
 }
 SeekBar.OnSeekBarChangeListener simple(Runnable r){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){r.run();}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}};}

 void showTimeoutDialog(SharedPreferences pref){
  String[] items={"1 שנייה","2 שניות","3 שניות","5 שניות","10 שניות","עד שמגיע אירוע חדש"};
  int[] vals={1000,2000,3000,5000,10000,0};
  int cur=pref.getInt("notification_timeout_ms",3200), checked=3;
  for(int i=0;i<vals.length;i++)if(vals[i]==cur)checked=i;
  new AlertDialog.Builder(this).setTitle("כמה זמן ההתראה תישאר באי?").setSingleChoiceItems(items,checked,(d,which)->{
   pref.edit().putInt("notification_timeout_ms",vals[which]).apply();d.dismiss();
  }).setNegativeButton("ביטול",null).show();
 }
}
