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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.wink.common.internal.providers.entity.csv.CsvReader;
import org.path.common.android.data.PlaceModel;
import org.path.common.android.database.PlaceNameDatabaseFactory;
import org.path.common.android.utilities.ODKFileUtils;
import org.path.common.android.utilities.PlaceNameUtil;
import org.path.episample.android.listeners.PlaceNameLoaderListener;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class LoadPlaceTask extends AsyncTask<Void, Void, Void> {
	private static final String t = "LoadPlaceTask";
	private PlaceNameLoaderListener mPlaceNameReaderListener;
	/*
	 * private static final char DELIMITING_CHAR = ','; private static final
	 * char QUOTE_CHAR = '"'; private static final char ESCAPE_CHAR = '\0';
	 */

	private Application appContext;
	private String appName;

	@Override
	protected Void doInBackground(Void... arg0) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(
				getApplication(), getAppName());

		readPlace(db);

		db.close();

		return null;
	}

	private void readPlace(SQLiteDatabase db) {
		CsvReader reader = null;
		try {
			reader = new CsvReader(new InputStreamReader(new FileInputStream(
					ODKFileUtils.getPlaceNameCsvFile(getAppName())), "UTF-8"));

			PlaceNameUtil.delete(db);
			// db.beginTransaction();

			String[] row;
			while ((row = reader.readLine()) != null) {
				if (isCancelled() == true) {
					break;
				}

				PlaceModel place = new PlaceModel();

				place.setHierarchyName(row[0]);
				place.setCode(row[1]);
				place.setName(row[2]);
				place.setRelationship(row[3]);

				PlaceNameUtil.insertPlace(db, place);
			}

			// db.setTransactionSuccessful();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// db.endTransaction();
		}
	}

	@Override
	protected void onPostExecute(Void v) {
		synchronized (this) {
			if (mPlaceNameReaderListener != null) {
				mPlaceNameReaderListener.loadingComplete();
			}
		}
	}

	public void setListener(PlaceNameLoaderListener listener) {
		synchronized (this) {
			mPlaceNameReaderListener = listener;
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
