package org.path.episample.android.logic;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import org.path.common.android.data.CensusModel;
import org.path.common.android.database.CensusDatabaseFactory;
import org.path.common.android.utilities.CensusUtil;
import org.path.episample.android.listeners.ISelectAlgorithm;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class RandomSample implements ISelectAlgorithm {

	private Application appContext;
	private String appName;
	private AsyncTask mTask;

	public RandomSample(Application appContext, String appName,
			AsyncTask task) {
		setApplication(appContext);
		setAppName(appName);
		mTask = task;
	}

	@Override
	public boolean select(int numberOfMainPoints, int numberOfAdditionalPoints,
			int numberOfAlternatePoints) {
		// reset all random values in Census.db to zero
		CensusUtil.resetRandomAndSelectedValueToZero(getApplication());
		
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String dateLastSelected = sdf.format(calendar.getTime());

		int sampleFrame = CensusUtil.getTotalValid(getApplication())
				+ CensusUtil.getTotalInvalid(getApplication());

		ArrayList<CensusModel> censuses = CensusUtil
				.getAllValid(getApplication());// get all valid points from
												// Census.db

		censuses = assignRandomValues(censuses); // assign random values to each census
		
		if (mTask.isCancelled() == true) {
			return false;
		}
		
		sortCensuses(censuses);

		// make the selection
		int totalPointsToSelect = numberOfMainPoints + numberOfAdditionalPoints
				+ numberOfAlternatePoints;
		int selectionSize = censuses.size() > totalPointsToSelect ? totalPointsToSelect
				: censuses.size();
		int mainPlusAdditionalPoint = numberOfMainPoints
				+ numberOfAdditionalPoints;

		for (int i = 0; i < selectionSize && mTask.isCancelled() == false; i++) {
			if (i < numberOfMainPoints) {// select as main point
				censuses.get(i).setSelected(CensusModel.MAIN_POINT);
			} else if (i >= numberOfMainPoints && i < mainPlusAdditionalPoint) {
				// select as additional point
				censuses.get(i).setSelected(CensusModel.ADDITIONAL_POINT);
			} else if (i >= mainPlusAdditionalPoint && i < totalPointsToSelect) {
				// select as alternate point
				censuses.get(i).setSelected(CensusModel.ALTERNATE_POINT);
			}
		}

		if (mTask.isCancelled() == true) {
			return false;
		}
		
		saveSelectionToCensusDb(censuses);
		saveSampleFrameToCensusDb(dateLastSelected, sampleFrame);

		boolean status = CensusUtil.syncAllRecordToWebDb(getApplication());
		return status;
	}

	@Override
	public void setAppName(String appName) {
		synchronized (this) {
			this.appName = appName;
		}
	}

	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public void setApplication(Application appContext) {
		synchronized (this) {
			this.appContext = appContext;
		}
	}

	@Override
	public Application getApplication() {
		return appContext;
	}

	private ArrayList<CensusModel> assignRandomValues(ArrayList<CensusModel> censuses) {
		SecureRandom random = null;

		try {
			random = SecureRandom.getInstance("SHA1PRNG");

			byte[] bytes = new byte[512];
			random.nextBytes(bytes);
		} catch (NoSuchAlgorithmException noSuchAlgo) {
			random = new SecureRandom();
		}
		
		ArrayList<CensusModel> randomizedCensuses = new ArrayList<CensusModel>();

		Collections.shuffle(censuses);
		
		while(censuses.size()>0 && mTask.isCancelled() == false) {
			CensusModel census;
			if(censuses.size() > 1) {
				census = censuses.remove(random.nextInt(censuses.size()));
			} else {
				census = censuses.remove(0);
			}
			census.setRandom(random.nextDouble());
			randomizedCensuses.add(census);
		}

		return randomizedCensuses;
	}
	
	private void sortCensuses(ArrayList<CensusModel> censuses) {
		Collections.sort(censuses, new Comparator<CensusModel>() {
			public int compare(CensusModel c1, CensusModel c2) {
				return Double.compare(c1.getRandom(), c2.getRandom());
			}
		});
	}

	private void saveSelectionToCensusDb(ArrayList<CensusModel> censuses) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(
				getApplication(), getAppName());
		for (CensusModel census : censuses) {
			CensusUtil.updateSelected(db, census);
		}
		db.close();
	}

	private void saveSampleFrameToCensusDb(String dateLastSelected,
			int sampleFrame) {

		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(
				getApplication(), getAppName());

		CensusUtil.updateSampleFrame(db, dateLastSelected, sampleFrame);
		db.close();
	}
}
