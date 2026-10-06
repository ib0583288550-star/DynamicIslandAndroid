package com.dynamicisland.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context, Intent intent) {
  if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) &&
      !Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) return;
  if (!context.getSharedPreferences("island_settings", Context.MODE_PRIVATE).getBoolean("auto_start", false)) return;
  Intent service = new Intent(context, IslandService.class);
  try {
   if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
   else context.startService(service);
  } catch (Exception ignored) {}
 }
}
