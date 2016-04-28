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

import org.path.common.android.utilities.CensusUtil;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.RemoveCensusDataFromPreviousDateListener;
import org.path.episample.android.utilities.BackupRestoreUtils;

import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class RemoveCensusDataFromPreviousDateTask extends
		AsyncTask<Void, String, Void> {
	private static final String t = "RemoveCensusDataFromPreviousDateTask";
	private RemoveCensusDataFromPreviousDateListener mListener;
	private boolean mSuccess = true;
	private long mNumOfAffectedRows = 0;

	@Override
	protected Void doInBackground(Void... params) {
		try {
			BackupRestoreUtils.backupCensus(Survey.getInstance()
					.getApplicationContext(), "survey");
			mNumOfAffectedRows = CensusUtil.deleteCensusFromPreviousDate(Survey
					.getInstance().getApplicationContext());

		} catch (Exception ex) {
			mSuccess = false;
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		synchronized (this) {
			if (mListener != null) {
				mListener.removeCensusFromPreviousDateComplete(mSuccess,
						mNumOfAffectedRows);
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		synchronized (this) {
			if (mListener != null) {
				mListener.removeCensusFromPreviousDateProgressUpdate(values[0]);
			}
		}
	}

	@Override
	protected void onCancelled() {
		synchronized (this) {
			if (mListener != null) {
				mListener.removeCensusFromPreviousDateCanceled();
			}
		}
	}

	public void setRemoveCensusDataFromPreviousDateListener(
			RemoveCensusDataFromPreviousDateListener listener) {
		synchronized (this) {
			mListener = listener;
		}
	}

	public boolean getSuccess() {
		return mSuccess;
	}

	public long getNumOfAffectedRows() {
		return mNumOfAffectedRows;
	}
}
