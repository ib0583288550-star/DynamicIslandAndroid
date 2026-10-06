package com.dynamicisland.android;

import android.app.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.view.*;import android.widget.*;

public class MainActivity extends Activity {
  LinearLayout root;
  int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
  TextView text(String s,float size){ TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(dp(18),dp(12),dp(18),dp(12));return t; }
  GradientDrawable bg(int color,float r){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(r));return g;}
  public void onCreate(Bundle b){super.onCreate(b); build();}
  void build(){
    root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(24),dp(18),dp(18));root.setBackgroundColor(Color.rgb(8,8,13));
    TextView title=text("Dynamic Island",30); title.setTypeface(null,1);root.addView(title);
    TextView sub=text("אי דינמי צבעוני וחי מעל האפליקציות",15);sub.setTextColor(Color.LTGRAY);root.addView(sub);
    TextView preview=text("      ●   Dynamic Island   ●      ",20);preview.setGravity(17);preview.setBackground(bg(Color.BLACK,50));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(64));pp.setMargins(0,dp(22),0,dp(18));root.addView(preview,pp);
    Button start=new Button(this);start.setText("🚀  הפעל את ה־Dynamic Island");start.setTextColor(Color.WHITE);start.setBackground(bg(Color.rgb(70,35,130),32));root.addView(start,new LinearLayout.LayoutParams(-1,dp(58)));
    Button test=new Button(this);test.setText("✨  בדיקת אנימציה");test.setTextColor(Color.WHITE);test.setBackground(bg(Color.rgb(25,25,38),32));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,dp(54));tp.setMargins(0,dp(12),0,0);root.addView(test,tp);
    TextView info=text("\nמה תקבל:\n• אי שחור עם אנימציות והתרחבות\n• סוללה וטעינה\n• טיימר והתראות\n• מידע מנגן מדיה\n• עיצוב זוהר וצבעוני\n• עבודה מעל אפליקציות אחרות",16);info.setTextColor(Color.rgb(220,215,235));root.addView(info);
    setContentView(root);
    start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));}else startService(new Intent(this,IslandService.class));});
    test.setOnClickListener(v->{startService(new Intent(this,IslandService.class).putExtra("demo",true));});
  }
}
