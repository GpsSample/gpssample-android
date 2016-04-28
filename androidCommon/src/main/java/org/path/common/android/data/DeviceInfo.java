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

/*
 * @author belendia@gmail.com
 */

import org.json.JSONException;
import org.json.JSONObject;
import org.path.common.android.utilities.CensusUtil;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class DeviceInfo implements Parcelable {
	public static final String t = "DeviceInfo";
	public static final String VALID = "valid";
	public static final String INVALID = "invalid";
	public static final String EXCLUDED = "excluded";
	public static final String HOST_NAME = "hostName";

	private String hostName;
	private int status;
	private int totalValid;
	private int totalInvalid;
	private int totalExcluded;
	private int received;
	private int sent;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String value) {
		hostName = value;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int value) {
		status = value;
	}

	public int getTotalValid() {
		return totalValid;
	}

	public void setTotalValid(int value) {
		totalValid = value;
	}

	public int getTotalInvalid() {
		return totalInvalid;
	}

	public void setTotalInvalid(int value) {
		totalInvalid = value;
	}

	public int getTotalExcluded() {
		return totalExcluded;
	}

	public void setTotalExcluded(int value) {
		totalExcluded = value;
	}

	public int getReceived() {
		return received;
	}

	public void setReceived(int value) {
		received = value;
	}

	public int getSent() {
		return sent;
	}

	public void setSent(int value) {
		sent = value;
	}

	public DeviceInfo() {

	}

	public DeviceInfo(Parcel in) {
		readFromParcel(in);
	}

	public static DeviceInfo parseDeviceInfo(JSONObject jsonobj) {
		DeviceInfo info = null;
		if (jsonobj != null) {
			try {
				info = new DeviceInfo();

				info.setHostName(jsonobj.getString(HOST_NAME));
				info.setTotalValid(jsonobj.getInt(VALID));
				info.setTotalInvalid(jsonobj.getInt(INVALID));
				info.setTotalExcluded(jsonobj.getInt(EXCLUDED));

			} catch (JSONException e) {
				Log.e(t, "parseCensusRow: " + e.toString());
			}
		}
		return info;
	}

	public static JSONObject getAsJSONObject(Context context, String hostName) {
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.put(HOST_NAME, hostName);
			jsonobj.put(VALID, CensusUtil.getTotalValid(context));
			jsonobj.put(INVALID, CensusUtil.getTotalInvalid(context));
			jsonobj.put(EXCLUDED, CensusUtil.getTotalExcluded(context));

		} catch (JSONException e) {
			Log.e(t, "getAsJSONObject : " + e.toString());
		}
		return jsonobj;
	}

	public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
		public DeviceInfo createFromParcel(Parcel in) {
			return new DeviceInfo(in);
		}

		public DeviceInfo[] newArray(int size) {
			return new DeviceInfo[size];
		}
	};

	@Override
	public int describeContents() {

		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(getHostName());
		dest.writeInt(getStatus());
		dest.writeInt(getTotalValid());
		dest.writeInt(getTotalInvalid());
		dest.writeInt(getTotalExcluded());
		dest.writeInt(getReceived());
		dest.writeInt(getSent());
	}

	public void readFromParcel(Parcel in) {
		setHostName(getHostName());
		setStatus(getStatus());
		setTotalValid(getTotalValid());
		setTotalInvalid(getTotalInvalid());
		setTotalExcluded(getTotalExcluded());
		setReceived(getReceived());
		setSent(getSent());
	}
}