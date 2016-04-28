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

import org.path.common.android.provider.CensusColumns;
import org.path.common.android.utilities.ODKFileUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Opens and manages the Census.db database Java app.
 * 
 */
public class CensusDatabaseHelper extends ODKSQLiteOpenHelper {

	public static final String CENSUS_DATABASE_NAME = "Census.db";
	public static final String CENSUS_DATABASES_TABLE = "census";

	static final int CENSUS_VERSION = 1;

	final Context context;

	private static CensusDatabaseHelper mInstance;

	public static String dbPath(String path) {
		ODKFileUtils.createFolder(path);
		return path;
	}

	public CensusDatabaseHelper(Context context, String appName, String path) {
		super(appName, dbPath(path), CENSUS_DATABASE_NAME, null, CENSUS_VERSION);
		this.context = context;
	}

	/**
	 * Called when the database is created for the first time. This is where the
	 * creation of tables and the initial population of the tables should
	 * happen.
	 * 
	 * @param db
	 *            The database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(CensusColumns.getTableCreateSql(CENSUS_DATABASES_TABLE));
	}

	/**
	 * Called when the database needs to be upgraded. The implementation should
	 * use this method to drop tables, add tables, or do anything else it needs
	 * to upgrade to the new schema version.
	 * <p>
	 * The SQLite ALTER TABLE documentation can be found <a
	 * href="http://sqlite.org/lang_altertable.html">here</a>. If you add new
	 * columns you can use ALTER TABLE to insert them into a live table. If you
	 * rename or remove columns you can use ALTER TABLE to rename the old table,
	 * then create the new table and then populate the new table with the
	 * contents of the old table.
	 * 
	 * @param db
	 *            The database.
	 * @param oldVersion
	 *            The old database version.
	 * @param newVersion
	 *            The new database version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + CENSUS_DATABASES_TABLE + ";");

		onCreate(db);
	}

	public static CensusDatabaseHelper getInstance(Context context,
			String appName, String path) {

		if (mInstance == null) {

			mInstance = new CensusDatabaseHelper(
					context.getApplicationContext(), appName, path);
		}
		return mInstance;
	}
}