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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.path.common.android.data.DeviceInfo;
import org.path.common.android.data.SendReceiveConstants;
import org.path.common.android.utilities.JSONUtils;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.SendReceiveFragment;
import org.path.episample.android.utilities.SendReceiveSocketUtils;

import android.os.Handler;
import android.util.Log;

/*
 * @author belendia@gmail.com
 */
/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
public class GroupOwnerSocketHandler extends Thread {

	private static final String t = "GroupOwnerSocketHandler";
	private Handler mHandler;

	public GroupOwnerSocketHandler(Handler handler) throws IOException {
		mHandler = handler;

	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = SendReceiveSocketUtils
					.getServerSocket();
			Socket client = serverSocket.accept();
			SendReceiveFragment.mRemoteAddress = client.getInetAddress();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					client.getOutputStream()));
			out.write(JSONUtils.prepareDeviceInfoJSONData(
					DeviceInfo.getAsJSONObject(Survey.getInstance()
							.getApplicationContext(), SendReceiveFragment
							.getDevice().deviceName),
					JSONUtils.SERVER_INFO_COMMAND).toString());
			out.newLine();
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			String data = null;

			data = in.readLine();
			if (data != null && !(data.equals(""))) {
				String command = JSONUtils.getCommandFromJSON(data);
				if (command != null) {
					if (command.equals(JSONUtils.CLIENT_INFO_COMMAND)) {
						mHandler.obtainMessage(SendReceiveConstants.MSG_INFO,
								data).sendToTarget();
					}
				}
			}

			serverSocket.close();

		} catch (IOException e) {
			Log.d(t, "Server Error");
			// e.printStackTrace();
		}
	}

}
