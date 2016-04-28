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

package org.path.common.android.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.path.common.android.provider.CensusColumns;
import org.path.common.android.utilities.JSONUtils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/*
 * @author belendia@gmail.com
 */
public class CensusModel implements Parcelable {
	public static final String TAG = "CensusModel";

	public static int MAIN_POINT = 1000;
	public static int ADDITIONAL_POINT = 100;
	public static int ALTERNATE_POINT = 10;

	private String instanceId;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String value) {
		instanceId = value;
	}

	private String placeName;

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String value) {
		placeName = value;
	}

	private String houseNumber;

	public String getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(String value) {
		houseNumber = value;
	}

	private String headName;

	public String getHeadName() {
		return headName;
	}

	public void setHeadName(String value) {
		headName = value;
	}

	private String comment;

	public String getComment() {
		return comment;
	}

	public void setComment(String value) {
		comment = value;
	}

	private double latitude;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double value) {
		latitude = value;
	}

	private double longitude;

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double value) {
		longitude = value;
	}

	private double altitude;

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double value) {
		altitude = value;
	}

	private double accuracy;

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double value) {
		accuracy = value;
	}

	private int selected;

	public int getSelected() {
		return selected;
	}

	public void setSelected(int value) {
		selected = value;
	}

	private double random;

	public double getRandom() {
		return random;
	}

	public void setRandom(double value) {
		random = value;
	}

	private int valid;

	public int getValid() {
		return valid;
	}

	public void setValid(int value) {
		valid = value;
	}

	private int excluded;

	public int getExcluded() {
		return excluded;
	}

	public void setExcluded(int value) {
		excluded = value;
	}

	private int processed;

	public int getProcessed() {
		return processed;
	}

	public void setProcessed(int value) {
		processed = value;
	}

	private String phoneNumber;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String value) {
		phoneNumber = value;
	}

	private String interviewerName;

	public String getInterviewerName() {
		return interviewerName;
	}

	public void setInterviewerName(String value) {
		interviewerName = value;
	}

	private String deviceId;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String value) {
		deviceId = value;
	}

	private String deviceName;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String value) {
		deviceName = value;
	}

	private String dateLastSelected;

	public String getDateLastSelected() {
		return dateLastSelected;
	}

	public void setDateLastSelected(String value) {
		dateLastSelected = value;
	}

	private int sampleFrame;

	public int getSampleFrame() {
		return sampleFrame;
	}

	public void setSampleFrame(int value) {
		sampleFrame = value;
	}

	private String createdDate;

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String value) {
		createdDate = value;
	}

	private String updatedDate;

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String value) {
		updatedDate = value;
	}

	private String formName;

	public String getFormName() {
		return formName;
	}

	public void setFormName(String value) {
		formName = value;
	}

	private double distance = -1;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double value) {
		distance = value;
	}

	public CensusModel() {

	}

	public CensusModel(Parcel in) {
		readFromParcel(in);
	}

	public static JSONObject getAsJSONObject(CensusModel census) {
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.put(CensusColumns._ID, census.getInstanceId() == null ? ""
					: census.getInstanceId());
			jsonobj.put(CensusColumns.PLACE_NAME,
					census.getPlaceName() == null ? "" : census.getPlaceName());
			jsonobj.put(
					CensusColumns.HOUSE_NUMBER,
					census.getHouseNumber() == null ? "" : census
							.getHouseNumber());
			jsonobj.put(CensusColumns.HEAD_NAME,
					census.getHeadName() == null ? "" : census.getHeadName());
			jsonobj.put(CensusColumns.COMMENT, census.getComment() == null ? ""
					: census.getComment());
			jsonobj.put(CensusColumns.LATITUDE, census.getLatitude());
			jsonobj.put(CensusColumns.LONGITUDE, census.getLongitude());
			jsonobj.put(CensusColumns.ALTITUDE, census.getAltitude());
			jsonobj.put(CensusColumns.ACCURACY, census.getAccuracy());
			jsonobj.put(CensusColumns.SELECTED, census.getSelected());
			jsonobj.put(CensusColumns.RANDOM, census.getRandom());
			jsonobj.put(CensusColumns.VALID, census.getValid());
			jsonobj.put(CensusColumns.EXCLUDED, census.getExcluded());
			jsonobj.put(CensusColumns.PROCESSED, census.getProcessed());
			jsonobj.put(
					CensusColumns.PHONE_NUMBER,
					census.getPhoneNumber() == null ? "" : census
							.getPhoneNumber());
			jsonobj.put(
					CensusColumns.INTERVIEWER_NAME,
					census.getInterviewerName() == null ? "" : census
							.getInterviewerName());
			jsonobj.put(CensusColumns.DEVICE_ID,
					census.getDeviceId() == null ? "" : census.getDeviceId());
			jsonobj.put(
					CensusColumns.DEVICE_NAME,
					census.getDeviceName() == null ? "" : census
							.getDeviceName());
			jsonobj.put(
					CensusColumns.DATE_LAST_SELECTED,
					census.getDateLastSelected() == null ? "" : census
							.getDateLastSelected());
			jsonobj.put(CensusColumns.SAMPLE_FRAME, census.getSampleFrame());
			jsonobj.put(
					CensusColumns.CREATED_DATE,
					census.getCreatedDate() == null ? "" : census
							.getCreatedDate());
			jsonobj.put(
					CensusColumns.UPDATED_DATE,
					census.getUpdatedDate() == null ? "" : census
							.getUpdatedDate());
			jsonobj.put(CensusColumns.FORM_NAME,
					census.getFormName() == null ? "" : census.getFormName());

		} catch (JSONException e) {
			Log.e(TAG, "getAsJSONObject : " + e.toString());
		}
		return jsonobj;
	}

	/*
	 * convert json object to census row.
	 */
	public static CensusModel parseCensusRow(JSONObject jsonobj) {
		CensusModel census = null;
		if (jsonobj != null) {
			try {
				census = new CensusModel();
				String temp = "";

				temp = jsonobj.getString(CensusColumns._ID);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setInstanceId(temp);

				temp = jsonobj.getString(CensusColumns.PLACE_NAME);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setPlaceName(temp);

				temp = jsonobj.getString(CensusColumns.HOUSE_NUMBER);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setHouseNumber(temp);

				temp = jsonobj.getString(CensusColumns.HEAD_NAME);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setHeadName(temp);

				temp = jsonobj.getString(CensusColumns.COMMENT);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setComment(temp);

				census.setLatitude(jsonobj.getDouble(CensusColumns.LATITUDE));
				census.setLongitude(jsonobj.getDouble(CensusColumns.LONGITUDE));
				census.setAltitude(jsonobj.getDouble(CensusColumns.ALTITUDE));
				census.setAccuracy(jsonobj.getDouble(CensusColumns.ACCURACY));
				census.setSelected(jsonobj.getInt(CensusColumns.SELECTED));
				census.setRandom(jsonobj.getDouble(CensusColumns.RANDOM));
				census.setValid(jsonobj.getInt(CensusColumns.VALID));
				census.setExcluded(jsonobj.getInt(CensusColumns.EXCLUDED));
				census.setProcessed(jsonobj.getInt(CensusColumns.PROCESSED));

				temp = jsonobj.getString(CensusColumns.PHONE_NUMBER);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setPhoneNumber(temp);

				temp = jsonobj.getString(CensusColumns.INTERVIEWER_NAME);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setInterviewerName(temp);

				temp = jsonobj.getString(CensusColumns.DEVICE_ID);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setDeviceId(temp);

				temp = jsonobj.getString(CensusColumns.DEVICE_NAME);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setDeviceName(temp);

				temp = jsonobj.getString(CensusColumns.DATE_LAST_SELECTED);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setDateLastSelected(temp);

				census.setSampleFrame(jsonobj
						.getInt(CensusColumns.SAMPLE_FRAME));

				temp = jsonobj.getString(CensusColumns.CREATED_DATE);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setCreatedDate(temp);

				temp = jsonobj.getString(CensusColumns.UPDATED_DATE);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setUpdatedDate(temp);

				temp = jsonobj.getString(CensusColumns.FORM_NAME);
				temp = (temp != null && temp.length() > 0 ? temp : null);
				census.setFormName(temp);
			} catch (JSONException e) {
				Log.e(TAG, "parseCensusRow: " + e.toString());
			}
		}
		return census;
	}

	/**
	 * convert a json string representation of census row into CensusModel
	 * object.
	 */
	public static CensusModel parseCensusRow(String jsonCensus) {
		JSONObject jsonobj = JSONUtils.getJsonObject(jsonCensus);
		Log.d(TAG, "parseCensusRow : " + jsonobj.toString());
		return parseCensusRow(jsonobj);
	}

	public static final Parcelable.Creator<CensusModel> CREATOR = new Parcelable.Creator<CensusModel>() {
		public CensusModel createFromParcel(Parcel in) {
			return new CensusModel(in);
		}

		public CensusModel[] newArray(int size) {
			return new CensusModel[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(getInstanceId());
		dest.writeString(getPlaceName());
		dest.writeString(getHouseNumber());
		dest.writeString(getHeadName());
		dest.writeString(getComment());
		dest.writeDouble(getLatitude());
		dest.writeDouble(getLongitude());
		dest.writeDouble(getAltitude());
		dest.writeDouble(getAccuracy());
		dest.writeInt(getSelected());
		dest.writeDouble(getRandom());
		dest.writeInt(getValid());
		dest.writeInt(getExcluded());
		dest.writeInt(getProcessed());
		dest.writeString(getPhoneNumber());
		dest.writeString(getInterviewerName());
		dest.writeString(getDeviceId());
		dest.writeString(getDeviceName());
		dest.writeString(getDateLastSelected());
		dest.writeInt(getSampleFrame());
		dest.writeString(getCreatedDate());
		dest.writeString(getUpdatedDate());
		dest.writeString(getFormName());
	}

	public void readFromParcel(Parcel in) {
		setInstanceId(in.readString());
		setPlaceName(in.readString());
		setHouseNumber(in.readString());
		setHeadName(in.readString());
		setComment(in.readString());
		setLatitude(in.readDouble());
		setLongitude(in.readDouble());
		setAltitude(in.readDouble());
		setAccuracy(in.readDouble());
		setSelected(in.readInt());
		setRandom(in.readDouble());
		setValid(in.readInt());
		setExcluded(in.readInt());
		setProcessed(in.readInt());
		setPhoneNumber(in.readString());
		setInterviewerName(in.readString());
		setDeviceId(in.readString());
		setDeviceName(in.readString());
		setDateLastSelected(in.readString());
		setSampleFrame(in.readInt());
		setCreatedDate(in.readString());
		setUpdatedDate(in.readString());
		setFormName(in.readString());
	}
}
