package org.path.episample.android.receivers;

import org.path.episample.android.services.FullBackupService;
import org.path.episample.android.utilities.AlarmUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FullBackupBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmUtil.setAlarm(context);

		if(AlarmUtil.ACTION.equals(intent.getAction())) {
			Intent fullBackupIntent = new Intent(context, FullBackupService.class);
	        context.startService(fullBackupIntent);
		}
	}
}
