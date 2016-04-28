package org.path.episample.android.utilities;

import java.util.Calendar;

import org.path.episample.android.receivers.FullBackupBroadcastReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmUtil {
	public static final int BACKUP = 21753;
	public static final String ACTION = "org.path.episample.android.BACKUP_NOW";
	
	public static void setAlarm(Context context) {
	    AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	    Calendar cal=Calendar.getInstance();
	    
	    cal.set(Calendar.HOUR_OF_DAY, 18);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    
	    if (cal.getTimeInMillis()<System.currentTimeMillis()) {
	      cal.add(Calendar.DAY_OF_YEAR, 1);
	    }
	    
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
	                      AlarmManager.INTERVAL_DAY,
	                      getPendingIntent(context));
	  }
	  
	  public static void cancelAlarm(Context context) {
	    AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

	    alarmManager.cancel(getPendingIntent(context));
	  }
	  
	  private static PendingIntent getPendingIntent(Context context) {
		  
		  Intent backupIntent = new Intent(context, FullBackupBroadcastReceiver.class);
		    backupIntent.setAction(ACTION);
		    PendingIntent recurringBackup = PendingIntent.getBroadcast(context,
		    		BACKUP, backupIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    
	    return recurringBackup;
	  }

}
