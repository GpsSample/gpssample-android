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

import android.provider.BaseColumns;

/*
 * @author: belendia@gmail.com
 */

public class PlaceColumns implements BaseColumns {

	public static final String _ID = "_id";
	public static final String HIERARCHY_NAME = "hierarchyName";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String RELATIONSHIP = "relationship";
	public static final String CREATED_DATE = "createdDate";

	// This class cannot be instantiated
	private PlaceColumns() {
	}

	public static String getTableCreateSql(String tableName) {

		String create = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + _ID
				+ " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ HIERARCHY_NAME + " TEXT NOT NULL, " + CODE
				+ " TEXT NOT NULL, " + NAME + "  TEXT NULL, " + RELATIONSHIP
				+ " TEXT NULL, " + CREATED_DATE + " TEXT NULL)";

		return create;
	}

}
