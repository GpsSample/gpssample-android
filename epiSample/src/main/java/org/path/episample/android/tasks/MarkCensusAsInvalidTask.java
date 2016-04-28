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

import org.path.common.android.data.CensusModel;
import org.path.common.android.utilities.CensusUtil;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.InvalidateCensusFragment.DistanceType;
import org.path.episample.android.listeners.MarkCensusAsInvalidListener;
import org.path.episample.android.logic.PairSearch;
import org.path.episample.android.logic.PairSearch.Pair;

import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class MarkCensusAsInvalidTask extends AsyncTask<Integer, String, Void> {
	private static final String t = "MarkCensusAsInvalidTask";
	private MarkCensusAsInvalidListener mListener;
	private List<Pair> mPairs = new ArrayList<Pair>();

	@Override
	protected Void doInBackground(Integer... params) {
		try {							
			
			if(params.length>1) {
				int meters = params[0];
				DistanceType type = DistanceType.values()[params[1]];
				
				ArrayList<CensusModel> census = CensusUtil.getAllValid(Survey.getInstance().getApplicationContext());
				PairSearch pairSearch = new PairSearch();
				
				if(type == DistanceType.Farthest) {
					mPairs = pairSearch.getAllFarthestPairs(census, meters);
				} else {
					mPairs = pairSearch.getAllClosestPairs(census, meters);
				}

			}
			
		} catch (Exception ex) {
			
		} 
		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		synchronized (this) {
			if (mListener != null) {
				mListener.markAsInvalidComplete(mPairs);
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		synchronized (this) {
			if (mListener != null) {
				mListener.markAsInvalidProgressUpdate(values[0]);
			}
		}
	}

	@Override
	protected void onCancelled() {
		synchronized (this) {
			if (mListener != null) {
				mListener.markAsInvalidCanceled();
			}
		}
	}

	public void setMarkCensusAsInvalidListener(MarkCensusAsInvalidListener listener) {
		synchronized (this) {
			mListener = listener;
		}
	}

	public List<Pair> getPairs() {
		return mPairs;
	}
}
