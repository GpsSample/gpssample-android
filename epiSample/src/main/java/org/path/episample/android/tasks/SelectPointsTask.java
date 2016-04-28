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

import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.PointSelectionListener;
import org.path.episample.android.logic.RandomSample;
import org.path.episample.android.utilities.BackupRestoreUtils;

import android.app.Application;
import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class SelectPointsTask extends AsyncTask<Integer, Void, Void> {
	private static final String t = "SelectPointsTask";
	private Application appContext;
	private String appName;
	private boolean mSuccess = false;
	private PointSelectionListener psl;

	@Override
	protected Void doInBackground(Integer... params) {
		int numberOfMainPoints = params.length > 0 ? params[0] : 0;
		int numberOfAdditionalPoints = params.length > 1 ? params[1] : 0;
		int numberOfAlternatePoints = params.length > 2 ? params[2] : 0;

		BackupRestoreUtils.backupCensus(Survey.getInstance().getApplicationContext(), "survey");
		// ToDo: check what type of algorithm user selected in the admin setting
		// and do the selection based on that
		RandomSample randomSample = new RandomSample(getApplication(),
				getAppName(), this);
		mSuccess = randomSample.select(numberOfMainPoints,
				numberOfAdditionalPoints, numberOfAlternatePoints);
		
		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		synchronized (this) {
			appContext = null;
			if (psl != null) {
				psl.selectionComplete(mSuccess);
			}
		}
	}

	@Override
	  protected void onCancelled() {
	    synchronized (this) {
	      appContext = null;
	      if (psl != null) {
	        psl.selectionCanceled();
	      }
	    }
	  }
	
	public void setPointSelectionListener(PointSelectionListener listener) {
		synchronized (this) {
			psl = listener;
		}
	}
	
	public boolean getSuccess() {
		return mSuccess;
	}
	
	public void setAppName(String appName) {
		synchronized (this) {
			this.appName = appName;
		}
	}

	public String getAppName() {
		return appName;
	}

	public void setApplication(Application appContext) {
		synchronized (this) {
			this.appContext = appContext;
		}
	}

	public Application getApplication() {
		return appContext;
	}
}
