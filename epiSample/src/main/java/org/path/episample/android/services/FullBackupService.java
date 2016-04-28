package org.path.episample.android.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.path.common.android.database.CensusDatabaseHelper;
import org.path.common.android.database.WebDbDefinition;
import org.path.common.android.database.WebSqlDatabaseHelper;
import org.path.common.android.utilities.ODKFileUtils;
import org.path.episample.android.utilities.BackupRestoreUtils;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class FullBackupService extends Service {
	private static final String TAG = "FullBackupService";

	FullBackupTask mBackup;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			mBackup = new FullBackupTask();
			mBackup.execute();
		} catch (Exception e) {
			Log.e(TAG, "Unable to backup");
		}

		return Service.START_FLAG_REDELIVERY;
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	private class FullBackupTask extends AsyncTask<Void, Void, Boolean> {

		private static final String TAG = "FullBackupTask";

		@Override
		protected Boolean doInBackground(Void... params) {
			String appName = "survey";

			ArrayList<String> backupDirs = BackupRestoreUtils
					.getAllBackupDirs();
			ArrayList<Calendar> backupDirTimestamps = BackupRestoreUtils
					.getAllBackupTimestamp(backupDirs);
			Calendar cal = Calendar.getInstance();
			if (BackupRestoreUtils.isBackupDoneToday(backupDirTimestamps) == false
					&& cal.get(Calendar.HOUR_OF_DAY) >= 15) {

				ODKFileUtils.createFolder(ODKFileUtils
						.getEpiSampleBackupFolder());

				String toPath = ODKFileUtils.getEpiSampleBackupFolder()
						+ File.separator
						+ BackupRestoreUtils
								.generateFolderName(getBaseContext())
						+ File.separator;

				ODKFileUtils.createFolder(toPath);

				try {
					ODKFileUtils.verifyExternalStorageAvailability();
					ODKFileUtils.assertDirectoryStructure(appName);
				} catch (Exception e) {

					return false;
				}

				String path = ODKFileUtils.getWebDbFolder(appName);
				File webDb = new File(path);
				if (!webDb.exists() || !webDb.isDirectory()) {
					ODKFileUtils.assertDirectoryStructure(appName);
				}

				// the assert above should have created it...
				if (!webDb.exists() || !webDb.isDirectory()) {
					return false;
				}

				WebSqlDatabaseHelper h;
				h = new WebSqlDatabaseHelper(getBaseContext(), path, appName);
				WebDbDefinition defn = h.getWebKitDatabaseInfoHelper();
				if (defn != null) {
					BackupRestoreUtils.copyFile(defn.dbFile.getPath(), toPath
							+ defn.dbFile.getName());
				}

				String censusFrom = ODKFileUtils.getCensusDbFolder(appName)
						+ File.separator;
				BackupRestoreUtils.copyFile(censusFrom
						+ CensusDatabaseHelper.CENSUS_DATABASE_NAME, toPath
						+ CensusDatabaseHelper.CENSUS_DATABASE_NAME);
			}
			return true;
		}
	}

}
