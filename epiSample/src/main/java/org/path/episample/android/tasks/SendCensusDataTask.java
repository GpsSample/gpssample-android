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

package org.path.episample.android.tasks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.path.common.android.data.CensusModel;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.JSONUtils;
import org.path.episample.android.R;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.SendCensusDataListener;
import org.path.episample.android.utilities.SendReceiveSocketUtils;

import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class SendCensusDataTask extends AsyncTask<InetAddress, String, Void> {
	private static final String t = "SendCensusDataTask";
	private SendCensusDataListener mListener;
	private static int TIMEOUT = 50000;
	int mCounter = 0;
	private Socket mSocket;
	private static int RECORD_TO_SEND_AT_A_TIME = 10;
	private boolean mErrorOccured = false;
	private String mErrorMessage = "";

	@Override
	protected Void doInBackground(InetAddress... params) {
		InetAddress remoteAddress = params[0];
		if (remoteAddress != null) {
			try {
				mSocket = new Socket();
				mSocket.bind(null);

				mSocket.connect(
						new InetSocketAddress(remoteAddress.getHostAddress(),
								SendReceiveSocketUtils.SERVER_PORT), TIMEOUT);

				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream()));

				publishProgress(Survey.getInstance().getString(
						R.string.sending_data));

				ArrayList<CensusModel> censuses = CensusUtil.getAll(Survey
						.getInstance().getApplicationContext());
				int total = censuses.size();

				ArrayList<CensusModel> censusModelBucket = new ArrayList<CensusModel>();
				int start = 1;
				publishProgress(Survey.getInstance().getString(
						R.string.saving_data));
				for (mCounter = 0; mCounter < total && isCancelled() == false; mCounter++) {
					if ((mCounter > 0 && mCounter % RECORD_TO_SEND_AT_A_TIME == 0)
							|| mCounter == total - 1) {
						censusModelBucket.add(censuses.get(mCounter));
						out.write(JSONUtils.prepareCensusJSONData(
								censusModelBucket, total, start, mCounter + 1)
								.toString());
						out.newLine();
						out.flush();

						censusModelBucket.clear();
						start = mCounter + 1;
					} else {
						censusModelBucket.add(censuses.get(mCounter));
					}
				}

				mSocket.close();
			} catch (UnknownHostException e) {
				mErrorOccured = true;
				mErrorMessage = Survey.getInstance().getString(
						R.string.unknown_host);
			} catch (ConnectException e) {
				mErrorOccured = true;
				mErrorMessage = Survey.getInstance().getString(
						R.string.connection_refused);
			} catch (NoRouteToHostException e) {
				mErrorOccured = true;
				mErrorMessage = Survey.getInstance().getString(
						R.string.connection_timed_out);
			} catch (IOException e) {
				mErrorOccured = true;
				mErrorMessage = Survey.getInstance().getString(
						R.string.unable_to_send_data);
				e.printStackTrace();
			}

		}

		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		synchronized (this) {
			closeSocket();
			if (mListener != null) {
				if (mErrorOccured == true) {
					mListener.sendError(mCounter, mErrorMessage);
				} else {
					mListener.sendingComplete(mCounter);
				}
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		synchronized (this) {
			if (mListener != null) {
				// update progress and total
				mListener.sendProgressUpdate(values[0]);
			}
		}
	}

	@Override
	protected void onCancelled() {
		synchronized (this) {
			closeSocket();
			if (mListener != null) {
				mListener.sendingCanceled(mCounter);
			}
		}
	}

	public void setSendCensusDataListener(SendCensusDataListener listener) {
		synchronized (this) {
			mListener = listener;
		}
	}

	public int getTotalSent() {
		return mCounter;
	}

	private void closeSocket() {
		if (mSocket != null) {
			try {
				if (mSocket.isConnected()) {
					mSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
