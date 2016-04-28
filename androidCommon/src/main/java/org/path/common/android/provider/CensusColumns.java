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

package org.path.common.android.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.path.common.android.database.CensusDatabaseHelper;

import android.provider.BaseColumns;

/*
 * @author: belendia@gmail.com
 */

public class CensusColumns implements BaseColumns {

	public static final String _ID = "instanceId";

	public static final String PLACE_NAME = "placeName";
	public static final String HOUSE_NUMBER = "houseNumber";
	public static final String HEAD_NAME = "headName";
	public static final String COMMENT = "comment";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ALTITUDE = "altitude";
	public static final String ACCURACY = "accuracy";
	public static final String SELECTED = "selected";
	public static final String RANDOM = "random";
	public static final String VALID = "valid";
	public static final String EXCLUDED = "excluded";
	public static final String PROCESSED = "processed";
	public static final String PHONE_NUMBER = "phoneNumber";
	public static final String INTERVIEWER_NAME = "interviewerName";
	public static final String DEVICE_ID = "deviceId";
	public static final String DEVICE_NAME = "deviceName";
	public static final String DATE_LAST_SELECTED = "dateLastSelected";
	public static final String SAMPLE_FRAME = "sampleFrame";
	public static final String CREATED_DATE = "createdDate";
	public static final String UPDATED_DATE = "updatedDate";
	public static final String FORM_NAME = "formName";

	// This class cannot be instantiated
	private CensusColumns() {
	}

	public static String getTableCreateSql(String tableName) {

		String create = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + _ID
				+ " TEXT NOT NULL PRIMARY KEY, " + PLACE_NAME + " TEXT NULL,"
				+ HOUSE_NUMBER + " TEXT NULL," + HEAD_NAME + " TEXT NULL,"
				+ COMMENT + " TEXT NULL," + LATITUDE + " REAL NULL,"
				+ LONGITUDE + " REAL NULL," + ALTITUDE + " REAL NULL,"
				+ ACCURACY + " REAL NULL," + SELECTED + " INTEGER NULL,"
				+ RANDOM + " REAL NULL," + VALID + " INTEGER NULL," + EXCLUDED
				+ " INTEGER NULL," + PROCESSED + " INTEGER NULL,"
				+ PHONE_NUMBER + " TEXT NULL," + INTERVIEWER_NAME
				+ " TEXT NULL," + DEVICE_ID + " TEXT NULL, " + DEVICE_NAME
				+ " TEXT NULL, " + DATE_LAST_SELECTED + " TEXT NULL, "
				+ SAMPLE_FRAME + " INTEGER NULL, " + CREATED_DATE
				+ " TEXT NULL, " + UPDATED_DATE + " TEXT NULL, " + FORM_NAME
				+ " TEXT NULL )";

		return create;
	}

	public static String FDN = "location";
	private static String LOCATION_CHILD_ELEMENT_KEY = "[\"" + FDN + "_"
			+ CensusColumns.LATITUDE + "\",\"" + FDN + "_"
			+ CensusColumns.LONGITUDE + "\",\"" + FDN + "_"
			+ CensusColumns.ALTITUDE + "\",\"" + FDN + "_"
			+ CensusColumns.ACCURACY + "\" ]";

	public static List<Column> USER_DEFINED_COLUMNS;
	static {
		ArrayList<Column> columns = new ArrayList<Column>();
		columns.add(new Column(CensusColumns.PLACE_NAME,
				CensusColumns.PLACE_NAME, "string", "[]"));
		columns.add(new Column(CensusColumns.HOUSE_NUMBER,
				CensusColumns.HOUSE_NUMBER, "string", "[]"));
		columns.add(new Column(CensusColumns.HEAD_NAME,
				CensusColumns.HEAD_NAME, "string", "[]"));
		columns.add(new Column(CensusColumns.COMMENT, CensusColumns.COMMENT,
				"string", "[]"));
		columns.add(new Column(FDN, FDN, "geopoint", LOCATION_CHILD_ELEMENT_KEY));
		columns.add(new Column(FDN + "_" + CensusColumns.LATITUDE,
				CensusColumns.LATITUDE, "number", "[]"));
		columns.add(new Column(FDN + "_" + CensusColumns.LONGITUDE,
				CensusColumns.LONGITUDE, "number", "[]"));
		columns.add(new Column(FDN + "_" + CensusColumns.ALTITUDE,
				CensusColumns.ALTITUDE, "number", "[]"));
		columns.add(new Column(FDN + "_" + CensusColumns.ACCURACY,
				CensusColumns.ACCURACY, "number", "[]"));
		columns.add(new Column(CensusColumns.SELECTED, CensusColumns.SELECTED,
				"integer", "[]"));
		columns.add(new Column(CensusColumns.RANDOM, CensusColumns.RANDOM,
				"decimal", "[]"));
		columns.add(new Column(CensusColumns.VALID, CensusColumns.VALID,
				"integer", "[]"));
		columns.add(new Column(CensusColumns.EXCLUDED, CensusColumns.EXCLUDED,
				"integer", "[]"));
		columns.add(new Column(CensusColumns.PHONE_NUMBER,
				CensusColumns.PHONE_NUMBER, "string", "[]"));
		columns.add(new Column(CensusColumns.INTERVIEWER_NAME,
				CensusColumns.INTERVIEWER_NAME, "string", "[]"));
		columns.add(new Column(CensusColumns.DEVICE_ID,
				CensusColumns.DEVICE_ID, "string", "[]"));
		columns.add(new Column(CensusColumns.DEVICE_NAME,
				CensusColumns.DEVICE_NAME, "string", "[]"));
		columns.add(new Column(CensusColumns.DATE_LAST_SELECTED,
				CensusColumns.DATE_LAST_SELECTED, "string", "[]"));
		columns.add(new Column(CensusColumns.SAMPLE_FRAME,
				CensusColumns.SAMPLE_FRAME, "integer", "[]"));
		columns.add(new Column(CensusColumns.CREATED_DATE,
				CensusColumns.CREATED_DATE, "string", "[]"));
		columns.add(new Column(CensusColumns.UPDATED_DATE,
				CensusColumns.UPDATED_DATE, "string", "[]"));
		columns.add(new Column(CensusColumns.FORM_NAME,
				CensusColumns.FORM_NAME, "string", "[]"));
		USER_DEFINED_COLUMNS = Collections.unmodifiableList(columns);
	}

	public static String getInsertSqlCensusDb() {

		String insert = "INSERT INTO "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE + "(" + _ID
				+ ", " + PLACE_NAME + ", " + HOUSE_NUMBER + ", " + HEAD_NAME
				+ ", " + COMMENT + ", " + LATITUDE + ", " + LONGITUDE + ", "
				+ ALTITUDE + ", " + ACCURACY + ", " + SELECTED + ", " + RANDOM
				+ ", " + VALID + ", " + EXCLUDED + ", " + PROCESSED + ", "
				+ PHONE_NUMBER + ", " + INTERVIEWER_NAME + ", " + DEVICE_ID
				+ ", " + DEVICE_NAME + ", " + DATE_LAST_SELECTED + ", "
				+ SAMPLE_FRAME + ", " + CREATED_DATE + ", " + UPDATED_DATE
				+ ", " + FORM_NAME
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		return insert;
	}

	public static String getUpdateSqlCensusDb() {

		String update = "UPDATE " + CensusDatabaseHelper.CENSUS_DATABASES_TABLE
				+ " SET " + _ID + "=?, " + PLACE_NAME + "=?, " + HOUSE_NUMBER
				+ "=?, " + HEAD_NAME + "=?, " + COMMENT + "=?, " + LATITUDE
				+ "=?, " + LONGITUDE + "=?, " + ALTITUDE + "=?, " + ACCURACY
				+ "=?, " + SELECTED + "=?, " + RANDOM + "=?, " + VALID + "=?, "
				+ EXCLUDED + "=?, " + PROCESSED + "=?, " + PHONE_NUMBER
				+ "=?, " + INTERVIEWER_NAME + "=?, " + DEVICE_ID + "=?, "
				+ DEVICE_NAME + "=?, " + DATE_LAST_SELECTED + "=?, "
				+ SAMPLE_FRAME + "=?, " + CREATED_DATE + "=?, " + UPDATED_DATE
				+ "=?, " + FORM_NAME + "=?" + " WHERE " + _ID + "=?";

		return update;
	}

	public static String getInsertSqlWebDb() {

		String insert = "INSERT INTO "
				+ CensusDatabaseHelper.CENSUS_DATABASES_TABLE
				+ "("
				+ DataTableColumns.ID
				+ ", "
				+ DataTableColumns.ROW_ETAG
				+ ", "
				+ DataTableColumns.SYNC_STATE
				+ ", "
				+ DataTableColumns.CONFLICT_TYPE
				+ ", "
				+ DataTableColumns.FILTER_TYPE
				+ ", "
				+ DataTableColumns.FILTER_VALUE
				+ ", "
				+ DataTableColumns.FORM_ID
				+ ", "
				+ DataTableColumns.LOCALE
				+ ", "
				+ DataTableColumns.SAVEPOINT_TYPE
				+ ", "
				+ DataTableColumns.SAVEPOINT_TIMESTAMP
				+ ", "
				+ DataTableColumns.SAVEPOINT_CREATOR
				+ ", "
				+ PLACE_NAME
				+ ", "
				+ HOUSE_NUMBER
				+ ", "
				+ HEAD_NAME
				+ ", "
				+ COMMENT
				+ ", "
				+ FDN
				+ "_"
				+ LATITUDE
				+ ", "
				+ FDN
				+ "_"
				+ LONGITUDE
				+ ", "
				+ FDN
				+ "_"
				+ ALTITUDE
				+ ", "
				+ FDN
				+ "_"
				+ ACCURACY
				+ ", "
				+ SELECTED
				+ ", "
				+ RANDOM
				+ ", "
				+ VALID
				+ ", "
				+ EXCLUDED
				+ ", "
				+ PHONE_NUMBER
				+ ", "
				+ INTERVIEWER_NAME
				+ ", "
				+ DEVICE_ID
				+ ", "
				+ DEVICE_NAME
				+ ", "
				+ DATE_LAST_SELECTED
				+ ", "
				+ SAMPLE_FRAME
				+ ", "
				+ CREATED_DATE
				+ ", "
				+ UPDATED_DATE
				+ ", "
				+ FORM_NAME
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		return insert;
	}

	public static String getUpdateSqlWebDb() {

		String update = "UPDATE " + CensusDatabaseHelper.CENSUS_DATABASES_TABLE
				+ " SET " + DataTableColumns.ID + "=?, "
				+ DataTableColumns.ROW_ETAG + "=?, "
				+ DataTableColumns.SYNC_STATE + "=?, "
				+ DataTableColumns.CONFLICT_TYPE + "=?, "
				+ DataTableColumns.FILTER_TYPE + "=?, "
				+ DataTableColumns.FILTER_VALUE + "=?, "
				+ DataTableColumns.FORM_ID + "=?, " + DataTableColumns.LOCALE
				+ "=?, " + DataTableColumns.SAVEPOINT_TYPE + "=?, "
				+ DataTableColumns.SAVEPOINT_TIMESTAMP + "=?, "
				+ DataTableColumns.SAVEPOINT_CREATOR + "=?, " + PLACE_NAME
				+ "=?, " + HOUSE_NUMBER + "=?, " + HEAD_NAME + "=?, " + COMMENT
				+ "=?, " + FDN + "_" + LATITUDE + "=?, " + FDN + "_"
				+ LONGITUDE + "=?, " + FDN + "_" + ALTITUDE + "=?, " + FDN
				+ "_" + ACCURACY + "=?, " + SELECTED + "=?, " + RANDOM + "=?, "
				+ VALID + "=?, " + EXCLUDED + "=?, " + PHONE_NUMBER + "=?, "
				+ INTERVIEWER_NAME + "=?, " + DEVICE_ID + "=?, " + DEVICE_NAME
				+ "=?, " + DATE_LAST_SELECTED + "=?, " + SAMPLE_FRAME + "=?, "
				+ CREATED_DATE + "=?, " + UPDATED_DATE + "=?, " + FORM_NAME
				+ "=?" + " WHERE " + DataTableColumns.ID + "=?";

		return update;
	}
}
