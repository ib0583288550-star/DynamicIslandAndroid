package com.dynamicisland.android;

import android.app.*;import android.content.*;import android.graphics.Color;import android.graphics.drawable.GradientDrawable;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.view.*;import android.widget.*;

public class MainActivity extends Activity {
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float z){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(z);t.setPadding(dp(16),dp(12),dp(16),dp(12));return t;}
 GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(18),dp(26),dp(18),dp(18));r.setBackgroundColor(Color.rgb(7,7,12));
 TextView title=tv("Dynamic Island ✦",30);title.setTypeface(null,1);r.addView(title);
 TextView sub=tv("אי דינמי צבעוני וחי — התראות, מדיה, סוללה וטיימרים",15);sub.setTextColor(Color.LTGRAY);r.addView(sub);
 TextView p=tv("       ●  ✦  Dynamic Island  ✦  ●       ",19);p.setGravity(17);p.setBackground(bg(Color.BLACK,60));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(68));pp.setMargins(0,dp(20),0,dp(16));r.addView(p,pp);
 Button start=new Button(this);start.setText("🚀  הפעל Dynamic Island");start.setTextColor(Color.WHITE);start.setBackground(bg(Color.rgb(104,55,190),34));r.addView(start,new LinearLayout.LayoutParams(-1,dp(58)));
 Button demo=new Button(this);demo.setText("✨  הדגמת אנימציה צבעונית");demo.setTextColor(Color.WHITE);demo.setBackground(bg(Color.rgb(30,24,48),34));LinearLayout.LayoutParams d=new LinearLayout.LayoutParams(-1,dp(54));d.setMargins(0,dp(12),0,0);r.addView(demo,d);
 TextView info=tv("\n✨ כולל\n• אי צף עם אנימציות והתרחבות\n• סוללה וטעינה\n• טיימרים והתראות\n• מדיה\n• Wi‑Fi / Bluetooth\n• צבעים וזוהר לפי מצב",16);info.setTextColor(Color.rgb(220,215,235));r.addView(info);
 setContentView(r);
 start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));else startService(new Intent(this,IslandService.class));});
 demo.setOnClickListener(v->{if(!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));else startService(new Intent(this,IslandService.class).putExtra("demo",true));});
 }
}