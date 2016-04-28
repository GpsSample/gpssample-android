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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.path.common.android.data.CensusModel;
import org.path.common.android.database.CensusDatabaseFactory;
import org.path.common.android.database.DatabaseFactory;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.JSONUtils;
import org.path.episample.android.R;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.ReceiveCensusDataListener;
import org.path.episample.android.utilities.BackupRestoreUtils;
import org.path.episample.android.utilities.SendReceiveSocketUtils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

/*
 * @author belendia@gmail.com
 */

public class ReceiveCensusDataTask extends AsyncTask<InetAddress, String, Void> {
	private static final String t = "ReceiveCensusDataTask";
	private ReceiveCensusDataListener mListener;
	private int mCounter = 0;
	private ServerSocket mServerSocket;
	private boolean mErrorOccured = false;
	private SQLiteDatabase mWebDb;
	private SQLiteDatabase mCensusDb;
	private SQLiteStatement mInsertSqlCensusDb;
	private SQLiteStatement mUpdateSqlCensusDb;
	private SQLiteStatement mInsertSqlWebDb;
	private SQLiteStatement mUpdateSqlWebDb;

	@Override
	protected Void doInBackground(InetAddress... params) {
		boolean stopListening = false;
		while (stopListening == false && isCancelled() == false) {
			try {
				mServerSocket = SendReceiveSocketUtils.getServerSocket();
				mServerSocket.setSoTimeout(1000);

				Socket client = mServerSocket.accept();
				if (client != null) {
					stopListening = true;

					BackupRestoreUtils.backupCensus(Survey.getInstance()
							.getApplicationContext(), "survey");
					publishProgress(Survey.getInstance().getString(
							R.string.receiving_data));

					mWebDb = DatabaseFactory.get().getDatabase(
							Survey.getInstance().getApplicationContext(),
							"survey");
					mCensusDb = CensusDatabaseFactory.get().getDatabase(
							Survey.getInstance().getApplicationContext(),
							"survey");

					mInsertSqlCensusDb = CensusUtil
							.getInsertStatementCensusDb(mCensusDb);
					mUpdateSqlCensusDb = CensusUtil
							.getUpdateStatementCensusDb(mCensusDb);
					mInsertSqlWebDb = CensusUtil
							.getInsertStatementWebDb(mWebDb);
					mUpdateSqlWebDb = CensusUtil
							.getUpdateStatementWebDb(mWebDb);

					mCensusDb.beginTransaction();
					mWebDb.beginTransaction();

					BufferedReader in = new BufferedReader(
							new InputStreamReader(client.getInputStream()));
					String data = null;
					while ((data = in.readLine()) != null
							&& isCancelled() == false) {
						ArrayList<CensusModel> censuses = JSONUtils
								.getCensusesFromString(data);
						for (int i = 0; i < censuses.size()
								&& isCancelled() == false; i++) {
							if (censuses.get(i) != null) {
								CensusUtil.insertOrUpdate(Survey.getInstance()
										.getApplicationContext(),
										mInsertSqlCensusDb, mUpdateSqlCensusDb,
										mInsertSqlWebDb, mUpdateSqlWebDb,
										censuses.get(i), true);
								mCounter++;
							}
						}
					}

					mCensusDb.setTransactionSuccessful();
					mCensusDb.endTransaction();

					mWebDb.setTransactionSuccessful();
					mWebDb.endTransaction();

					mInsertSqlCensusDb.close();
					mUpdateSqlCensusDb.close();
					mInsertSqlWebDb.close();
					mUpdateSqlWebDb.close();

					mServerSocket.close();
					mWebDb.close();
					mCensusDb.close();
				}

			} catch (IOException e) {
				Log.d(t, "Server error in receiver");
				// e.printStackTrace();
			} catch (Exception e) {
				mErrorOccured = true;
				closeDB();
				break;
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		synchronized (this) {
			closeSocket();
			if (mListener != null) {
				if (mErrorOccured == true) {
					mListener.receiveError(mCounter);
				} else {
					mListener.receivingComplete(mCounter);
				}
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		synchronized (this) {
			if (mListener != null) {
				mListener.receiveProgressUpdate(values[0]);
			}
		}
	}

	@Override
	protected void onCancelled() {
		synchronized (this) {
			closeSocket();
			closeDB();
			if (mListener != null) {
				mListener.receivingCanceled(mCounter);
			}
		}
	}

	public void setReceiveCensusDataListener(ReceiveCensusDataListener listener) {
		synchronized (this) {
			mListener = listener;
		}
	}

	public int getTotalSent() {
		return mCounter;
	}

	private void closeDB() {
		try {
			if (mInsertSqlCensusDb != null) {
				mInsertSqlCensusDb.close();
			}
			if (mUpdateSqlCensusDb != null) {
				mUpdateSqlCensusDb.close();
			}
			if (mInsertSqlWebDb != null) {
				mInsertSqlWebDb.close();
			}
			if (mUpdateSqlWebDb != null) {
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

	private void closeSocket() {
		if (mServerSocket != null) {
			try {
				if (mServerSocket.isClosed() == false) {
					mServerSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(t, "Error");
			}
		}
	}
}
