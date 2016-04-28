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
import java.util.List;

import org.path.common.android.utilities.CensusUtil;

import android.content.AsyncTaskLoader;
import android.content.Context;

/*
 * @author belendia@gmail.com
 */

public class CensusListByDateLoader extends AsyncTaskLoader<List<String>> {
	private static final String t = "CensusListByDateLoader";
	private String mAppName;

	public CensusListByDateLoader(Context context, String appName) {
		super(context);
		this.mAppName = appName;
	}

	@Override
	public List<String> loadInBackground() {
		List<String> rows = new ArrayList<String>();
		try {
			rows = CensusUtil.getAllCensusCreatedDate(getContext());
		} finally {

		}

		return rows;
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<String> rows) {
		super.onCanceled(rows);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();
	}

	public String getAppName() {
		return mAppName;
	}
}
