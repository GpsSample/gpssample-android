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

package org.path.episample.android.receivers;

import org.path.episample.android.listeners.ISendReceiveScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/*
 * @author belendia@gmail.com
 */

public class SendReceiveBroadcastReceiver extends BroadcastReceiver {
	private static final String t = "SendReceiveBroadcastReceiver";
	private WifiP2pManager mManager;
	private Channel mChannel;
	private ISendReceiveScreen mScreen;
	private PeerListListener mPeerListListener;
	private ConnectionInfoListener mConnectionInfoListener;

	/**
	 * @param manager
	 *            WifiP2pManager system service
	 * @param channel
	 *            Wifi p2p channel
	 * @param activity
	 *            fragment associated with the receiver
	 */
	public SendReceiveBroadcastReceiver(WifiP2pManager manager,
			Channel channel, ISendReceiveScreen screen) {
		super();
		mManager = manager;
		mChannel = channel;
		mScreen = screen;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Determine if Wifi P2P mode is enabled or not, alert
			// the Activity.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (mScreen != null) {
				if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					mScreen.setIsWifiP2pEnabled(true);
					mScreen.wifiStateMessage(true);
				} else {
					mScreen.setIsWifiP2pEnabled(false);
					mScreen.resetData();
					mScreen.wifiStateMessage(false);
				}
			}
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

			// request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			if (mManager != null) {
				mManager.requestPeers(mChannel, mPeerListListener);
			}

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {

			if (mManager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {
				// we are connected with the other device, request connection
				// info to find group owner IP
				mManager.requestConnectionInfo(mChannel,
						mConnectionInfoListener);
			} else {
				// It's a disconnect
				mScreen.disconnected();
				mScreen.resetData();
			}

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			if (mScreen != null) {
				mScreen.updateDeviceInfo((WifiP2pDevice) intent
						.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
			}
		}
	}

	public void setPeerListListener(PeerListListener listener) {
		mPeerListListener = listener;
	}

	public void setConnectionInfoListener(ConnectionInfoListener listener) {
		mConnectionInfoListener = listener;
	}
}
