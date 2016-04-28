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
package org.path.episample.android.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.path.common.android.data.DeviceInfo;
import org.path.common.android.data.SendReceiveConstants;
import org.path.common.android.utilities.JSONUtils;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.SendReceiveFragment;

import android.os.Handler;
import android.util.Log;

/*
 * @author belendia@gmail.com
 */
/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */

public class SendReceiveManager implements Runnable {
	private static final String t = "SendReceiveManager";

	private Socket mSocket = null;
	private Handler mHandler;

	private BufferedReader in;
	private BufferedWriter out;
	private static int mCounter = 0;

	public enum ManagerType {
		Client, Server
	};

	private ManagerType mManagerType;

	public SendReceiveManager(Socket socket, Handler handler, ManagerType type) {
		this.mSocket = socket;
		this.mHandler = handler;
		mManagerType = type;
		SendReceiveFragment.mRemoteAddress = socket.getInetAddress();

		if (mManagerType == ManagerType.Server) {
			// introduce the basic info like valid, invalid and excluded points
			// to client and wait the client introduction.
			write(JSONUtils.prepareDeviceInfoJSONData(
					DeviceInfo.getAsJSONObject(Survey.getInstance()
							.getApplicationContext(), SendReceiveFragment
							.getDevice().deviceName),
					JSONUtils.SERVER_INFO_COMMAND).toString());
		}

	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(
					mSocket.getInputStream()));
			String line = null;
			line = in.readLine();
			if (line != null && !(line.equals(""))) {
				executeCommand(line);
				// mHandler.obtainMessage(SendReceiveConstants.MSG_READ,
				// line).sendToTarget();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void executeCommand(final String data) {
		try {
			String command = JSONUtils.getCommandFromJSON(data);
			if (command != null) {
				if (command.equals(JSONUtils.SERVER_INFO_COMMAND)) {
					// when the client gets server's basic info then it will
					// send its basic info to the server
					write(JSONUtils
							.prepareDeviceInfoJSONData(
									DeviceInfo
											.getAsJSONObject(
													Survey.getInstance()
															.getApplicationContext(),
													SendReceiveFragment
															.getDevice().deviceName),
									JSONUtils.CLIENT_INFO_COMMAND).toString());
					mHandler.obtainMessage(SendReceiveConstants.MSG_INFO, data)
							.sendToTarget();
				} else if (command.equals(JSONUtils.CLIENT_INFO_COMMAND)) {
					mHandler.obtainMessage(SendReceiveConstants.MSG_INFO, data)
							.sendToTarget();
				}
			}

		} finally {
		}
	}

	public void write(String strToSend) {
		try {

			if (mSocket != null && mSocket.isConnected()) {
				out = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream()));
				out.write(strToSend);
				out.newLine();
				out.flush();
			}
		} catch (IOException e) {
			Log.e(t, "Exception during write", e);
		}
	}
}
