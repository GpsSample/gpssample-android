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

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Opens and manages the backup census.db databases Java app.
 * 
 */
public class BackupCensusDatabaseHelper extends ODKSQLiteOpenHelper {

	public static final String CENSUS_DATABASES_TABLE = "census";

	static final int CENSUS_VERSION = 1;

	final Context context;

	public BackupCensusDatabaseHelper(Context context, String appName,
			String path, String dbName) {
		super(appName, path, dbName, null, CENSUS_VERSION);

		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public static BackupCensusDatabaseHelper getInstance(Context context,
			String appName, String path) {
		String dbName = new File(path).getName();
		String dbPath = new File(path).getParent();

		return new BackupCensusDatabaseHelper(context.getApplicationContext(),
				appName, dbPath, dbName);
	}
}