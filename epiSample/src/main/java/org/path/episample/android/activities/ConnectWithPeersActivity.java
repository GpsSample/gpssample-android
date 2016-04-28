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

package org.path.episample.android.activities;

import java.util.List;

import org.path.episample.android.R;
import org.path.episample.android.fragments.SendReceiveFragment;
import org.path.episample.android.listeners.ISendReceiveScreen;
import org.path.episample.android.receivers.SendReceiveBroadcastReceiver;
import org.path.episample.android.utilities.WiFiUtils;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/*
 * @author belendia@gmail.com
 */

public class ConnectWithPeersActivity extends ListActivity implements
		ISendReceiveScreen, PeerListListener {

	private String appName = null;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private SendReceiveBroadcastReceiver mConnectionReceiver;
	private final IntentFilter mIntentFilter = new IntentFilter();

	private ImageView mConnectionFoundImageView;
	private LinearLayout mLoadingLinearLayout;
	private TextView mEmptyTextView;

	public static String WIFI_P2P_MANAGER = "wifi_p2p_manager";
	public static String CHANNEL = "channel";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppName("survey");
		setContentView(R.layout.connect_with_peers_dialog);

		mConnectionFoundImageView = (ImageView) findViewById(R.id.connectionFoundImageView);
		mLoadingLinearLayout = (LinearLayout) findViewById(R.id.loadingLinearLayout);
		mEmptyTextView = (TextView) findViewById(android.R.id.empty);

		// Indicates a change in the Wi-Fi P2P status.
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

		// Indicates a change in the list of available peers.
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// Indicates the state of Wi-Fi P2P connectivity has changed.
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		// Indicates this device's details have changed.
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);

	}

	@Override
	public void onResume() {
		super.onResume();
		mConnectionReceiver = new SendReceiveBroadcastReceiver(mManager,
				mChannel, this);
		mConnectionReceiver.setPeerListListener(this);
		registerReceiver(mConnectionReceiver, mIntentFilter);
		setListAdapter(new PeerListAdapter(ConnectWithPeersActivity.this,
				R.layout.device_list_item, SendReceiveFragment.getPeers()));

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				discoverPeers();
			}
		}, 1500);

	}

	public void discoverPeers() {
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				mLoadingLinearLayout.setVisibility(View.VISIBLE);
				mConnectionFoundImageView.setVisibility(View.VISIBLE);
				mEmptyTextView.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(int reasonCode) {
				mLoadingLinearLayout.setVisibility(View.GONE);
				mConnectionFoundImageView.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		mConnectionReceiver.setPeerListListener(null);
		unregisterReceiver(mConnectionReceiver);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (SendReceiveFragment.getPeers().size() >= 0) {
			WifiP2pDevice selectedDevice = (WifiP2pDevice) SendReceiveFragment
					.getPeers().get(position);
			Intent returnIntent = new Intent();
			returnIntent.putExtra("selectedDevice", selectedDevice);

			setResult(RESULT_OK, returnIntent);
			finish();
		}

	}

	public void closeClickListener(View view) {
		finish();
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return this.appName;
	}

	public void setWifiP2pManager(WifiP2pManager manager) {
		mManager = manager;
	}

	public void setChannel(Channel channel) {
		mChannel = channel;
	}

	/**
	 * Array adapter for ListFragment that maintains WifiP2pDevice list.
	 */
	private class PeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

		private List<WifiP2pDevice> items;

		/**
		 * @param context
		 * @param resourceId
		 * @param objects
		 */
		public PeerListAdapter(Context context, int resourceId,
				List<WifiP2pDevice> objects) {
			super(context, resourceId, objects);
			items = objects;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.device_list_item, null);
			}
			WifiP2pDevice device = items.get(position);
			if (device != null) {
				TextView deviceName = (TextView) v
						.findViewById(R.id.deviceNameTextView);
				TextView deviceStatus = (TextView) v
						.findViewById(R.id.deviceStatusTextView);
				deviceName.setText(device.deviceName);
				deviceStatus.setText(WiFiUtils.getDeviceStatus(device.status));
			}

			return v;
		}
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		mConnectionFoundImageView.setVisibility(View.VISIBLE);
		mLoadingLinearLayout.setVisibility(View.GONE);
		mEmptyTextView.setVisibility(View.VISIBLE);
		SendReceiveFragment.getPeers().clear();
		SendReceiveFragment.getPeers().addAll(peerList.getDeviceList());

		// If an AdapterView is backed by this data, notify it
		// of the change. For instance, if you have a ListView of available
		// peers, trigger an update.
		((PeerListAdapter) getListAdapter()).notifyDataSetChanged();
		if (SendReceiveFragment.getPeers().size() == 0) {
			mConnectionFoundImageView.setVisibility(View.GONE);
			return;
		}
	}

	public void rediscoverPeersClickListener(View view) {
		discoverPeers();
	}

	@Override
	public void updateDeviceInfo(WifiP2pDevice device) {

	}

	@Override
	public void setIsWifiP2pEnabled(boolean value) {

	}

	@Override
	public void resetData() {

	}

	@Override
	public void disconnected() {

	}

	@Override
	public void wifiStateMessage(boolean value) {
		if (value == true) {
			mEmptyTextView.setText(getString(R.string.no_devices_found));
		} else {
			mEmptyTextView
					.setText(getString(R.string.no_devices_found_p2p_disabled));
		}

		mEmptyTextView.setVisibility(View.VISIBLE);
		mLoadingLinearLayout.setVisibility(View.GONE);
		mConnectionFoundImageView.setVisibility(View.GONE);
	}
}
