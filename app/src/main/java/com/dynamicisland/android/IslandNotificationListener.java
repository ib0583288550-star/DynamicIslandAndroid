package com.dynamicisland.android;

import android.app.Notification;
import android.content.ComponentName;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import java.util.List;

public class IslandNotificationListener extends NotificationListenerService {
 MediaSessionManager mediaManager;
 MediaSessionManager.OnActiveSessionsChangedListener mediaListener;
 static MediaController pendingController;
 static String pendingTitle="", pendingDetail="";

 MediaController chooseMedia(List<MediaController> sessions){
  if(sessions==null||sessions.isEmpty())return null;
  for(MediaController c:sessions){
   try{
    PlaybackState p=c.getPlaybackState();
    if(p!=null&&p.getState()==PlaybackState.STATE_PLAYING)return c;
   }catch(Exception ignored){}
  }
  return sessions.get(0);
 }

 @Override public void onListenerConnected(){
  super.onListenerConnected();
  try{
   mediaManager=(MediaSessionManager)getSystemService(MEDIA_SESSION_SERVICE);
   ComponentName cn=new ComponentName(this,IslandNotificationListener.class);
   mediaListener=sessions->{
    MediaController chosen=chooseMedia(sessions);
    if(chosen!=null){
     pendingController=chosen;
     IslandService.setMediaController(chosen);
    }
   };
   mediaManager.addOnActiveSessionsChangedListener(mediaListener,cn);
   MediaController chosen=chooseMedia(mediaManager.getActiveSessions(cn));
   if(chosen!=null){
    pendingController=chosen;
    IslandService.setMediaController(chosen);
   }
   if(!pendingTitle.isEmpty()) IslandService.event(pendingTitle,pendingDetail);
   postLatestNotification();
  }catch(Exception ignored){}
 }

 boolean isMediaNotification(Notification n){
  if(n==null)return false;
  if(Notification.CATEGORY_TRANSPORT.equals(n.category))return true;
  return n.extras!=null && n.extras.get(Notification.EXTRA_MEDIA_SESSION)!=null;
 }

 void postLatestNotification(){
  try{
   StatusBarNotification[] all=getActiveNotifications();
   if(all==null)return;
   for(int i=all.length-1;i>=0;i--){
    StatusBarNotification sbn=all[i];
    if(getPackageName().equals(sbn.getPackageName()))continue;
    Notification n=sbn.getNotification();
    if(n==null||n.extras==null||isMediaNotification(n))continue;
    Bundle e=n.extras;
    CharSequence title=e.getCharSequence(Notification.EXTRA_TITLE);
    CharSequence text=e.getCharSequence(Notification.EXTRA_TEXT);
    if(text==null)text=e.getCharSequence(Notification.EXTRA_BIG_TEXT);
    if(title==null&&text==null)continue;
    pendingTitle=title==null?"התראה":title.toString();
    pendingDetail=text==null?"":text.toString();
    IslandService.event(pendingTitle,pendingDetail);
    break;
   }
  }catch(Exception ignored){}
 }

 @Override public void onListenerDisconnected(){
  try{if(mediaManager!=null&&mediaListener!=null)mediaManager.removeOnActiveSessionsChangedListener(mediaListener);}catch(Exception ignored){}
  pendingController=null;
  IslandService.setMediaController(null);
  super.onListenerDisconnected();
 }

 @Override public void onNotificationPosted(StatusBarNotification sbn) {
  if(getPackageName().equals(sbn.getPackageName()))return;
  Notification n=sbn.getNotification();
  if(n==null||isMediaNotification(n))return;
  Bundle e=n.extras;
  if(e==null)return;
  CharSequence title=e.getCharSequence(Notification.EXTRA_TITLE);
  CharSequence text=e.getCharSequence(Notification.EXTRA_TEXT);
  if(text==null)text=e.getCharSequence(Notification.EXTRA_BIG_TEXT);
  if(title==null&&text==null)return;
  pendingTitle=title==null?"התראה":title.toString();
  pendingDetail=text==null?"":text.toString();
  IslandService.event(pendingTitle,pendingDetail);
  // Do not cancel the system notification. Cancelling notifications here can interfere with media/player notifications.
 }

 @Override public void onNotificationRemoved(StatusBarNotification sbn){}
}
