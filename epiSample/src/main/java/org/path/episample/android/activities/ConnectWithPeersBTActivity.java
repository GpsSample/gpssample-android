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

import java.util.ArrayList;
import java.util.List;

import org.path.episample.android.R;
import org.path.episample.android.utilities.BluetoothDeviceInfo;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

public class ConnectWithPeersBTActivity extends ListActivity {

	private String appName = null;
	
	private ImageView mConnectionFoundImageView;
	private LinearLayout mLoadingLinearLayout;
	private TextView mEmptyTextView;
	private TextView mDiscoverPeersTextView;
	
	// Member fields
    private BluetoothAdapter mBtAdapter;
    private List<BluetoothDeviceInfo> mBluetoothDevices;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppName("survey");
		setContentView(R.layout.connect_with_peers_dialog);
		
		mConnectionFoundImageView = (ImageView) findViewById(R.id.connectionFoundImageView);
		mLoadingLinearLayout = (LinearLayout) findViewById(R.id.loadingLinearLayout);
		mEmptyTextView = (TextView) findViewById(android.R.id.empty);
		mDiscoverPeersTextView = (TextView) findViewById(R.id.discoverPeersTextView);
		
		mDiscoverPeersTextView.setText(R.string.discover);
		 mBluetoothDevices = new ArrayList<BluetoothDeviceInfo>();
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setListAdapter(new PeerListAdapter(ConnectWithPeersBTActivity.this, R.layout.device_list_item, mBluetoothDevices));
	}

	public void startDiscovery() {
		// If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
        
        mLoadingLinearLayout.setVisibility(View.VISIBLE);
		mConnectionFoundImageView.setVisibility(View.VISIBLE);
		mEmptyTextView.setVisibility(View.GONE);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }
	
	@Override
	 public void onListItemClick(ListView l, View v, int position, long id) {
		if(mBluetoothDevices.size() >= 0) {
			BluetoothDeviceInfo selectedDevice = mBluetoothDevices.get(position);
			Intent returnIntent = new Intent();
			returnIntent.putExtra("selectedDevice", selectedDevice.getMAC());

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
	
	/**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class PeerListAdapter extends ArrayAdapter<BluetoothDeviceInfo> {

        private List<BluetoothDeviceInfo> mItems;
        private Context mContext;
        /**
         * @param context
         * @param resourceId
         * @param objects
         */
        public PeerListAdapter(Context context, int resourceId,
                List<BluetoothDeviceInfo> objects) {
            super(context, resourceId, objects);
            mItems = objects;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.device_list_item, null);
            }
            BluetoothDeviceInfo device = mItems.get(position);
            if (device != null) {
                TextView deviceName = (TextView) v.findViewById(R.id.deviceNameTextView);
                TextView deviceStatus = (TextView) v.findViewById(R.id.deviceStatusTextView);
                deviceName.setText(device.getName());  
                if(device.getDecivePaired()) {
                	deviceStatus.setText(mContext.getString(R.string.paired));
                } else {
                	deviceStatus.setText("");
                }
            }
            
            return v;
        }
    }
    
 // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            	mConnectionFoundImageView.setVisibility(View.VISIBLE);
            	mLoadingLinearLayout.setVisibility(View.GONE);
            	mEmptyTextView.setVisibility(View.VISIBLE);
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothDeviceInfo info = new BluetoothDeviceInfo(); 
                info.setName(device.getName());
                info.setMAC(device.getAddress());
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                   info.setDevicePaired(true);
                } else {
                	info.setDevicePaired(false);
                }
                
                int index = isDeviceAdded(info);
                if(index >-1) {
                	mBluetoothDevices.remove(index);
                	mBluetoothDevices.add(info);
                } else {
                	mBluetoothDevices.add(info);
                }
                
                ((PeerListAdapter) getListAdapter()).notifyDataSetChanged();
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mBluetoothDevices.size() == 0) {
                	mEmptyTextView.setText(getString(R.string.no_devices_found));            		
            		mEmptyTextView.setVisibility(View.VISIBLE);
            		mLoadingLinearLayout.setVisibility(View.GONE);
            		mConnectionFoundImageView.setVisibility(View.GONE);
                }
            }
        }
    };
    
    private int isDeviceAdded(BluetoothDeviceInfo deviceInfo) {
    	int index = -1;
    	for(int i=0; i< mBluetoothDevices.size(); i++) {
    		if(mBluetoothDevices.get(i).getMAC().equals(deviceInfo.getMAC())) {
    			index = i;
    			break;
    		}
    	}
    	
    	return index;
    }
    
    public void rediscoverPeersClickListener(View view) {
    	startDiscovery();
    }
}
