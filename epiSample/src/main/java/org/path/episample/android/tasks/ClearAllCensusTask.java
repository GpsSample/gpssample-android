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
import org.path.episample.android.listeners.ClearAllCensusListener;

import android.app.Application;
import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class ClearAllCensusTask extends AsyncTask<Void, Void, Void> {
	private static final String t = "ClearAllCensusTask";
	private ClearAllCensusListener mClearAllCensusListener;

	private Application appContext;
	private String appName;

	@Override
	protected Void doInBackground(Void... arg0) {
		CensusUtil.clearAll(getApplication());

		return null;
	}

	@Override
	protected void onPostExecute(Void v) {
		synchronized (this) {
			if (mClearAllCensusListener != null) {
				mClearAllCensusListener.complete();
			}
		}
	}

	public void setListener(ClearAllCensusListener listener) {
		synchronized (this) {
			mClearAllCensusListener = listener;
		}
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
