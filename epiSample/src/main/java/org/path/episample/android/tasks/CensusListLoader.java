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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.path.common.android.data.CensusModel;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.CensusUtil.FilterCensusList;
import org.path.common.android.utilities.CensusUtil.Sort;
import org.path.episample.android.fragments.NavigateFragment;
import org.path.episample.android.utilities.DistanceUtil;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

/*
 * @author belendia@gmail.com
 */

public class CensusListLoader extends AsyncTaskLoader<List<CensusModel>> {
	private static final String t = "CensusListLoader";
	private String appName;
	private Bundle mArgs;
	private double mLatitude;
	private double mLongitude;

	public CensusListLoader(Context context, String appName, Bundle args) {
		super(context);
		this.appName = appName;
		mArgs = args;
	}

	@Override
	public List<CensusModel> loadInBackground() {
		Sort sort = NavigateFragment.mSort != null ? NavigateFragment.mSort
				: Sort.POINT_TYPES;
		if (mArgs != null) {
			/*
			 * if(mArgs.containsKey(NavigateFragment.SORT)) { sort =
			 * Sort.values()[mArgs.getInt(NavigateFragment.SORT)]; }
			 */

			if (mArgs.containsKey(NavigateFragment.CURRENT_LATITUDE)) {
				mLatitude = mArgs.getDouble(NavigateFragment.CURRENT_LATITUDE);
			}

			if (mArgs.containsKey(NavigateFragment.CURRENT_LONGITUDE)) {
				mLongitude = mArgs
						.getDouble(NavigateFragment.CURRENT_LONGITUDE);
			}
		}

		ArrayList<CensusModel> result = new ArrayList<CensusModel>();
		try {
			FilterCensusList params[] = NavigateFragment.mFilterCensusList
					.toArray(new FilterCensusList[NavigateFragment.mFilterCensusList
							.size()]);
			result = CensusUtil.getCensus(getContext(), sort,
					NavigateFragment.mSelectedCode, params);

			if (mArgs != null) {
				calculateDistance(result);
			}

			if (sort == Sort.CLOSEST) {
				closestSort(result);
			} else if (sort == Sort.FARTHEST) {
				farthestSort(result);
			}
		} finally {

		}

		return result;
	}

	private void calculateDistance(ArrayList<CensusModel> result) {
		int numOfCensus = result.size();
		for (int i = 0; i < numOfCensus; i++) {
			CensusModel census = result.get(i);
			census.setDistance(DistanceUtil.getDistance(mLatitude, mLongitude,
					census.getLatitude(), census.getLongitude()));
		}
	}

	private void closestSort(ArrayList<CensusModel> result) {
		Collections.sort(result, new Comparator<CensusModel>() {
			public int compare(CensusModel c1, CensusModel c2) {
				return Double.compare(c1.getDistance(), c2.getDistance());
			}
		});
	}

	private void farthestSort(ArrayList<CensusModel> result) {
		Collections.sort(result, new Comparator<CensusModel>() {
			public int compare(CensusModel c1, CensusModel c2) {
				return Double.compare(c2.getDistance(), c1.getDistance());
			}
		});
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
	public void onCanceled(List<CensusModel> censuses) {
		super.onCanceled(censuses);
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

}
