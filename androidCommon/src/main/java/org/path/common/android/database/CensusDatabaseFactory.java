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

package org.path.common.android.database;

/*
 * @author belendia@gmail.com
 */

import org.path.common.android.utilities.ODKFileUtils;
import org.path.common.android.utilities.StaticStateManipulator;
import org.path.common.android.utilities.StaticStateManipulator.IStaticFieldManipulator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class CensusDatabaseFactory {

	private static CensusDatabaseFactory censusDatabaseFactory = new CensusDatabaseFactory();

	static {
		// register a state-reset manipulator for 'databaseFactory' field.
		StaticStateManipulator.get().register(50,
				new IStaticFieldManipulator() {

					@Override
					public void reset() {
						censusDatabaseFactory = new CensusDatabaseFactory();
					}

				});
	}

	public static CensusDatabaseFactory get() {
		return censusDatabaseFactory;
	}

	/**
	 * For mocking -- supply a mocked object.
	 * 
	 * @param databaseFactory
	 */
	public static void set(CensusDatabaseFactory factory) {
		censusDatabaseFactory = factory;
	}

	protected CensusDatabaseFactory() {
	}

	/**
	 * Shared accessor to get a database handle.
	 * 
	 * @param appName
	 * @return an entry in dbHelpers
	 */
	private synchronized CensusDatabaseHelper getDbHelper(Context context,
			String appName) {
		// WebLogger log = null;

		String path = ODKFileUtils.getCensusDbFolder(appName);
		/*
		 * File censusDb = new File(path); if ( !censusDb.exists() ||
		 * !censusDb.isDirectory()) {
		 * ODKFileUtils.assertDirectoryStructure(appName); }
		 * 
		 * // the assert above should have created it... if ( !censusDb.exists()
		 * || !censusDb.isDirectory()) { log.e("CensusDatabaseFactory",
		 * "censusDb directory not available"); return null; }
		 */

		CensusDatabaseHelper censusHelper = CensusDatabaseHelper.getInstance(
				context, appName, path);

		return censusHelper;
	}

	private synchronized BackupCensusDatabaseHelper getDbHelper(
			Context context, String appName, String path) {
		BackupCensusDatabaseHelper censusHelper = BackupCensusDatabaseHelper
				.getInstance(context, appName, path);

		return censusHelper;
	}

	public SQLiteDatabase getDatabase(Context context, String appName) {
		return getDbHelper(context, appName).getWritableDatabase();
	}

	// used mainly by restore module
	public SQLiteDatabase getBackupDatabase(Context context, String appName,
			String path) {
		return getDbHelper(context, appName, path).getWritableDatabase();
	}
}
