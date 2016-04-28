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

package org.path.episample.android.fragments;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.path.common.android.data.DeviceInfo;
import org.path.common.android.data.SendReceiveConstants;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.JSONUtils;
import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.ConnectWithPeersActivity;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.fragments.ProgressDialogFragment.CancelProgressDialog;
import org.path.episample.android.listeners.DisconnectDeviceListener;
import org.path.episample.android.listeners.ISendReceiveScreen;
import org.path.episample.android.listeners.ReceiveCensusDataListener;
import org.path.episample.android.listeners.SendCensusDataListener;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;
import org.path.episample.android.receivers.SendReceiveBroadcastReceiver;
import org.path.episample.android.tasks.ClientSocketHandler;
import org.path.episample.android.tasks.GroupOwnerSocketHandler;
import org.path.episample.android.tasks.SendCensusDataTask;
import org.path.episample.android.utilities.WiFiUtils;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author belendia@gmail.com
 */

public class SendReceiveFragment extends ListFragment implements
		Handler.Callback, ISendReceiveScreen, ConnectionInfoListener,
		PeerListListener, ChannelListener, CancelProgressDialog,
		ConfirmAlertDialog, DisconnectDeviceListener, SendCensusDataListener,
		ReceiveCensusDataListener {
	private static final String t = "SendReceiveFragment";
	public static final int ID = R.layout.send_receive;
	protected static final int REQUEST_CODE = 27843;
	private View view;

	private static enum DialogState {
		ConnectingProgress, SendingProgress, ReceivingProgress, Alert, None
	};

	private static final String DIALOG_STATE = "dialogState";
	private static final String DIALOG_MSG = "dialogMsg";
	private static final String PROGRESS_MSG = "progressMsg";
	private static final String DEVICE_INFO_FROM_PEER = "deviceInfoFromPeer";
	private static final String CLIENT_DEVICE = "clientDevice";

	private String mAlertMsg;
	private String mProgressMsg;
	private Handler handler = new Handler();

	private DialogState mDialogState = DialogState.None;
	private static final String PROGRESS_DIALOG_TAG = "progressDialog";
	private ProgressDialogFragment progressDialog = null;

	private RelativeLayout mConnectButtonContainer;
	private ImageButton mConnectImageButton;
	private RelativeLayout mDiscoverPeersButtonContainer;
	private ImageButton mDiscoverPeersImageButton;
	private RelativeLayout mReceiveButtonContainer;
	private ImageButton mReceiveImageButton;
	private RelativeLayout mSendButtonContainer;
	private ImageButton mSendImageButton;
	private LinearLayout mPeersDiscoveredContainer;

	private TextView mValidPointsTextView;
	private TextView mInvalidPointsTextView;
	private TextView mExcludedPointsTextView;
	private TextView mMyDeviceNameTextView;
	private TextView mMyDeviceStatusTextView;
	private TextView mEmptyTextView;
	private TextView mPeersDiscoveredTextView;

	private ProgressBar mSearchingProgressBar;

	private final IntentFilter mIntentFilter = new IntentFilter();
	private WifiP2pManager mManager;
	private Channel mChannel;
	private SendReceiveBroadcastReceiver mConnectionReceiver;
	private static WifiP2pDevice mDevice;

	private String mAppName;
	private boolean mIsWifiP2pEnabled;
	private boolean mRetryChannel = false;
	private WifiP2pDevice mClientDevice;
	private boolean mDeviceBelongToTeamLead = false;

	private Handler mMsgHandler = new Handler(this);

	SendCensusDataTask mSendDataToPeerTask = new SendCensusDataTask();
	DeviceInfo mDeviceInfoFromPeer = new DeviceInfo();
	private WifiManager mWifiManager;
	public static InetAddress mRemoteAddress;

	private static List mPeers = new ArrayList();

	public static List getPeers() {
		return mPeers;
	}

	private static ArrayList<DeviceInfo> mConnectedDevices = new ArrayList<DeviceInfo>();

	public static ArrayList<DeviceInfo> getConnectedDevices() {
		return mConnectedDevices;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.send_receive_wifi_direct);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}

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

		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(DEVICE_INFO_FROM_PEER)) {
			mDeviceInfoFromPeer = savedInstanceState
					.getParcelable(DEVICE_INFO_FROM_PEER);
		}

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(DIALOG_MSG)) {
			mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
		}

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(PROGRESS_MSG)) {
			mProgressMsg = savedInstanceState.getString(PROGRESS_MSG);
		}

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(DIALOG_STATE)) {
			mDialogState = DialogState.valueOf(savedInstanceState
					.getString(DIALOG_STATE));
		}

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(CLIENT_DEVICE)) {
			mClientDevice = savedInstanceState.getParcelable(CLIENT_DEVICE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(ID, container, false);

		mValidPointsTextView = (TextView) view
				.findViewById(R.id.totalValidTextView);
		mInvalidPointsTextView = (TextView) view
				.findViewById(R.id.totalInalidTextView);
		mExcludedPointsTextView = (TextView) view
				.findViewById(R.id.totalExcludedTextView);

		mConnectButtonContainer = (RelativeLayout) view
				.findViewById(R.id.connectWithFriendsButtonContainer);
		mConnectImageButton = (ImageButton) view
				.findViewById(R.id.connectWithFriendsImageButton);

		mConnectButtonContainer.setOnClickListener(connectClickListener);
		mConnectImageButton.setOnClickListener(connectClickListener);

		mDiscoverPeersButtonContainer = (RelativeLayout) view
				.findViewById(R.id.discoverPeersButtonContainer);
		mDiscoverPeersImageButton = (ImageButton) view
				.findViewById(R.id.discoverPeersImageButton);

		mDiscoverPeersButtonContainer
				.setOnClickListener(discoverPeersClickListener);
		mDiscoverPeersImageButton
				.setOnClickListener(discoverPeersClickListener);

		mReceiveButtonContainer = (RelativeLayout) view
				.findViewById(R.id.receiveButtonContainer);
		mReceiveImageButton = (ImageButton) view
				.findViewById(R.id.receiveImageButton);
		mPeersDiscoveredContainer = (LinearLayout) view
				.findViewById(R.id.peersDiscoveredContainer);

		mReceiveButtonContainer.setOnClickListener(receiveClickListener);
		mReceiveImageButton.setOnClickListener(receiveClickListener);

		mSendButtonContainer = (RelativeLayout) view
				.findViewById(R.id.sendButtonContainer);
		mSendImageButton = (ImageButton) view
				.findViewById(R.id.sendImageButton);

		mSendButtonContainer.setOnClickListener(sendClickListener);
		mSendImageButton.setOnClickListener(sendClickListener);

		mMyDeviceNameTextView = (TextView) view
				.findViewById(R.id.myDeviceNameTextView);
		mMyDeviceStatusTextView = (TextView) view
				.findViewById(R.id.myDeviceStatusTextView);
		mEmptyTextView = (TextView) view.findViewById(android.R.id.empty);
		mPeersDiscoveredTextView = (TextView) view
				.findViewById(R.id.peersDiscoveredTextView);

		mSearchingProgressBar = (ProgressBar) view
				.findViewById(R.id.searchingProgressBar);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mManager = (WifiP2pManager) getActivity().getSystemService(
				Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(getActivity(), getActivity()
				.getMainLooper(), null);
		DeviceListAdapter adapter = new DeviceListAdapter(getActivity(),
				R.layout.device_detail_list_item, getConnectedDevices());
		adapter.setDisconnect(this);
		setListAdapter(adapter);
		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DIALOG_STATE, mDialogState.name());
		outState.putString(DIALOG_MSG, mAlertMsg);
		outState.putString(PROGRESS_MSG, mProgressMsg);
		// outState.putSerializable(SEND_RECEIVE_MANAGER, mSendReceiveManager);
		outState.putParcelable(DEVICE_INFO_FROM_PEER, mDeviceInfoFromPeer);
		outState.putParcelable(CLIENT_DEVICE, mClientDevice);
	}

	public void discoverPeers() {
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				mSearchingProgressBar.setVisibility(View.VISIBLE);
				mPeersDiscoveredTextView.setText(getActivity().getString(
						R.string.finding_peers));
			}

			@Override
			public void onFailure(int reasonCode) {
				mSearchingProgressBar.setVisibility(View.GONE);
				mPeersDiscoveredTextView.setText(getActivity().getString(
						R.string.finding_peers_failed));
			}
		});
	}

	private void refreshTotalShortInfo() {
		mValidPointsTextView.setText(String.valueOf(CensusUtil
				.getTotalValid(getActivity())));
		mInvalidPointsTextView.setText(String.valueOf(CensusUtil
				.getTotalInvalid(getActivity())));
		mExcludedPointsTextView.setText(String.valueOf(CensusUtil
				.getTotalExcluded(getActivity())));
	}

	public String getAppName() {
		return mAppName;
	}

	@Override
	public void onResume() {
		super.onResume();
		mConnectionReceiver = new SendReceiveBroadcastReceiver(mManager,
				mChannel, this);
		mConnectionReceiver.setPeerListListener(this);
		mConnectionReceiver.setConnectionInfoListener(this);

		getActivity().registerReceiver(mConnectionReceiver, mIntentFilter);

		refreshTotalShortInfo();

		if (mDialogState == DialogState.ConnectingProgress
				|| mDialogState == DialogState.SendingProgress
				|| mDialogState == DialogState.ReceivingProgress) {
			restoreProgressDialog();
		} else if (mDialogState == DialogState.Alert) {
			restoreAlertDialog();
		}

		String get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_DEVICE_BELONG_TO_TEAM_LEAD);
		if (get != null && get.equalsIgnoreCase("true")) {
			mConnectButtonContainer.setVisibility(View.VISIBLE);
			// mSendPullButtonContainer.setVisibility(View.VISIBLE);

			mDiscoverPeersButtonContainer.setVisibility(View.GONE);
			mPeersDiscoveredContainer.setVisibility(View.GONE);
			mEmptyTextView.setText(getActivity().getString(
					R.string.no_connected_peers));
			mDeviceBelongToTeamLead = true;
		} else {
			mConnectButtonContainer.setVisibility(View.GONE);
			// mSendPullButtonContainer.setVisibility(View.GONE);

			mDiscoverPeersButtonContainer.setVisibility(View.VISIBLE);
			mPeersDiscoveredContainer.setVisibility(View.VISIBLE);
			mEmptyTextView.setText(getActivity().getString(
					R.string.team_lead_device_not_connected));
			mDeviceBelongToTeamLead = false;
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_TURN_ON_OFF_WIFI_AUTOMATICALLY);
		// enable wifi
		if (!(get != null && get.equalsIgnoreCase("false"))) {
			if (!mWifiManager.isWifiEnabled()
					&& mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
				mWifiManager.setWifiEnabled(true);
			}
		}
	}

	@Override
	public void onPause() {
		dismissProgressDialog();

		super.onPause();
		mConnectionReceiver.setPeerListListener(null);
		mConnectionReceiver.setConnectionInfoListener(null);
		getActivity().unregisterReceiver(mConnectionReceiver);
	}

	public OnClickListener connectClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {

			Intent intent = new Intent(getActivity(),
					ConnectWithPeersActivity.class);
			startActivityForResult(intent, REQUEST_CODE);

		}
	};

	public OnClickListener discoverPeersClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			discoverPeers();
		}
	};

	public OnClickListener receiveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			receiveData();
		}
	};

	public OnClickListener sendClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			sendData();
		}
	};

	@Override
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		mIsWifiP2pEnabled = isWifiP2pEnabled;
	}

	/**
	 * Update UI for this device.
	 * 
	 * @param device
	 *            WifiP2pDevice object
	 */
	@Override
	public void updateDeviceInfo(WifiP2pDevice device) {
		mDevice = device;
		mMyDeviceNameTextView.setText(device.deviceName);
		mMyDeviceStatusTextView.setText(WiFiUtils
				.getDeviceStatus(device.status));
	}

	public static WifiP2pDevice getDevice() {
		return mDevice;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE) {
			if (data != null) {
				mClientDevice = data.getParcelableExtra("selectedDevice");
				if (mClientDevice != null) {
					connect(mClientDevice);
				}
			}
		}
	}

	public void connect(WifiP2pDevice device) {

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		// config.groupOwnerIntent = 15;

		mManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				showProgressDialog(DialogState.ConnectingProgress, Survey
						.getInstance().getString(R.string.connecting_message));
			}

			@Override
			public void onFailure(int reason) {
				mPeersDiscoveredTextView.setText(getActivity().getString(
						R.string.connection_failed));
				if (mDialogState == DialogState.ConnectingProgress) {
					try {
						mDialogState = DialogState.None;
						dismissProgressDialog();
					} catch (IllegalArgumentException e) {
						WebLogger
								.getLogger(
										((ODKActivity) getActivity())
												.getAppName())
								.i(t,
										"Attempting to close a dialog that was not previously opened");
					}
				}
			}
		});
	}

	@Override
	public void resetData() {
		getConnectedDevices().clear();
		((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
		mDeviceInfoFromPeer = null;
		// mPeersDiscoveredTextView.setText(getActivity().getString(R.string.start_peer_discovery));
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		if (mDialogState == DialogState.ConnectingProgress) {
			try {
				mDialogState = DialogState.None;
				dismissProgressDialog();
			} catch (IllegalArgumentException e) {
				WebLogger
						.getLogger(((ODKActivity) getActivity()).getAppName())
						.i(t,
								"Attempting to close a dialog that was not previously opened");
			}
		}

		Thread handler = null;
		/*
		 * The group owner accepts connections using a server socket and then
		 * spawns a client socket for every client. This is handled by {@code
		 * GroupOwnerSocketHandler}
		 */

		if (info.groupFormed && info.isGroupOwner) {
			Log.d(t, "Connected as group owner");
			try {

				handler = new GroupOwnerSocketHandler(mMsgHandler);
				handler.start();

			} catch (IOException e) {
				Log.d(t, "Failed to create a server thread - " + e.getMessage());
				return;
			}
		} else if (info.groupFormed) {
			Log.d(t, "Connected as peer");
			handler = new ClientSocketHandler(mMsgHandler,
					info.groupOwnerAddress);
			handler.start();
		}

		getConnectedDevices().clear();

		// WifiP2pDevice peer = getConnectedPeerInfo();
		if (mDeviceInfoFromPeer == null) {
			mDeviceInfoFromPeer = new DeviceInfo();
		}

		mDeviceInfoFromPeer
				.setHostName(mClientDevice != null ? mClientDevice.deviceName
						: mDeviceInfoFromPeer.getHostName());

		// if(deviceShownInList(deviceInfo)== false) {
		mDeviceInfoFromPeer.setStatus(mDevice.status);
		getConnectedDevices().add(mDeviceInfoFromPeer);
		((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
		// }
	}

	/*
	 * private boolean deviceShownInList(DeviceInfo device) { for(DeviceInfo d :
	 * getConnectedDevices()) { if(d.getHostName().equals(device.getHostName()))
	 * { return true; } }
	 * 
	 * return false; }
	 */

	private void restoreProgressDialog() {
		Fragment alert = getFragmentManager().findFragmentByTag("alertDialog");
		if (alert != null) {
			((AlertDialogFragment) alert).dismiss();
		}

		if (mDialogState == DialogState.ConnectingProgress
				|| mDialogState == DialogState.SendingProgress
				|| mDialogState == DialogState.ReceivingProgress) {
			Fragment dialog = getFragmentManager().findFragmentByTag(
					PROGRESS_DIALOG_TAG);

			if (dialog != null
					&& ((ProgressDialogFragment) dialog).getDialog() != null) {
				((ProgressDialogFragment) dialog).getDialog().setTitle(
						Survey.getInstance().getString(
								R.string.send_receive_wifi_direct));
				((ProgressDialogFragment) dialog).setMessage(mProgressMsg);
			} else if (progressDialog != null
					&& progressDialog.getDialog() != null) {
				progressDialog.getDialog().setTitle(
						Survey.getInstance().getString(
								R.string.send_receive_wifi_direct));
				progressDialog.setMessage(mProgressMsg);
			} else {
				if (progressDialog != null) {
					dismissProgressDialog();
				}
				progressDialog = ProgressDialogFragment.newInstance(
						getId(),
						Survey.getInstance().getString(
								R.string.send_receive_wifi_direct),
						mProgressMsg);
				progressDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
			}
		} else {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		}
	}

	private void dismissProgressDialog() {
		final Fragment dialog = getFragmentManager().findFragmentByTag(
				PROGRESS_DIALOG_TAG);

		if (dialog != null && dialog != progressDialog) {
			// the UI may not yet have resolved the showing of the dialog.
			// use a handler to add the dismiss to the end of the queue.
			handler.post(new Runnable() {
				@Override
				public void run() {
					((ProgressDialogFragment) dialog).dismiss();
				}
			});
		}

		if (progressDialog != null) {
			final ProgressDialogFragment scopedReference = progressDialog;
			progressDialog = null;
			// the UI may not yet have resolved the showing of the dialog.
			// use a handler to add the dismiss to the end of the queue.
			handler.post(new Runnable() {
				@Override
				public void run() {
					try {
						scopedReference.dismiss();
					} catch (Exception e) {
						// ignore... we tried!
					}
				}
			});
		}
	}

	private void showProgressDialog(DialogState state, String message) {
		mDialogState = state;
		mProgressMsg = message;
		restoreProgressDialog();
	}

	private void updateProgressDialogMessage(String message) {
		if (mDialogState == DialogState.ConnectingProgress
				|| mDialogState == DialogState.SendingProgress
				|| mDialogState == DialogState.ReceivingProgress) {
			mProgressMsg = message;
			restoreProgressDialog();
		} else {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		}
	}

	@Override
	public void okAlertDialog() {
		mDialogState = DialogState.None;

	}

	/**
	 * Creates an alert dialog with the given message. If shouldExit is set to
	 * true, the activity will exit when the user clicks "ok".
	 * 
	 * @param title
	 * @param shouldExit
	 */
	private void createAlertDialog(String message) {
		mAlertMsg = message;
		restoreAlertDialog();
	}

	private void restoreAlertDialog() {
		mDialogState = DialogState.None;
		dismissProgressDialog();

		Fragment dialog = getFragmentManager().findFragmentByTag("alertDialog");

		if (dialog != null
				&& ((AlertDialogFragment) dialog).getDialog() != null) {
			mDialogState = DialogState.Alert;
			((AlertDialogFragment) dialog).getDialog().setTitle(
					Survey.getInstance().getString(
							R.string.send_receive_wifi_direct));
			((AlertDialogFragment) dialog).setMessage(mAlertMsg);

		} else {

			AlertDialogFragment f = AlertDialogFragment.newInstance(
					getId(),
					Survey.getInstance().getString(
							R.string.send_receive_wifi_direct), mAlertMsg);

			mDialogState = DialogState.Alert;
			f.show(getFragmentManager(), "alertDialog");
		}
	}

	@Override
	public void cancelProgressDialog() {
		DialogState tempDialogState = mDialogState;
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		if (tempDialogState == DialogState.SendingProgress) {
			f.cancelSendCensusDataTask();
		} else if (tempDialogState == DialogState.ReceivingProgress) {
			f.cancelReceiveCensusDataTask();
		} else if (tempDialogState == DialogState.ConnectingProgress) {
			if (mManager != null && mDevice != null) {

				if (mDevice.status == WifiP2pDevice.CONNECTED) {
					disconnect();
				} else if (mDevice.status == WifiP2pDevice.AVAILABLE
						|| mDevice.status == WifiP2pDevice.INVITED) {
					mManager.cancelConnect(mChannel, new ActionListener() {

						@Override
						public void onSuccess() {
							Toast.makeText(getActivity(),
									"Aborting connection", Toast.LENGTH_SHORT)
									.show();
						}

						@Override
						public void onFailure(int reasonCode) {
							Toast.makeText(
									getActivity(),
									"Connect abort request failed. Reason Code: "
											+ reasonCode, Toast.LENGTH_SHORT)
									.show();
						}
					});
				}
			}
		}
	}

	/**
	 * Array adapter for ListFragment that maintains WifiP2pDevice list.
	 */
	private class DeviceListAdapter extends ArrayAdapter<DeviceInfo> {

		private List<DeviceInfo> items;
		public DisconnectDeviceListener disconnectListener = null;

		/**
		 * @param context
		 * @param resourceId
		 * @param objects
		 */
		public DeviceListAdapter(Context context, int resourceId,
				List<DeviceInfo> objects) {
			super(context, resourceId, objects);
			items = objects;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.device_detail_list_item, null);
			}

			String get = PropertiesSingleton.getProperty("survey",
					AdminPreferencesActivity.KEY_DEVICE_BELONG_TO_TEAM_LEAD);

			DeviceInfo item = items.get(position);
			if (item != null) {
				ImageView disconnectImageView = (ImageView) v
						.findViewById(R.id.disconnectImageView);

				if (get != null && get.equalsIgnoreCase("true")) {
					disconnectImageView.setVisibility(View.VISIBLE);
				} else {
					disconnectImageView.setVisibility(View.GONE);
				}

				disconnectImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (disconnectListener != null) {
							disconnectListener.disconnect();
						}
					}
				});

				TextView totalValid = (TextView) v
						.findViewById(R.id.totalValidTextView);
				TextView totalInvalid = (TextView) v
						.findViewById(R.id.totalInalidTextView);
				TextView totalExcluded = (TextView) v
						.findViewById(R.id.totalExcludedTextView);
				totalValid.setText(getString(R.string.send_receive_valid,
						item.getTotalValid()));
				totalInvalid.setText(getString(R.string.send_receive_invalid,
						item.getTotalInvalid()));
				totalExcluded.setText(getString(R.string.send_receive_excluded,
						item.getTotalExcluded()));

				TextView deviceName = (TextView) v
						.findViewById(R.id.deviceNameTextView);
				TextView deviceStatus = (TextView) v
						.findViewById(R.id.deviceStatusTextView);
				deviceName.setText(item.getHostName());
				deviceStatus
						.setText(WiFiUtils.getDeviceStatus(item.getStatus()));

				TextView received = (TextView) v
						.findViewById(R.id.receivedTextView);
				TextView sent = (TextView) v.findViewById(R.id.sentTextView);
				received.setText(getString(R.string.received,
						item.getReceived()));
				sent.setText(getString(R.string.sent, item.getSent()));
			}

			return v;
		}

		public void setDisconnect(DisconnectDeviceListener value) {
			disconnectListener = value;
		}
	}

	/*
	 * public WifiP2pDevice getConnectedPeerInfo() { for (Object device :
	 * getPeers()) { WifiP2pDevice peer = (WifiP2pDevice) device; if
	 * (peer.status == WifiP2pDevice.CONNECTED) { return peer; } }
	 * 
	 * return null; }
	 */

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		getPeers().clear();
		getPeers().addAll(peerList.getDeviceList());
		int numOfPeers = getPeers().size();

		if (mDeviceBelongToTeamLead == false) {
			mPeersDiscoveredTextView.setText(getActivity().getString(
					R.string.peers_discovered, numOfPeers,
					numOfPeers > 1 ? "s" : ""));
			mSearchingProgressBar.setVisibility(View.GONE);
		}

		if (numOfPeers == 0) {
			if (mDeviceBelongToTeamLead == false) {
				mPeersDiscoveredTextView.setText(getActivity().getString(
						R.string.no_peers_discovered));
			}
			return;
		}
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (mManager != null && !mRetryChannel) {
			mPeersDiscoveredTextView.setText(getActivity().getString(
					R.string.channel_disconnected));
			createAlertDialog(getActivity().getString(
					R.string.channel_disconnected));
			resetData();
			mRetryChannel = true;
			mManager.initialize(getActivity(), getActivity().getMainLooper(),
					this);
		} else {
			mPeersDiscoveredTextView.setText(getActivity().getString(
					R.string.channel_lost));
			createAlertDialog(getActivity().getString(R.string.channel_lost));
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case SendReceiveConstants.MSG_INFO:
			String data = (String) msg.obj;

			DeviceInfo tempDeviceInfo = JSONUtils.getDeviceInfoFromJSON(data);
			if (tempDeviceInfo != null) {
				getConnectedDevices().clear();
				if (mDeviceInfoFromPeer == null) {
					mDeviceInfoFromPeer = tempDeviceInfo;
				} else {
					mDeviceInfoFromPeer.setHostName(tempDeviceInfo
							.getHostName());
					mDeviceInfoFromPeer.setTotalValid(tempDeviceInfo
							.getTotalValid());
					mDeviceInfoFromPeer.setTotalInvalid(tempDeviceInfo
							.getTotalInvalid());
					mDeviceInfoFromPeer.setTotalExcluded(tempDeviceInfo
							.getTotalExcluded());
				}
				getConnectedDevices().add(mDeviceInfoFromPeer);
				((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			}

			break;

		}
		return true;
	}

	public void sendData() {

		if (mRemoteAddress != null && isAdded()) {
			mProgressMsg = getActivity().getString(
					R.string.connecting_with_receiver);
			showProgressDialog(DialogState.SendingProgress, mProgressMsg);

			BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
					.findFragmentByTag("background");

			f.sendCensusData(this, mRemoteAddress);
		} else {
			createAlertDialog(getActivity().getString(
					R.string.peer_address_unknown));
		}
	}

	public void receiveData() {

		if (mRemoteAddress != null && isAdded()) {
			mProgressMsg = getActivity().getString(
					R.string.waiting_sender_to_connect);
			showProgressDialog(DialogState.ReceivingProgress, mProgressMsg);

			BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
					.findFragmentByTag("background");

			f.receiveCensusData(this, mRemoteAddress);
		} else {
			createAlertDialog(getActivity().getString(
					R.string.peer_address_unknown));
		}
	}

	@Override
	public void disconnect() {

		mManager.removeGroup(mChannel, new ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d(t, "Disconnect failed. Reason :" + reasonCode);
			}

			@Override
			public void onSuccess() {
				mRemoteAddress = null;
				mDeviceInfoFromPeer = null;
			}
		});
		mClientDevice = null;
	}

	@Override
	public void sendingComplete(int totalSent) {
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		f.clearSendCensusDataTask();

		if (getConnectedDevices().size() > 0) {
			DeviceInfo deviceInfo = getConnectedDevices().get(0);
			deviceInfo.setSent(totalSent);
			((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			if (mDeviceInfoFromPeer != null)
				mDeviceInfoFromPeer.setSent(totalSent);
		}

		createAlertDialog(getString(R.string.sending_data_completed, totalSent,
				totalSent > 1 ? "s" : ""));
	}

	@Override
	public void sendProgressUpdate(String message) {
		mProgressMsg = message;
		updateProgressDialogMessage(message);
	}

	@Override
	public void receivingComplete(int totalReceived) {
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		f.clearReceiveCensusDataTask();

		if (getConnectedDevices().size() > 0) {
			DeviceInfo deviceInfo = getConnectedDevices().get(0);
			deviceInfo.setReceived(totalReceived);
			((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			if (mDeviceInfoFromPeer != null)
				mDeviceInfoFromPeer.setReceived(totalReceived);
		}

		refreshTotalShortInfo();
		createAlertDialog(getString(R.string.receiving_data_completed,
				totalReceived, totalReceived > 1 ? "s" : ""));
	}

	@Override
	public void receiveProgressUpdate(String message) {
		mProgressMsg = message;
		updateProgressDialogMessage(message);
	}

	@Override
	public void disconnected() {
		mRemoteAddress = null;
	}

	@Override
	public void wifiStateMessage(boolean value) {
		if (value == true) {
			mPeersDiscoveredTextView.setText(getActivity().getString(
					R.string.no_peers_discovered));
		} else {
			mPeersDiscoveredTextView.setText(getActivity().getString(
					R.string.start_peer_discovery));
		}

		mSearchingProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void receivingCanceled(int totalReceived) {
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		f.clearReceiveCensusDataTask();

		if (getConnectedDevices().size() > 0) {
			DeviceInfo deviceInfo = getConnectedDevices().get(0);
			deviceInfo.setReceived(totalReceived);
			((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			if (mDeviceInfoFromPeer != null)
				mDeviceInfoFromPeer.setReceived(totalReceived);
		}

		refreshTotalShortInfo();
		createAlertDialog(getActivity().getString(
				R.string.receiving_canceled_by_user));
	}

	@Override
	public void sendingCanceled(int totalSent) {
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		f.clearSendCensusDataTask();

		if (getConnectedDevices().size() > 0) {
			DeviceInfo deviceInfo = getConnectedDevices().get(0);
			deviceInfo.setSent(totalSent);
			((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			if (mDeviceInfoFromPeer != null)
				mDeviceInfoFromPeer.setSent(totalSent);
		}

		createAlertDialog(getActivity().getString(
				R.string.sending_canceled_by_user));
	}

	@Override
	public void sendError(int totalSent, String message) {
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		f.clearSendCensusDataTask();

		if (getConnectedDevices().size() > 0) {
			DeviceInfo deviceInfo = getConnectedDevices().get(0);
			deviceInfo.setSent(totalSent);
			((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			if (mDeviceInfoFromPeer != null)
				mDeviceInfoFromPeer.setSent(totalSent);
		}

		createAlertDialog(message);
	}

	@Override
	public void receiveError(int totalReceived) {
		try {
			mDialogState = DialogState.None;
			dismissProgressDialog();
		} catch (IllegalArgumentException e) {
			WebLogger
					.getLogger(((ODKActivity) getActivity()).getAppName())
					.i(t,
							"Attempting to close a dialog that was not previously opened");
		}

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");
		f.clearReceiveCensusDataTask();

		if (getConnectedDevices().size() > 0) {
			DeviceInfo deviceInfo = getConnectedDevices().get(0);
			deviceInfo.setReceived(totalReceived);
			((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
			if (mDeviceInfoFromPeer != null)
				mDeviceInfoFromPeer.setReceived(totalReceived);
		}

		refreshTotalShortInfo();
		createAlertDialog(getActivity().getString(R.string.receiving_error));
	}

}
