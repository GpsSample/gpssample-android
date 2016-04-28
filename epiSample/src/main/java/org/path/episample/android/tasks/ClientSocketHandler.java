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
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

public class ClientSocketHandler extends Thread {
	private static final String t = "ClientSocketHandler";
	private static final int TIMEOUT = 50000;
	private Handler mHandler;
	private InetAddress mAddress;

	public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress) {
		this.mHandler = handler;
		this.mAddress = groupOwnerAddress;
	}

	@Override
	public void run() {

		try {
			Socket socket = new Socket();
			socket.bind(null);
			socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
					SendReceiveSocketUtils.SERVER_PORT), TIMEOUT);
			SendReceiveFragment.mRemoteAddress = socket.getInetAddress();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String data = null;

			data = in.readLine();
			if (data != null && !(data.equals(""))) {
				String command = JSONUtils.getCommandFromJSON(data);
				if (command != null) {
					if (command.equals(JSONUtils.SERVER_INFO_COMMAND)) {
						mHandler.obtainMessage(SendReceiveConstants.MSG_INFO,
								data).sendToTarget();
					}
				}
			}

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			out.write(JSONUtils.prepareDeviceInfoJSONData(
					DeviceInfo.getAsJSONObject(Survey.getInstance()
							.getApplicationContext(), SendReceiveFragment
							.getDevice().deviceName),
					JSONUtils.CLIENT_INFO_COMMAND).toString());
			out.newLine();
			out.flush();

			socket.close();
		} catch (IOException e) {
			Log.d(t, "Client error");
			e.printStackTrace();
		}
	}
}
