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
import java.util.Collections;
import java.util.List;

import org.path.common.android.data.PlaceModel;
import org.path.common.android.database.PlaceNameDatabaseFactory;
import org.path.common.android.database.PlaceNameDatabaseHelper;
import org.path.common.android.provider.PlaceColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

/*
 * @author: belendia@gmail.com
 */

public class PlaceNameUtil {

	protected PlaceNameUtil() {
	}

	public static void insertPlace(SQLiteDatabase db, PlaceModel place) {
		ContentValues values = new ContentValues();
		values.put(PlaceColumns.HIERARCHY_NAME, place.getHierarchyName());
		values.put(PlaceColumns.CODE, place.getCode());
		values.put(PlaceColumns.NAME, place.getName());
		values.put(PlaceColumns.RELATIONSHIP, place.getRelationship());
		db.insert(PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE, null, values);
	}

	public static void delete(SQLiteDatabase db) {
		db.delete(PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE, null, null);
	}

	// Returns the number of database records.
	public static long getCount(Context context) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(context,
				"survey");
		long result = DatabaseUtils.queryNumEntries(db,
				PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE);
		db.close();

		return result;
	}

	// get all places from the database based on the parameters provided
	public static List<PlaceModel> getAll(Context context, String hierarchy,
			String relationship) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(context,
				"survey");

		String selection = PlaceColumns.HIERARCHY_NAME + " = ?";
		String[] selectionArgs = new String[] { hierarchy };
		if (relationship != null) {
			selection += " AND " + PlaceColumns.RELATIONSHIP + " = ?";
			selectionArgs = new String[] { hierarchy, relationship };
		}

		Cursor cursor = db.query(PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE,
				null, selection, selectionArgs, null, null, null);

		ArrayList<PlaceModel> places = new ArrayList<PlaceModel>();

		PlaceModel place = new PlaceModel();
		place.setId(-1);
		place.setName("");
		places.add(place);

		while (cursor.moveToNext()) {
			place = new PlaceModel();
			place.setId(cursor.getInt(cursor.getColumnIndex(PlaceColumns._ID)));
			place.setHierarchyName(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.HIERARCHY_NAME)));
			place.setCode(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.CODE)));
			place.setName(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.NAME)));
			place.setRelationship(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.RELATIONSHIP)));
			places.add(place);
		}
		cursor.close();
		db.close();
		return places;
	}

	// get name of a place from the database given its code
	public static String getName(Context context, String hierarchy, String code) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(context,
				"survey");

		String selection = PlaceColumns.CODE + " = ? AND "
				+ PlaceColumns.HIERARCHY_NAME + " = ?";
		String[] selectionArgs = new String[] { code, hierarchy };

		Cursor cursor = db.query(PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE,
				new String[] { PlaceColumns.NAME }, selection, selectionArgs,
				null, null, null);

		String name = "";

		if (cursor.moveToNext()) {
			name = cursor.getString(cursor.getColumnIndex(PlaceColumns.NAME));
		}
		cursor.close();
		db.close();
		return name;
	}

	private enum Type {
		Code, Name
	};

	// get place name detail given its name or code and hierarchy
	private static PlaceModel getPlaceNameDetail(Context context,
			String hierarchy, String codeOrName, Type type) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(context,
				"survey");

		String selection = "";
		if (type == Type.Name) {
			selection = PlaceColumns.NAME + " = ? AND "
					+ PlaceColumns.HIERARCHY_NAME + " = ?";
		} else {
			selection = PlaceColumns.CODE + " = ? AND "
					+ PlaceColumns.HIERARCHY_NAME + " = ?";
		}
		String[] selectionArgs = new String[] { codeOrName, hierarchy };

		Cursor cursor = db.query(PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE,
				null, selection, selectionArgs, null, null, null);

		PlaceModel result = null;

		if (cursor.moveToNext()) {
			result = new PlaceModel();
			result.setHierarchyName(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.HIERARCHY_NAME)));
			result.setName(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.NAME)));
			result.setCode(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.CODE)));
			result.setRelationship(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.RELATIONSHIP)));
		}
		cursor.close();
		db.close();
		return result;
	}

	// get all distinct hierarchy from the database
	public static ArrayList<String> getAllHierarchies(Context context) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(context,
				"survey");

		Cursor cursor = db.rawQuery("SELECT distinct "
				+ PlaceColumns.HIERARCHY_NAME + " FROM "
				+ PlaceNameDatabaseHelper.PLACE_DATABASES_TABLE, null);
		ArrayList<String> hierarchy = new ArrayList<String>();

		while (cursor.moveToNext()) {
			hierarchy.add(cursor.getString(cursor
					.getColumnIndex(PlaceColumns.HIERARCHY_NAME)));
		}

		cursor.close();
		db.close();

		return hierarchy;
	}

	public static enum HirarchyOrder {
		HigherToLower, LowerToHigher
	};

	public static ArrayList<PlaceModel> getHierarchiesDetail(Context context,
			String placeName, HirarchyOrder order) {
		ArrayList<String> hirarchyName = PlaceNameUtil
				.getAllHierarchies(context);
		ArrayList<PlaceModel> result = null;
		if (hirarchyName.size() > 0) {
			PlaceModel p = PlaceNameUtil.getPlaceNameDetail(context,
					hirarchyName.get(hirarchyName.size() - 1), placeName,
					Type.Code);
			if (p != null) {
				result = new ArrayList<PlaceModel>();
				result.add(p);
				for (int i = hirarchyName.size() - 2; i >= 0; i--) {
					p = PlaceNameUtil
							.getPlaceNameDetail(context, hirarchyName.get(i),
									p.getRelationship(), Type.Code);
					result.add(p);
				}
			}
		}

		/*
		 * result is accumulated from lower hierarchy to higher e.g. EA,
		 * Chiefdom, District, Province reverse the result to start setting
		 * place name spinner from the higher hierarchy.
		 */
		if (order == HirarchyOrder.HigherToLower) {
			if (result != null) {
				Collections.reverse(result);
			}
		}

		return result;
	}
}
