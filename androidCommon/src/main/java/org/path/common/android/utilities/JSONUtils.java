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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.path.common.android.data.CensusModel;
import org.path.common.android.data.DeviceInfo;

import android.util.Log;

/*
 * @author belendia@gmail.com
 * 
 */

public class JSONUtils {
	public static final String t = "JSONUtils";
	public static final String DATA_COMMAND = "data";
	public static final String PULL_COMMAND = "pull";
	public static final String SERVER_INFO_COMMAND = "server_info";
	public static final String CLIENT_INFO_COMMAND = "client_info";
	public static final String START_DATA_TRANSFER_COMMAND = "start";
	public static final String FINISHED_DATA_TRANSFER_COMMAND = "finished";

	private static final String COMMAND = "command";
	private static final String CENSUS_OBJECT = "census";
	private static final String DEVICE_INFO_OBJECT = "deviceInfo";
	private static final String TOTAL = "total";
	private static final String END = "end";
	private static final String START = "start";

	/**
	 * convert json string to json object
	 */
	public static JSONObject getJsonObject(String jsonstr) {
		JSONObject jsonobj = null;
		try {
			jsonobj = new JSONObject(jsonstr);
		} catch (JSONException e) {
			Log.e(t, "getJsonObject : " + e.toString());
		}
		return jsonobj;
	}

	public static JSONObject prepareCensusJSONData(
			ArrayList<CensusModel> censuses, int total, int start, int end) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND, DATA_COMMAND);
			jsonObj.put(START, start);
			jsonObj.put(END, end);
			jsonObj.put(TOTAL, total);

			JSONArray ja = new JSONArray();

			for (CensusModel census : censuses) {
				ja.put(CensusModel.getAsJSONObject(census));
			}
			jsonObj.put(CENSUS_OBJECT, ja);
		} catch (JSONException e) {
			Log.e(t, "prepareCensusJSONData : " + e.toString());
		}

		return jsonObj;
	}

	public static ArrayList<CensusModel> getCensusesFromString(String data) {
		ArrayList<CensusModel> result = new ArrayList<CensusModel>();
		try {
			JSONObject censusObj = new JSONObject(data);

			JSONArray censusArr = (JSONArray) censusObj.get(CENSUS_OBJECT);
			for (int i = 0; i < censusArr.length(); i++) {
				JSONObject jsonobject = censusArr.getJSONObject(i);
				CensusModel census = CensusModel.parseCensusRow(jsonobject);
				result.add(census);
			}
		} catch (JSONException e) {
			Log.e(t, "getCensusesFromString : " + e.toString());
		}

		return result;
	}

	public static JSONObject prepareDeviceInfoJSONData(JSONObject deviceInfo,
			String command) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND, command);
			jsonObj.put(DEVICE_INFO_OBJECT, deviceInfo);
		} catch (JSONException e) {
			Log.e(t, "prepareDeviceInfoJSONData : " + e.toString());
		}

		return jsonObj;
	}

	public static String getCommandFromJSON(String data) {
		String command = null;
		try {
			JSONObject obj = (new JSONObject(data));
			command = obj.getString(COMMAND);
		} catch (JSONException e) {
			Log.e(t, "getCommandFromJSON : " + e.toString());
		}

		return command;
	}

	public static CensusModel getCensusFromJSON(String data) {
		CensusModel census = null;
		try {
			JSONObject censusObj = (new JSONObject(data))
					.getJSONObject(CENSUS_OBJECT);
			census = CensusModel.parseCensusRow(censusObj);
		} catch (JSONException e) {
			Log.e(t, "getCommandFromJSON : " + e.toString());
		}

		return census;
	}

	public static DeviceInfo getDeviceInfoFromJSON(String data) {
		DeviceInfo info = null;
		try {
			JSONObject infoObj = (new JSONObject(data))
					.getJSONObject(DEVICE_INFO_OBJECT);
			info = DeviceInfo.parseDeviceInfo(infoObj);
		} catch (JSONException e) {
			Log.e(t, "getDeviceInfoFromJSON : " + e.toString());
		}

		return info;
	}

	public static JSONObject prepareCommand(String command) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put(COMMAND, command);
		} catch (JSONException e) {
			Log.e(t, "preparePullCommand : " + e.toString());
		}

		return jsonObj;
	}
}
