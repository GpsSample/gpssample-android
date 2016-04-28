/*
 * Copyright@ 2015 PATH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under 
 * the License.
 *
 */

package org.path.episample.android.tasks;

import java.util.ArrayList;

import org.path.common.android.data.CensusModel;
import org.path.common.android.database.CensusDatabaseFactory;
import org.path.common.android.database.DatabaseFactory;
import org.path.common.android.utilities.CensusUtil;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.RestoreListener;
import org.path.episample.android.utilities.BackupRestoreUtils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

/*
 * @author belendia@gmail.com
 */

public class RestoreTask extends AsyncTask<String, String, Void> {
	private static final String t = "RestoreTask";
	private RestoreListener mListener;
	private boolean mSuccess = true;
	private SQLiteDatabase mWebDb;
	private SQLiteDatabase mCensusDb;

	private SQLiteStatement mInsertSqlCensusDb;
	private SQLiteStatement mUpdateSqlCensusDb;
	private SQLiteStatement mInsertSqlWebDb;
	private SQLiteStatement mUpdateSqlWebDb;
	
	@Override
	protected Void doInBackground(String... params) {
		if (params.length > 0 && params[0].length() > 0
				&& BackupRestoreUtils.dbFileExists(params[0])) {
			try {

				CensusUtil.clearAll(Survey.getInstance()
						.getApplicationContext());
				mWebDb = DatabaseFactory.get().getDatabase(
						Survey.getInstance().getApplicationContext(), "survey");
				mCensusDb = CensusDatabaseFactory.get().getDatabase(
						Survey.getInstance().getApplicationContext(), "survey");

				mInsertSqlCensusDb = CensusUtil.getInsertStatementCensusDb(mCensusDb);
				mUpdateSqlCensusDb = CensusUtil.getUpdateStatementCensusDb(mCensusDb);
				mInsertSqlWebDb = CensusUtil.getInsertStatementWebDb(mWebDb);
				mUpdateSqlWebDb = CensusUtil.getUpdateStatementWebDb(mWebDb);
				
				mCensusDb.beginTransaction();
				mWebDb.beginTransaction();
				
				ArrayList<CensusModel> censusList = CensusUtil
						.getAllFromBackupDb(Survey.getInstance()
								.getApplicationContext(), params[0]);

				for (CensusModel censusModel : censusList) {
					CensusUtil.insertOrUpdate(Survey.getInstance()
							.getApplicationContext(), mInsertSqlCensusDb, mUpdateSqlCensusDb, mInsertSqlWebDb, mUpdateSqlWebDb,
							censusModel, true);
					if(isCancelled()) return null;
				}
				
				mCensusDb.setTransactionSuccessful();	
				mCensusDb.endTransaction();
			    
				mWebDb.setTransactionSuccessful();	
				mWebDb.endTransaction();
				
			} catch (Exception ex) {
				mSuccess = false;
			} finally {
				closeDB();
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		synchronized (this) {
			if (mListener != null) {
				mListener.restoreComplete(mSuccess);
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		synchronized (this) {
			if (mListener != null) {
				mListener.restoreProgressUpdate(values[0]);
			}
		}
	}

	@Override
	protected void onCancelled() {
		synchronized (this) {
			closeDB();
			if (mListener != null) {
				mListener.restoreCanceled();
			}
		}
	}

	public void setRestoreListener(RestoreListener listener) {
		synchronized (this) {
			mListener = listener;
		}
	}

	private void closeDB() {
		try {
			if(mInsertSqlCensusDb != null) {
				mInsertSqlCensusDb.close();
			}
			if(mUpdateSqlCensusDb != null) {
				mUpdateSqlCensusDb.close();
			}
			if(mInsertSqlWebDb != null) {
				mInsertSqlWebDb.close();
			}
			if(mUpdateSqlWebDb != null) {
				mUpdateSqlWebDb.close();
			}
			
			if (mWebDb != null && mWebDb.isOpen())
				mWebDb.close();
			if (mCensusDb != null && mCensusDb.isOpen())
				mCensusDb.close();
		} catch (Exception ex) {
			Log.d(t, "Error closing census or web database");
		}
	}

	public boolean getSuccess() {
		return mSuccess;
	}
}
