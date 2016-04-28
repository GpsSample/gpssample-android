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
 * 
 */
import org.path.common.android.utilities.ODKFileUtils;
import org.path.common.android.utilities.StaticStateManipulator;
import org.path.common.android.utilities.StaticStateManipulator.IStaticFieldManipulator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class PlaceNameDatabaseFactory {

	private static PlaceNameDatabaseFactory placeNameDatabaseFactory = new PlaceNameDatabaseFactory();

	static {
		// register a state-reset manipulator for 'databaseFactory' field.
		StaticStateManipulator.get().register(50,
				new IStaticFieldManipulator() {

					@Override
					public void reset() {
						placeNameDatabaseFactory = new PlaceNameDatabaseFactory();
					}

				});
	}

	public static PlaceNameDatabaseFactory get() {
		return placeNameDatabaseFactory;
	}

	/**
	 * For mocking -- supply a mocked object.
	 * 
	 * @param databaseFactory
	 */
	public static void set(PlaceNameDatabaseFactory factory) {
		placeNameDatabaseFactory = factory;
	}

	protected PlaceNameDatabaseFactory() {
	}

	/**
	 * Shared accessor to get a database handle.
	 * 
	 * @param appName
	 * @return an entry in dbHelpers
	 */
	private synchronized PlaceNameDatabaseHelper getDbHelper(Context context,
			String appName) {
		// WebLogger log = null;

		String path = ODKFileUtils.getPlaceNameDbFolder(appName);
		/*
		 * File placeNameDb = new File(path); if ( !placeNameDb.exists() ||
		 * !placeNameDb.isDirectory()) {
		 * ODKFileUtils.assertDirectoryStructure(appName); }
		 * 
		 * // the assert above should have created it... if (
		 * !placeNameDb.exists() || !placeNameDb.isDirectory()) {
		 * log.e("placeNameDatabaseFactory",
		 * "placeNameDb directory not available"); return null; }
		 */

		PlaceNameDatabaseHelper placeNameHelper = PlaceNameDatabaseHelper
				.getInstance(context, appName, path);

		return placeNameHelper;
	}

	public SQLiteDatabase getDatabase(Context context, String appName) {
		return getDbHelper(context, appName).getWritableDatabase();
	}
}
