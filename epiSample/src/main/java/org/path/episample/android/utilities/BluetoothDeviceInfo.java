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

package org.path.episample.android.utilities;

/*
 * @author belendia@gmail.com
 */

public class BluetoothDeviceInfo {
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String value) {
		name = value;
	}
	
	private String mac;
	public String getMAC() {
		return mac;
	}
	public void setMAC(String value) {
		mac = value;
	}
	
	private boolean isPaired = false;
	public boolean getDecivePaired() {
		return isPaired;
	}
	public void setDevicePaired(boolean value) {
		isPaired = value;
	}
	
}