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

package org.path.episample.android.listeners;

import android.net.wifi.p2p.WifiP2pDevice;

/*
 * @author belendia@gmail.com
 */

public interface ISendReceiveScreen {
	public void updateDeviceInfo(WifiP2pDevice device);

	public void setIsWifiP2pEnabled(boolean value);

	public void resetData();

	public void disconnected();

	public void wifiStateMessage(boolean value);
}
