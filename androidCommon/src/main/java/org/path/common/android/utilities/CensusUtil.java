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

package org.path.common.android.utilities;

import java.util.ArrayList;

import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.path.common.android.data.CensusModel;
import org.path.common.android.database.CensusDatabaseFactory;
import org.path.common.android.database.CensusDatabaseHelper;
import org.path.common.android.database.DatabaseFactory;
import org.path.common.android.provider.CensusColumns;
import org.path.common.android.provider.DataTableColumns;
import org.path.common.android.provider.InstanceColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/*
 * @author belendia@gmail.com
 * 
 */

public class CensusUtil {
	public static enum Sort {
		POINT_TYPES, CLOSEST, FARTHEST, NEWEST_ENTRY, OLDEST_ENTRY, ENUMERATOR_NAME, DEVICE_ID
	};

	public static long insertOrUpdate(Context context, CensusModel census,
			boolean updateWebDb) {
		long result = 0;
		ContentValues cv = new ContentValues();

		cv.put(CensusColumns.PLACE_NAME, census.getPlaceName());
		cv.put(CensusColumns.HOUSE_NUMBER, census.getHouseNumber());
		cv.put(CensusColumns.HEAD_NAME, census.getHeadName());
		cv.put(CensusColumns.COMMENT, census.getComment());
		cv.put(CensusColumns.LATITUDE, census.getLatitude());
		cv.put(CensusColumns.LONGITUDE, census.getLongitude());
		cv.put(CensusColumns.ALTITUDE, census.getAltitude());
		cv.put(CensusColumns.ACCURACY, census.getAccuracy());
		cv.put(CensusColumns.SELECTED, census.getSelected());
		cv.put(CensusColumns.RANDOM, census.getRandom());
		cv.put(CensusColumns.VALID, census.getValid());
		cv.put(CensusColumns.EXCLUDED, census.getExcluded());
		cv.put(CensusColumns.PROCESSED, census.getProcessed());
		cv.put(CensusColumns.PHONE_NUMBER, census.getPhoneNumber());
		cv.put(CensusColumns.INTERVIEWER_NAME, census.getInterviewerName());
		cv.put(CensusColumns.DEVICE_ID, census.getDeviceId());
		cv.put(CensusColumns.DEVICE_NAME, census.getDeviceName());
		cv.put(CensusColumns.DATE_LAST_SELECTED, census.getDateLastSelected());
		cv.put(CensusColumns.SAMPLE_FRAME, census.getSampleFrame());
		cv.put(CensusColumns.CREATED_DATE, census.getCreatedDate());
		cv.put(CensusColumns.UPDATED_DATE, census.getUpdatedDate());
		cv.put(CensusColumns.FORM_NAME, census.getFormName());

		boolean existingRecord = census.getInstanceId() != null
				&& census.getInstanceId().length() != 0
				&& exists(context, census.getInstanceId());
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		if (existingRecord) {// update

			result = db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
					CensusColumns._ID + "=?",
					new String[] { census.getInstanceId() });
		} else { // insert

			// census.setInstanceId(UUID.randomUUID().toString());
			cv.put(CensusColumns._ID, census.getInstanceId());
			result = db.insert(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
					census.getInstanceId(), cv);
		}
		db.close();
		if (updateWebDb == true) {
			// insert into survey's database census table
			insertOrUpdateWebDB(context, cv, census, existingRecord);
		}
		return result;
	}

	public static void insertOrUpdate(Context context,
			SQLiteStatement insertSqlCensusDb,
			SQLiteStatement updateSqlCensusDb, SQLiteStatement insertSqlWebDb,
			SQLiteStatement updateSqlWebDb, CensusModel census,
			boolean updateWebDb) {
		boolean existingRecord = census.getInstanceId() != null
				&& census.getInstanceId().length() != 0
				&& exists(context, census.getInstanceId());

		if (existingRecord) {// update

			updateSqlCensusDb.clearBindings();
			updateSqlCensusDb.bindString(1, census.getInstanceId());

			if (census.getPlaceName() == null)
				updateSqlCensusDb.bindNull(2);
			else
				updateSqlCensusDb.bindString(2, census.getPlaceName());

			if (census.getHouseNumber() == null)
				updateSqlCensusDb.bindNull(3);
			else
				updateSqlCensusDb.bindString(3, census.getHouseNumber());

			if (census.getHeadName() == null)
				updateSqlCensusDb.bindNull(4);
			else
				updateSqlCensusDb.bindString(4, census.getHeadName());

			if (census.getComment() == null)
				updateSqlCensusDb.bindNull(5);
			else
				updateSqlCensusDb.bindString(5, census.getComment());

			updateSqlCensusDb.bindDouble(6, census.getLatitude());
			updateSqlCensusDb.bindDouble(7, census.getLongitude());
			updateSqlCensusDb.bindDouble(8, census.getAltitude());
			updateSqlCensusDb.bindDouble(9, census.getAccuracy());
			updateSqlCensusDb.bindLong(10, census.getSelected());
			updateSqlCensusDb.bindDouble(11, census.getRandom());
			updateSqlCensusDb.bindLong(12, census.getValid());
			updateSqlCensusDb.bindLong(13, census.getExcluded());
			updateSqlCensusDb.bindLong(14, census.getProcessed());

			if (census.getPhoneNumber() == null)
				updateSqlCensusDb.bindNull(15);
			else
				updateSqlCensusDb.bindString(15, census.getPhoneNumber());

			if (census.getInterviewerName() == null)
				updateSqlCensusDb.bindNull(16);
			else
				updateSqlCensusDb.bindString(16, census.getInterviewerName());

			if (census.getDeviceId() == null)
				updateSqlCensusDb.bindNull(17);
			else
				updateSqlCensusDb.bindString(17, census.getDeviceId());

			if (census.getDeviceName() == null)
				updateSqlCensusDb.bindNull(18);
			else
				updateSqlCensusDb.bindString(18, census.getDeviceName());

			if (census.getDateLastSelected() == null)
				updateSqlCensusDb.bindNull(19);
			else
				updateSqlCensusDb.bindString(19, census.getDateLastSelected());

			updateSqlCensusDb.bindLong(20, census.getSampleFrame());

			if (census.getCreatedDate() == null)
				updateSqlCensusDb.bindNull(21);
			else
				updateSqlCensusDb.bindString(21, census.getCreatedDate());

			if (census.getUpdatedDate() == null)
				updateSqlCensusDb.bindNull(22);
			else
				updateSqlCensusDb.bindString(22, census.getUpdatedDate());

			if (census.getFormName() == null)
				updateSqlCensusDb.bindNull(23);
			else
				updateSqlCensusDb.bindString(23, census.getFormName());

			updateSqlCensusDb.bindString(24, census.getInstanceId());
			updateSqlCensusDb.execute();
		} else { // insert

			insertSqlCensusDb.clearBindings();
			insertSqlCensusDb.bindString(1, census.getInstanceId());
			if (census.getPlaceName() == null)
				insertSqlCensusDb.bindNull(2);
			else
				insertSqlCensusDb.bindString(2, census.getPlaceName());
			if (census.getHouseNumber() == null)
				insertSqlCensusDb.bindNull(3);
			else
				insertSqlCensusDb.bindString(3, census.getHouseNumber());
			if (census.getHeadName() == null)
				insertSqlCensusDb.bindNull(4);
			else
				insertSqlCensusDb.bindString(4, census.getHeadName());
			if (census.getComment() == null)
				insertSqlCensusDb.bindNull(5);
			else
				insertSqlCensusDb.bindString(5, census.getComment());

			insertSqlCensusDb.bindDouble(6, census.getLatitude());
			insertSqlCensusDb.bindDouble(7, census.getLongitude());
			insertSqlCensusDb.bindDouble(8, census.getAltitude());
			insertSqlCensusDb.bindDouble(9, census.getAccuracy());
			insertSqlCensusDb.bindLong(10, census.getSelected());
			insertSqlCensusDb.bindDouble(11, census.getRandom());
			insertSqlCensusDb.bindLong(12, census.getValid());
			insertSqlCensusDb.bindLong(13, census.getExcluded());
			insertSqlCensusDb.bindLong(14, census.getProcessed());

			if (census.getPhoneNumber() == null)
				insertSqlCensusDb.bindNull(15);
			else
				insertSqlCensusDb.bindString(15, census.getPhoneNumber());

			if (census.getInterviewerName() == null)
				insertSqlCensusDb.bindNull(16);
			else
				insertSqlCensusDb.bindString(16, census.getInterviewerName());

			if (census.getDeviceId() == null)
				insertSqlCensusDb.bindNull(17);
			else
				insertSqlCensusDb.bindString(17, census.getDeviceId());

			if (census.getDeviceName() == null)
				insertSqlCensusDb.bindNull(18);
			else
				insertSqlCensusDb.bindString(18, census.getDeviceName());

			if (census.getDateLastSelected() == null)
				insertSqlCensusDb.bindNull(19);
			else
				insertSqlCensusDb.bindString(19, census.getDateLastSelected());

			insertSqlCensusDb.bindLong(20, census.getSampleFrame());

			if (census.getCreatedDate() == null)
				insertSqlCensusDb.bindNull(21);
			else
				insertSqlCensusDb.bindString(21, census.getCreatedDate());

			if (census.getUpdatedDate() == null)
				insertSqlCensusDb.bindNull(22);
			else
				insertSqlCensusDb.bindString(22, census.getUpdatedDate());

			if (census.getFormName() == null)
				insertSqlCensusDb.bindNull(23);
			else
				insertSqlCensusDb.bindString(23, census.getFormName());

			insertSqlCensusDb.execute();
		}

		if (updateWebDb == true) {
			// insert into survey's database census table
			insertOrUpdateWebDB(insertSqlWebDb, updateSqlWebDb, census);
		}

	}

	private static void insertOrUpdateWebDB(Context context, ContentValues cv,
			CensusModel census, boolean existingRecord) {
		cv.remove(CensusColumns.LATITUDE);
		cv.remove(CensusColumns.LONGITUDE);
		cv.remove(CensusColumns.ALTITUDE);
		cv.remove(CensusColumns.ACCURACY);
		cv.remove(CensusColumns._ID);
		cv.remove(CensusColumns.PROCESSED);

		cv.put(CensusColumns.FDN + "_" + CensusColumns.LATITUDE,
				census.getLatitude());
		cv.put(CensusColumns.FDN + "_" + CensusColumns.LONGITUDE,
				census.getLongitude());
		cv.put(CensusColumns.FDN + "_" + CensusColumns.ALTITUDE,
				census.getAltitude());
		cv.put(CensusColumns.FDN + "_" + CensusColumns.ACCURACY,
				census.getAccuracy());

		cv.put(DataTableColumns.ROW_ETAG, DataTableColumns.DEFAULT_ROW_ETAG);
		cv.put(DataTableColumns.SYNC_STATE, SyncState.new_row.name());
		cv.putNull(DataTableColumns.CONFLICT_TYPE);
		cv.put(DataTableColumns.FILTER_TYPE,
				DataTableColumns.DEFAULT_FILTER_TYPE);
		cv.put(DataTableColumns.FILTER_VALUE,
				DataTableColumns.DEFAULT_FILTER_VALUE);
		cv.put(DataTableColumns.FORM_ID,
				CensusDatabaseHelper.CENSUS_DATABASES_TABLE);
		cv.put(DataTableColumns.LOCALE, DataTableColumns.DEFAULT_LOCALE);
		cv.put(DataTableColumns.SAVEPOINT_TYPE,
				SavepointTypeManipulator.complete());

		String timeStamp = TableConstants.nanoSecondsFromMillis(System
				.currentTimeMillis());
		cv.put(DataTableColumns.SAVEPOINT_TIMESTAMP, timeStamp);
		cv.put(DataTableColumns.SAVEPOINT_CREATOR,
				DataTableColumns.DEFAULT_SAVEPOINT_CREATOR);
		cv.put(DataTableColumns.ID, census.getInstanceId());

		SQLiteDatabase db = DatabaseFactory.get().getDatabase(
				context.getApplicationContext(), "survey");// DatabaseFactory.getInstanceDatabase(context,
															// "survey");

		if (existingRecord) {// update
			db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
					DataTableColumns.ID + "=?",
					new String[] { census.getInstanceId() });
		} else {// insert
			db.insert(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
					census.getInstanceId(), cv);
		}
		db.close();
	}

	private static void insertOrUpdateWebDB(SQLiteStatement insertSqlWebDb,
			SQLiteStatement updateSqlWebDb, CensusModel census) {

		updateSqlWebDb.clearBindings();
		updateSqlWebDb.bindString(1, census.getInstanceId());
		updateSqlWebDb.bindNull(2);// updateSqlWebDb.bindString(2,
									// DataTableColumns.DEFAULT_ROW_ETAG);
		updateSqlWebDb.bindString(3, SyncState.new_row.name());
		updateSqlWebDb.bindNull(4);
		updateSqlWebDb.bindString(5, DataTableColumns.DEFAULT_FILTER_TYPE);
		updateSqlWebDb.bindNull(6);// updateSqlWebDb.bindString(6,
									// DataTableColumns.DEFAULT_FILTER_VALUE);
		updateSqlWebDb.bindString(7,
				CensusDatabaseHelper.CENSUS_DATABASES_TABLE);
		updateSqlWebDb.bindString(8, DataTableColumns.DEFAULT_LOCALE);
		updateSqlWebDb.bindString(9, SavepointTypeManipulator.complete());

		String timeStamp = TableConstants.nanoSecondsFromMillis(System
				.currentTimeMillis());
		updateSqlWebDb.bindString(10, timeStamp);
		updateSqlWebDb.bindString(11,
				DataTableColumns.DEFAULT_SAVEPOINT_CREATOR);

		if (census.getPlaceName() == null)
			updateSqlWebDb.bindNull(12);
		else
			updateSqlWebDb.bindString(12, census.getPlaceName());

		if (census.getHouseNumber() == null)
			updateSqlWebDb.bindNull(13);
		else
			updateSqlWebDb.bindString(13, census.getHouseNumber());

		if (census.getHeadName() == null)
			updateSqlWebDb.bindNull(14);
		else
			updateSqlWebDb.bindString(14, census.getHeadName());

		if (census.getComment() == null)
			updateSqlWebDb.bindNull(15);
		else
			updateSqlWebDb.bindString(15, census.getComment());

		updateSqlWebDb.bindDouble(16, census.getLatitude());
		updateSqlWebDb.bindDouble(17, census.getLongitude());
		updateSqlWebDb.bindDouble(18, census.getAltitude());
		updateSqlWebDb.bindDouble(19, census.getAccuracy());
		updateSqlWebDb.bindLong(20, census.getSelected());
		updateSqlWebDb.bindDouble(21, census.getRandom());
		updateSqlWebDb.bindLong(22, census.getValid());
		updateSqlWebDb.bindLong(23, census.getExcluded());

		if (census.getPhoneNumber() == null)
			updateSqlWebDb.bindNull(24);
		else
			updateSqlWebDb.bindString(24, census.getPhoneNumber());

		if (census.getInterviewerName() == null)
			updateSqlWebDb.bindNull(25);
		else
			updateSqlWebDb.bindString(25, census.getInterviewerName());

		if (census.getDeviceId() == null)
			updateSqlWebDb.bindNull(26);
		else
			updateSqlWebDb.bindString(26, census.getDeviceId());

		if (census.getDeviceName() == null)
			updateSqlWebDb.bindNull(27);
		else
			updateSqlWebDb.bindString(27, census.getDeviceName());

		if (census.getDateLastSelected() == null)
			updateSqlWebDb.bindNull(28);
		else
			updateSqlWebDb.bindString(28, census.getDateLastSelected());

		updateSqlWebDb.bindLong(29, census.getSampleFrame());

		if (census.getCreatedDate() == null)
			updateSqlWebDb.bindNull(30);
		else
			updateSqlWebDb.bindString(30, census.getCreatedDate());

		if (census.getUpdatedDate() == null)
			updateSqlWebDb.bindNull(31);
		else
			updateSqlWebDb.bindString(31, census.getUpdatedDate());

		if (census.getFormName() == null)
			updateSqlWebDb.bindNull(32);
		else
			updateSqlWebDb.bindString(32, census.getFormName());

		updateSqlWebDb.bindString(33, census.getInstanceId());
		long result = updateSqlWebDb.executeInsert();

		if (result == -1) { // insert

			insertSqlWebDb.clearBindings();
			insertSqlWebDb.bindString(1, census.getInstanceId());
			insertSqlWebDb.bindNull(2);// insertSqlWebDb.bindString(2,
										// DataTableColumns.DEFAULT_ROW_ETAG);
			insertSqlWebDb.bindString(3, SyncState.new_row.name());
			insertSqlWebDb.bindNull(4);
			insertSqlWebDb.bindString(5, DataTableColumns.DEFAULT_FILTER_TYPE);
			insertSqlWebDb.bindNull(6);// insertSqlWebDb.bindString(6,
										// DataTableColumns.DEFAULT_FILTER_VALUE);
			insertSqlWebDb.bindString(7,
					CensusDatabaseHelper.CENSUS_DATABASES_TABLE);
			insertSqlWebDb.bindString(8, DataTableColumns.DEFAULT_LOCALE);
			insertSqlWebDb.bindString(9, SavepointTypeManipulator.complete());
			insertSqlWebDb.bindString(10, timeStamp);
			insertSqlWebDb.bindString(11,
					DataTableColumns.DEFAULT_SAVEPOINT_CREATOR);

			if (census.getPlaceName() == null)
				insertSqlWebDb.bindNull(12);
			else
				insertSqlWebDb.bindString(12, census.getPlaceName());

			if (census.getHouseNumber() == null)
				insertSqlWebDb.bindNull(13);
			else
				insertSqlWebDb.bindString(13, census.getHouseNumber());

			if (census.getHeadName() == null)
				insertSqlWebDb.bindNull(14);
			else
				insertSqlWebDb.bindString(14, census.getHeadName());

			if (census.getComment() == null)
				insertSqlWebDb.bindNull(15);
			else
				insertSqlWebDb.bindString(15, census.getComment());

			insertSqlWebDb.bindDouble(16, census.getLatitude());
			insertSqlWebDb.bindDouble(17, census.getLongitude());
			insertSqlWebDb.bindDouble(18, census.getAltitude());
			insertSqlWebDb.bindDouble(19, census.getAccuracy());
			insertSqlWebDb.bindLong(20, census.getSelected());
			insertSqlWebDb.bindDouble(21, census.getRandom());
			insertSqlWebDb.bindLong(22, census.getValid());
			insertSqlWebDb.bindLong(23, census.getExcluded());

			if (census.getPhoneNumber() == null)
				insertSqlWebDb.bindNull(24);
			else
				insertSqlWebDb.bindString(24, census.getPhoneNumber());

			if (census.getInterviewerName() == null)
				insertSqlWebDb.bindNull(25);
			else
				insertSqlWebDb.bindString(25, census.getInterviewerName());

			if (census.getDeviceId() == null)
				insertSqlWebDb.bindNull(26);
			else
				insertSqlWebDb.bindString(26, census.getDeviceId());

			if (census.getDeviceName() == null)
				insertSqlWebDb.bindNull(27);
			else
				insertSqlWebDb.bindString(27, census.getDeviceName());

			if (census.getDateLastSelected() == null)
				insertSqlWebDb.bindNull(28);
			else
				insertSqlWebDb.bindString(28, census.getDateLastSelected());

			insertSqlWebDb.bindLong(29, census.getSampleFrame());

			if (census.getCreatedDate() == null)
				insertSqlWebDb.bindNull(30);
			else
				insertSqlWebDb.bindString(30, census.getCreatedDate());

			if (census.getUpdatedDate() == null)
				insertSqlWebDb.bindNull(31);
			else
				insertSqlWebDb.bindString(31, census.getUpdatedDate());

			if (census.getFormName() == null)
				insertSqlWebDb.bindNull(32);
			else
				insertSqlWebDb.bindString(32, census.getFormName());

			insertSqlWebDb.execute();
		}

	}

	public static SQLiteStatement getInsertStatementCensusDb(
			SQLiteDatabase censusDb) {
		String sql = CensusColumns.getInsertSqlCensusDb();
		SQLiteStatement statement = censusDb.compileStatement(sql);
		return statement;
	}

	public static SQLiteStatement getUpdateStatementCensusDb(
			SQLiteDatabase censusDb) {
		String sql = CensusColumns.getUpdateSqlCensusDb();
		SQLiteStatement statement = censusDb.compileStatement(sql);
		return statement;
	}

	public static SQLiteStatement getInsertStatementWebDb(SQLiteDatabase webDb) {
		String sql = CensusColumns.getInsertSqlWebDb();
		SQLiteStatement statement = webDb.compileStatement(sql);
		return statement;
	}

	public static SQLiteStatement getUpdateStatementWebDb(SQLiteDatabase webDb) {
		String sql = CensusColumns.getUpdateSqlWebDb();
		SQLiteStatement statement = webDb.compileStatement(sql);
		return statement;
	}

	private static void updateSelectionInWebDB(SQLiteDatabase db,
			CensusModel census) {
		ContentValues cv = new ContentValues();

		cv.put(CensusColumns.SELECTED, census.getSelected());
		cv.put(CensusColumns.RANDOM, census.getRandom());
		cv.put(CensusColumns.DATE_LAST_SELECTED, census.getDateLastSelected());
		cv.put(CensusColumns.SAMPLE_FRAME, census.getSampleFrame());

		db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
				DataTableColumns.ID + "=?",
				new String[] { census.getInstanceId() });
	}

	public static long updateSelected(SQLiteDatabase db, CensusModel census) {
		long result = 0;
		ContentValues cv = new ContentValues();

		cv.put(CensusColumns.SELECTED, census.getSelected());
		cv.put(CensusColumns.RANDOM, census.getRandom());

		if (census.getInstanceId() != null
				&& census.getInstanceId().length() != 0) {// update
			result = db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
					CensusColumns._ID + "=?",
					new String[] { census.getInstanceId() });

		}

		return result;
	}

	public static long updateSampleFrame(SQLiteDatabase db,
			String dateLastSelected, int sampleFrame) {
		long result = 0;
		ContentValues cv = new ContentValues();

		cv.put(CensusColumns.DATE_LAST_SELECTED, dateLastSelected);
		cv.put(CensusColumns.SAMPLE_FRAME, sampleFrame);

		result = db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
				CensusColumns.EXCLUDED + "=?", new String[] { "0" });

		return result;
	}

	public static boolean syncAllRecordToWebDb(Context context) {
		boolean status = false;
		ArrayList<CensusModel> censuses = CensusUtil.getAll(context);
		SQLiteDatabase db = null;
		try {
			db = DatabaseFactory.get().getDatabase(
					context.getApplicationContext(), "survey");// DatabaseFactory.getInstanceDatabase(context,
																// "survey");
			db.beginTransaction();
			for (CensusModel census : censuses) {
				updateSelectionInWebDB(db, census);
			}
			db.setTransactionSuccessful();
			status = true;
		} finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return status;
	}

	public static int resetRandomAndSelectedValueToZero(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		int numOfUpdates = 0;
		ContentValues cv = new ContentValues();
		cv.put(CensusColumns.RANDOM, 0);
		cv.put(CensusColumns.SELECTED, 0);
		cv.putNull(CensusColumns.DATE_LAST_SELECTED);
		cv.put(CensusColumns.SAMPLE_FRAME, 0);
		numOfUpdates = db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				cv, null, null);
		db.close();
		return numOfUpdates;
	}

	public static boolean exists(Context context, String id) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		String selectString = "SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " WHERE "
				+ CensusColumns._ID + " =? ";
		Cursor cursor = db.rawQuery(selectString, new String[] { id }); // add
																		// the
																		// String
																		// your
																		// searching
																		// by
																		// here

		boolean exists = false;
		if (cursor.moveToFirst()) {
			exists = true;
		}
		cursor.close();
		db.close();
		return exists;
	}

	public static int getCount(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE, null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

	public static int getTotalPointsOtherThanExcluded(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " WHERE "
				+ CensusColumns.EXCLUDED + "=0 ", null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

	public static int getTotalExcluded(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " WHERE "
				+ CensusColumns.EXCLUDED + "=1 ", null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

	public static int getTotalInvalid(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " WHERE "
				+ CensusColumns.EXCLUDED + "=0 AND " + CensusColumns.VALID
				+ " =2", null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

	public static int getTotalValid(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " WHERE "
				+ CensusColumns.EXCLUDED + "=0 AND " + CensusColumns.VALID
				+ " =1", null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

	public static int excludeFromSample(Context context, String instanceId) {
		int affectedRecords = 0;
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");

		ContentValues cv = new ContentValues();
		cv.put(CensusColumns.EXCLUDED, "1");
		affectedRecords = db.update(
				CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
				CensusColumns._ID + "=?", new String[] { instanceId });

		db.close();
		excludeFromSampleWebDb(context, instanceId);

		return affectedRecords;
	}

	public static void excludeFromSampleWebDb(Context context, String instanceId) {
		SQLiteDatabase db = DatabaseFactory.get().getDatabase(
				context.getApplicationContext(), "survey");// DatabaseFactory.getInstanceDatabase(context,
															// "survey");

		ContentValues cv = new ContentValues();
		cv.put(CensusColumns.EXCLUDED, "1");
		db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
				DataTableColumns.ID + "=?", new String[] { instanceId });

		db.close();
	}

	public static void delete(Context context, CensusModel census) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		db.delete(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				CensusColumns._ID + "=?",
				new String[] { String.valueOf(census.getInstanceId()) });
		db.close();
	}

	public static ArrayList<CensusModel> getAll(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		ArrayList<CensusModel> censuses = new ArrayList<CensusModel>();
		Cursor cur = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE, null);
		if (cur.moveToFirst()) {
			do {
				CensusModel census = new CensusModel();
				census.setInstanceId(cur.getString(cur
						.getColumnIndex(CensusColumns._ID)));

				census.setPlaceName(cur.getString(cur
						.getColumnIndex(CensusColumns.PLACE_NAME)));
				census.setHouseNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.HOUSE_NUMBER)));
				census.setHeadName(cur.getString(cur
						.getColumnIndex(CensusColumns.HEAD_NAME)));
				census.setComment(cur.getString(cur
						.getColumnIndex(CensusColumns.COMMENT)));
				census.setLatitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LATITUDE)));
				census.setLongitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LONGITUDE)));
				census.setAltitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ALTITUDE)));
				census.setAccuracy(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ACCURACY)));
				census.setSelected(cur.getInt(cur
						.getColumnIndex(CensusColumns.SELECTED)));
				census.setRandom(cur.getDouble(cur
						.getColumnIndex(CensusColumns.RANDOM)));
				census.setValid(cur.getInt(cur
						.getColumnIndex(CensusColumns.VALID)));
				census.setExcluded(cur.getInt(cur
						.getColumnIndex(CensusColumns.EXCLUDED)));
				census.setProcessed(cur.getInt(cur
						.getColumnIndex(CensusColumns.PROCESSED)));
				census.setPhoneNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.PHONE_NUMBER)));
				census.setInterviewerName(cur.getString(cur
						.getColumnIndex(CensusColumns.INTERVIEWER_NAME)));
				census.setDeviceId(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_ID)));
				census.setDeviceName(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_NAME)));
				census.setDateLastSelected(cur.getString(cur
						.getColumnIndex(CensusColumns.DATE_LAST_SELECTED)));
				census.setSampleFrame(cur.getInt(cur
						.getColumnIndex(CensusColumns.SAMPLE_FRAME)));
				census.setCreatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.CREATED_DATE)));
				census.setUpdatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.UPDATED_DATE)));
				census.setFormName(cur.getString(cur
						.getColumnIndex(CensusColumns.FORM_NAME)));
				censuses.add(census);

			} while (cur.moveToNext());

		}
		cur.close();
		db.close();
		return censuses;
	}

	public static ArrayList<CensusModel> getAllValid(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");

		ArrayList<CensusModel> censuses = new ArrayList<CensusModel>();
		String selection = CensusColumns.VALID + "=? AND "
				+ CensusColumns.EXCLUDED + "=?";
		String[] selectionArgs = new String[] { "1", "0" };

		Cursor cur = db.query(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				null, selection, selectionArgs, null, null, null);

		if (cur.moveToFirst()) {
			do {
				CensusModel census = new CensusModel();
				census.setInstanceId(cur.getString(cur
						.getColumnIndex(CensusColumns._ID)));

				census.setPlaceName(cur.getString(cur
						.getColumnIndex(CensusColumns.PLACE_NAME)));
				census.setHouseNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.HOUSE_NUMBER)));
				census.setHeadName(cur.getString(cur
						.getColumnIndex(CensusColumns.HEAD_NAME)));
				census.setComment(cur.getString(cur
						.getColumnIndex(CensusColumns.COMMENT)));
				census.setLatitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LATITUDE)));
				census.setLongitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LONGITUDE)));
				census.setAltitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ALTITUDE)));
				census.setAccuracy(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ACCURACY)));
				census.setSelected(cur.getInt(cur
						.getColumnIndex(CensusColumns.SELECTED)));
				census.setRandom(cur.getDouble(cur
						.getColumnIndex(CensusColumns.RANDOM)));
				census.setValid(cur.getInt(cur
						.getColumnIndex(CensusColumns.VALID)));
				census.setExcluded(cur.getInt(cur
						.getColumnIndex(CensusColumns.EXCLUDED)));
				census.setProcessed(cur.getInt(cur
						.getColumnIndex(CensusColumns.PROCESSED)));
				census.setPhoneNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.PHONE_NUMBER)));
				census.setInterviewerName(cur.getString(cur
						.getColumnIndex(CensusColumns.INTERVIEWER_NAME)));
				census.setDeviceId(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_ID)));
				census.setDeviceName(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_NAME)));
				census.setDateLastSelected(cur.getString(cur
						.getColumnIndex(CensusColumns.DATE_LAST_SELECTED)));
				census.setSampleFrame(cur.getInt(cur
						.getColumnIndex(CensusColumns.SAMPLE_FRAME)));
				census.setCreatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.CREATED_DATE)));
				census.setUpdatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.UPDATED_DATE)));
				census.setFormName(cur.getString(cur
						.getColumnIndex(CensusColumns.FORM_NAME)));
				censuses.add(census);

			} while (cur.moveToNext());

		}
		cur.close();
		db.close();
		return censuses;
	}

	public static CensusModel getLastRecordHouseNumberPlusOne(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		CensusModel lastRecord = null;
		Cursor cur = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " ORDER BY "
				+ CensusColumns.CREATED_DATE + " DESC", null);
		if (cur.moveToFirst()) {
			lastRecord = new CensusModel();
			lastRecord.setInstanceId(cur.getString(cur
					.getColumnIndex(CensusColumns._ID)));

			lastRecord.setPlaceName(cur.getString(cur
					.getColumnIndex(CensusColumns.PLACE_NAME)));
			lastRecord.setHouseNumber(cur.getString(cur
					.getColumnIndex(CensusColumns.HOUSE_NUMBER)));
			lastRecord.setHeadName(cur.getString(cur
					.getColumnIndex(CensusColumns.HEAD_NAME)));
			lastRecord.setComment(cur.getString(cur
					.getColumnIndex(CensusColumns.COMMENT)));
			lastRecord.setLatitude(cur.getDouble(cur
					.getColumnIndex(CensusColumns.LATITUDE)));
			lastRecord.setLongitude(cur.getDouble(cur
					.getColumnIndex(CensusColumns.LONGITUDE)));
			lastRecord.setAltitude(cur.getDouble(cur
					.getColumnIndex(CensusColumns.ALTITUDE)));
			lastRecord.setAccuracy(cur.getDouble(cur
					.getColumnIndex(CensusColumns.ACCURACY)));
			lastRecord.setSelected(cur.getInt(cur
					.getColumnIndex(CensusColumns.SELECTED)));
			lastRecord.setRandom(cur.getDouble(cur
					.getColumnIndex(CensusColumns.RANDOM)));
			lastRecord.setValid(cur.getInt(cur
					.getColumnIndex(CensusColumns.VALID)));
			lastRecord.setExcluded(cur.getInt(cur
					.getColumnIndex(CensusColumns.EXCLUDED)));
			lastRecord.setProcessed(cur.getInt(cur
					.getColumnIndex(CensusColumns.PROCESSED)));
			lastRecord.setPhoneNumber(cur.getString(cur
					.getColumnIndex(CensusColumns.PHONE_NUMBER)));
			lastRecord.setInterviewerName(cur.getString(cur
					.getColumnIndex(CensusColumns.INTERVIEWER_NAME)));
		}
		cur.close();
		db.close();
		return lastRecord;
	}

	public static boolean isTableExists(SQLiteDatabase db, String tableName) {
		if (tableName == null || db == null || !db.isOpen()) {
			return false;
		}
		Cursor cursor = db
				.rawQuery(
						"SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
						new String[] { "table", tableName });
		if (!cursor.moveToFirst()) {
			return false;
		}
		int count = cursor.getInt(0);
		cursor.close();
		return count > 0;
	}

	// get all ids of all points
	public static ArrayList<String> getAllIDs(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");

		Cursor cursor = db.query(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				new String[] { CensusColumns._ID }, null, null, null, null,
				null);
		ArrayList<String> ids = new ArrayList<String>();

		while (cursor.moveToNext()) {
			ids.add(cursor.getString(cursor.getColumnIndex(CensusColumns._ID)));
		}

		cursor.close();
		db.close();

		return ids;
	}

	public static void clearCensusDb(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		db.delete(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, null, null);
		db.close();
	}

	public static void clearCensusInWebDb(Context context) {
		ArrayList<String> ids = getAllIDs(context);

		SQLiteDatabase db = DatabaseFactory.get().getDatabase(
				context.getApplicationContext(), "survey");// DatabaseFactory.getInstanceDatabase(context,
															// "survey");
		try {
			db.beginTransaction();
			for (String id : ids) {
				db.delete(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
						DataTableColumns.ID + "=?", new String[] { id });
			}
			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}

	}

	public static void clearAll(Context context) {
		clearCensusInWebDb(context);
		clearCensusDb(context);
	}

	public enum FilterCensusList {
		MainPoint, AdditionalPoint, AlternatePoint, ExcludedPoint, PointNotSelected, ProcessedPoint
	}

	public static ArrayList<CensusModel> getCensus(Context context,
			Sort sortBy, String filterPlaceName, FilterCensusList... filter) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");

		ArrayList<CensusModel> censuses = new ArrayList<CensusModel>();
		ArrayList<String> selectionArgs = new ArrayList<String>();
		String selection = "";

		for (int i = 0; i < filter.length; i++) {
			if (filter[i] == FilterCensusList.MainPoint) {
				selection += "(" + CensusColumns.SELECTED + "=? AND "
						+ CensusColumns.PROCESSED + " in(?,?)) OR ";
				selectionArgs.add(String.valueOf(CensusModel.MAIN_POINT));
				selectionArgs.add("0");
				selectionArgs.add("1");
			} else if (filter[i] == FilterCensusList.AdditionalPoint) {
				selection += "(" + CensusColumns.SELECTED + "=? AND "
						+ CensusColumns.PROCESSED + " in(?,?)) OR ";
				selectionArgs.add(String.valueOf(CensusModel.ADDITIONAL_POINT));
				selectionArgs.add("0");
				selectionArgs.add("1");
			} else if (filter[i] == FilterCensusList.AlternatePoint) {
				selection += "(" + CensusColumns.SELECTED + "=? AND "
						+ CensusColumns.PROCESSED + " in(?,?)) OR ";
				selectionArgs.add(String.valueOf(CensusModel.ALTERNATE_POINT));
				selectionArgs.add("0");
				selectionArgs.add("1");
			} else if (filter[i] == FilterCensusList.ExcludedPoint) {
				selection += "(" + CensusColumns.EXCLUDED + "=? AND "
						+ CensusColumns.PROCESSED + " in(?,?)) OR ";
				selectionArgs.add("1");
				selectionArgs.add("0");
				selectionArgs.add("1");
			} else if (filter[i] == FilterCensusList.PointNotSelected) {
				selection += "(" + CensusColumns.SELECTED + "=? AND "
						+ CensusColumns.EXCLUDED + "=? AND "
						+ CensusColumns.PROCESSED + " in (?,?)) OR ";
				selectionArgs.add("0");
				selectionArgs.add("0");
				selectionArgs.add("0");// questionnaire not started yet
				selectionArgs.add("1");// questionnaire started but not
										// finalized
			} else if (filter[i] == FilterCensusList.ProcessedPoint) {
				selection += CensusColumns.PROCESSED + " in(?,?) OR ";
				selectionArgs.add("2");// questionnaire is finalized
				selectionArgs.add("3");// questionnaire is finalized by friends
			}
		}

		selection = selection.substring(0, selection.length() - 3);
		if (filterPlaceName != null && filterPlaceName.length() > 0) {
			selection = "(" + selection + ") AND " + CensusColumns.PLACE_NAME
					+ " =?";
			selectionArgs.add(filterPlaceName);
		}
		String[] strArr = selectionArgs
				.toArray(new String[selectionArgs.size()]);

		String sortByStr = null;
		if (sortBy == Sort.POINT_TYPES) {
			sortByStr = CensusColumns.SELECTED + " DESC";
		} else if (sortBy == Sort.NEWEST_ENTRY) {
			sortByStr = "datetime(" + CensusColumns.CREATED_DATE + ") DESC";
		} else if (sortBy == Sort.OLDEST_ENTRY) {
			sortByStr = "datetime(" + CensusColumns.CREATED_DATE + ") ASC";
		} else if (sortBy == Sort.ENUMERATOR_NAME) {
			sortByStr = CensusColumns.INTERVIEWER_NAME + " ASC";
		} else if (sortBy == Sort.DEVICE_ID) {
			sortByStr = CensusColumns.DEVICE_ID + " ASC";
		}

		Cursor cur = db.query(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				null, selection, strArr, null, null, sortByStr);

		if (cur.moveToFirst()) {
			do {
				CensusModel census = new CensusModel();
				census.setInstanceId(cur.getString(cur
						.getColumnIndex(CensusColumns._ID)));

				census.setPlaceName(cur.getString(cur
						.getColumnIndex(CensusColumns.PLACE_NAME)));
				census.setHouseNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.HOUSE_NUMBER)));
				census.setHeadName(cur.getString(cur
						.getColumnIndex(CensusColumns.HEAD_NAME)));
				census.setComment(cur.getString(cur
						.getColumnIndex(CensusColumns.COMMENT)));
				census.setLatitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LATITUDE)));
				census.setLongitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LONGITUDE)));
				census.setAltitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ALTITUDE)));
				census.setAccuracy(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ACCURACY)));
				census.setSelected(cur.getInt(cur
						.getColumnIndex(CensusColumns.SELECTED)));
				census.setRandom(cur.getDouble(cur
						.getColumnIndex(CensusColumns.RANDOM)));
				census.setValid(cur.getInt(cur
						.getColumnIndex(CensusColumns.VALID)));
				census.setExcluded(cur.getInt(cur
						.getColumnIndex(CensusColumns.EXCLUDED)));
				census.setProcessed(cur.getInt(cur
						.getColumnIndex(CensusColumns.PROCESSED)));
				census.setPhoneNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.PHONE_NUMBER)));
				census.setInterviewerName(cur.getString(cur
						.getColumnIndex(CensusColumns.INTERVIEWER_NAME)));
				census.setDeviceId(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_ID)));
				census.setDeviceName(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_NAME)));
				census.setDateLastSelected(cur.getString(cur
						.getColumnIndex(CensusColumns.DATE_LAST_SELECTED)));
				census.setSampleFrame(cur.getInt(cur
						.getColumnIndex(CensusColumns.SAMPLE_FRAME)));
				census.setCreatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.CREATED_DATE)));
				census.setUpdatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.UPDATED_DATE)));
				census.setFormName(cur.getString(cur
						.getColumnIndex(CensusColumns.FORM_NAME)));
				censuses.add(census);

			} while (cur.moveToNext());

		}
		cur.close();
		db.close();

		return censuses;
	}

	public static int isQuestionnaireFinalized(Context context, String tableId,
			String instanceId) {
		int result = 0;// questionnaire not yet started
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = DatabaseFactory.get().getDatabase(
					context.getApplicationContext(), "survey");

			String selectString = "SELECT * FROM " + tableId + " WHERE "
					+ DataTableColumns.ID + " =? ";
			cursor = db.rawQuery(selectString, new String[] { instanceId });

			if (cursor.moveToFirst()) {
				result = 1;// questionnaire started but might not be finalized
				if (cursor.getString(cursor
						.getColumnIndex(DataTableColumns.SAVEPOINT_TYPE)) != null
						&& cursor
								.getString(
										cursor.getColumnIndex(DataTableColumns.SAVEPOINT_TYPE))
								.equals(InstanceColumns.STATUS_COMPLETE)) {
					result = 2;// questionnaire finalized
				}
			}
		} catch (Exception ex) {

		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();
		}

		return result;
	}

	public static int updateCensusQuestionnaireStatus(Context context,
			String instanceId, int result) {
		int affectedRecords = 0;
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");

		ContentValues cv = new ContentValues();
		if (result > 2)
			result = 1;
		cv.put(CensusColumns.PROCESSED, result);
		affectedRecords = db.update(
				CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
				CensusColumns._ID + "=?", new String[] { instanceId });

		db.close();

		return affectedRecords;
	}

	public static int invalidateCensus(Context context, String instanceId) {
		int affectedRecords = 0;
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");

		ContentValues cv = new ContentValues();
		cv.put(CensusColumns.VALID, 2);
		affectedRecords = db.update(
				CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
				CensusColumns._ID + "=?", new String[] { instanceId });

		db.close();

		return affectedRecords;
	}

	public static String getCensusEA(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		String ea = "";
		Cursor cur = db.rawQuery("SELECT MAX(occurrence) AS occurrence, "
				+ CensusColumns.PLACE_NAME + " FROM (SELECT COUNT("
				+ CensusColumns._ID + ") AS occurrence, "
				+ CensusColumns.PLACE_NAME + " FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + " GROUP BY "
				+ CensusColumns.PLACE_NAME + ") ", null);
		if (cur.moveToFirst()) {
			ea = cur.getString(cur.getColumnIndex(CensusColumns.PLACE_NAME));
		}
		cur.close();
		db.close();

		return ea;
	}

	public static CensusModel getCensusFromWebDb(Context context,
			String instanceId) {
		SQLiteDatabase db = null;
		Cursor cur = null;
		CensusModel census = null;
		try {
			db = DatabaseFactory.get().getDatabase(
					context.getApplicationContext(), "survey");

			String selectString = "SELECT * FROM "
					+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE
					+ " AS T WHERE " + DataTableColumns.ID + " =? AND T."
					+ DataTableColumns.SAVEPOINT_TIMESTAMP
					+ " = (SELECT MAX(V."
					+ DataTableColumns.SAVEPOINT_TIMESTAMP + ") FROM "
					+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE
					+ " AS V WHERE V." + DataTableColumns.ID + " = T."
					+ DataTableColumns.ID + ")";

			cur = db.rawQuery(selectString, new String[] { instanceId });

			if (cur.moveToFirst()) {
				census = new CensusModel();
				census.setInstanceId(cur.getString(cur
						.getColumnIndex(DataTableColumns.ID)));

				census.setPlaceName(cur.getString(cur
						.getColumnIndex(CensusColumns.PLACE_NAME)));
				census.setHouseNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.HOUSE_NUMBER)));
				census.setHeadName(cur.getString(cur
						.getColumnIndex(CensusColumns.HEAD_NAME)));
				census.setComment(cur.getString(cur
						.getColumnIndex(CensusColumns.COMMENT)));
				census.setLatitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.FDN + "_"
								+ CensusColumns.LATITUDE)));
				census.setLongitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.FDN + "_"
								+ CensusColumns.LONGITUDE)));
				census.setAltitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.FDN + "_"
								+ CensusColumns.ALTITUDE)));
				census.setAccuracy(cur.getDouble(cur
						.getColumnIndex(CensusColumns.FDN + "_"
								+ CensusColumns.ACCURACY)));
				census.setSelected(cur.getInt(cur
						.getColumnIndex(CensusColumns.SELECTED)));
				census.setRandom(cur.getDouble(cur
						.getColumnIndex(CensusColumns.RANDOM)));
				census.setValid(cur.getInt(cur
						.getColumnIndex(CensusColumns.VALID)));
				census.setExcluded(cur.getInt(cur
						.getColumnIndex(CensusColumns.EXCLUDED)));
				census.setPhoneNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.PHONE_NUMBER)));
				census.setInterviewerName(cur.getString(cur
						.getColumnIndex(CensusColumns.INTERVIEWER_NAME)));
				census.setDeviceId(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_ID)));
				census.setDeviceName(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_NAME)));
				census.setDateLastSelected(cur.getString(cur
						.getColumnIndex(CensusColumns.DATE_LAST_SELECTED)));
				census.setSampleFrame(cur.getInt(cur
						.getColumnIndex(CensusColumns.SAMPLE_FRAME)));
				census.setCreatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.CREATED_DATE)));
				census.setUpdatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.UPDATED_DATE)));
				census.setFormName(cur.getString(cur
						.getColumnIndex(CensusColumns.FORM_NAME)));
			}
		} catch (Exception ex) {

		} finally {
			if (cur != null)
				cur.close();
			if (db != null)
				db.close();
		}

		return census;
	}

	public static ArrayList<CensusModel> getAllFromBackupDb(Context context,
			String path) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getBackupDatabase(
				context, "survey", path);
		ArrayList<CensusModel> censuses = new ArrayList<CensusModel>();
		Cursor cur = db.rawQuery("SELECT * FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE, null);
		if (cur.moveToFirst()) {
			do {
				CensusModel census = new CensusModel();
				census.setInstanceId(cur.getString(cur
						.getColumnIndex(CensusColumns._ID)));

				census.setPlaceName(cur.getString(cur
						.getColumnIndex(CensusColumns.PLACE_NAME)));
				census.setHouseNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.HOUSE_NUMBER)));
				census.setHeadName(cur.getString(cur
						.getColumnIndex(CensusColumns.HEAD_NAME)));
				census.setComment(cur.getString(cur
						.getColumnIndex(CensusColumns.COMMENT)));
				census.setLatitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LATITUDE)));
				census.setLongitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.LONGITUDE)));
				census.setAltitude(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ALTITUDE)));
				census.setAccuracy(cur.getDouble(cur
						.getColumnIndex(CensusColumns.ACCURACY)));
				census.setSelected(cur.getInt(cur
						.getColumnIndex(CensusColumns.SELECTED)));
				census.setRandom(cur.getDouble(cur
						.getColumnIndex(CensusColumns.RANDOM)));
				census.setValid(cur.getInt(cur
						.getColumnIndex(CensusColumns.VALID)));
				census.setExcluded(cur.getInt(cur
						.getColumnIndex(CensusColumns.EXCLUDED)));
				census.setProcessed(cur.getInt(cur
						.getColumnIndex(CensusColumns.PROCESSED)));
				census.setPhoneNumber(cur.getString(cur
						.getColumnIndex(CensusColumns.PHONE_NUMBER)));
				census.setInterviewerName(cur.getString(cur
						.getColumnIndex(CensusColumns.INTERVIEWER_NAME)));
				census.setDeviceId(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_ID)));
				census.setDeviceName(cur.getString(cur
						.getColumnIndex(CensusColumns.DEVICE_NAME)));
				census.setDateLastSelected(cur.getString(cur
						.getColumnIndex(CensusColumns.DATE_LAST_SELECTED)));
				census.setSampleFrame(cur.getInt(cur
						.getColumnIndex(CensusColumns.SAMPLE_FRAME)));
				census.setCreatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.CREATED_DATE)));
				census.setUpdatedDate(cur.getString(cur
						.getColumnIndex(CensusColumns.UPDATED_DATE)));
				census.setFormName(cur.getString(cur
						.getColumnIndex(CensusColumns.FORM_NAME)));
				censuses.add(census);

			} while (cur.moveToNext());

		}
		cur.close();
		db.close();
		return censuses;
	}

	public static ArrayList<String> getAllCensusCreatedDate(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		ArrayList<String> result = new ArrayList<String>();
		Cursor cur = db.rawQuery("SELECT DISTINCT strftime( '%d-%m-%Y',"
				+ CensusColumns.CREATED_DATE + ") FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE
				+ " ORDER BY strftime( '%d-%m-%Y',"
				+ CensusColumns.CREATED_DATE + " ) DESC", null);
		if (cur.moveToFirst()) {
			do {
				result.add(cur.getString(0));
			} while (cur.moveToNext());
		}
		cur.close();
		db.close();

		return result;
	}

	public static int delete(Context context, String date) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		int rowsAffected = 0;
		rowsAffected = db.delete(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				"strftime( '%d-%m-%Y',createdDate) = ?", new String[] { date });
		db.close();
		return rowsAffected;
	}

	public static int delete(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		int rowsAffected = 0;
		rowsAffected = db.delete(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
				null, null);
		db.close();
		return rowsAffected;
	}

	public static int deleteCensusFromPreviousDate(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		int rowsAffected = 0;
		rowsAffected = db
				.delete(CensusDatabaseHelper.CENSUS_DATABASES_TABLE,
						"strftime( '%d-%m-%Y',createdDate) < strftime( '%d-%m-%Y','now')",
						null);
		db.close();
		return rowsAffected;
	}

	public static int getCountOfCensusFromPreviousDate(Context context) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		int count = 0;
		Cursor cur = db.rawQuery("SELECT DISTINCT strftime( '%d-%m-%Y',"
				+ CensusColumns.CREATED_DATE + ") FROM "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE
				+ " WHERE strftime( '%d-%m-%Y'," + CensusColumns.CREATED_DATE
				+ ") < strftime( '%d-%m-%Y','now')", null);
		count = cur.getCount();
		cur.close();
		db.close();

		return count;
	}

	public static int markAsFinalizedByFriends(Context context,
			CensusModel census) {
		SQLiteDatabase db = CensusDatabaseFactory.get().getDatabase(context,
				"survey");
		int result = 0;
		ContentValues cv = new ContentValues();

		cv.put(CensusColumns.PROCESSED, 3);

		if (census.getInstanceId() != null
				&& census.getInstanceId().length() != 0) {// update
			result = db.update(CensusDatabaseHelper.CENSUS_DATABASES_TABLE, cv,
					CensusColumns._ID + "=?",
					new String[] { census.getInstanceId() });

		}

		db.close();

		return result;
	}
}
