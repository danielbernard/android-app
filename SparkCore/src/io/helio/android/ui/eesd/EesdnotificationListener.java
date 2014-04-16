package io.helio.android.ui.eesd;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class EesdnotificationListener extends NotificationListenerService{
	private String TAG = "NOTIFY";
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("NOTIFY", "NotificationListener Created");
		IntentFilter filter = new IntentFilter();
		filter.addAction("io.helio.android.ui.eesd.NOTIFICATION_LISTENER");
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG,"== onNotificationPosted ==");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
		Intent i = new Intent("io.helio.android.ui.eesd.NOTIFICATION_LISTENER");
		i.putExtra("notification_package_name", sbn.getPackageName());
		i.putExtra("notification_id", sbn.getId());
		sendBroadcast(i);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"== onNOtificationRemoved ==");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
//        Intent i = new Intent("io.helio.android.ui.eesd.NOTIFICATION_LISTENER");
//        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");
//        sendBroadcast(i);
		
	}
}
