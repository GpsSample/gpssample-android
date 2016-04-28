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

import org.path.episample.android.R;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

/*
 * @author belendia@gmail.com
 */

public class ClearAllCensusPasswordDialogActivity extends Activity implements
		OnClickListener {

	private static final String t = "ClearAllCensusPasswordDialogActivity";

	public static String appName;

	private EditText passwordEditText;
	private EditText verifyEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppName("survey");

		setContentView(R.layout.clear_all_data_password_dialog_layout);

		passwordEditText = (EditText) findViewById(R.id.pwd_field);
		verifyEditText = (EditText) findViewById(R.id.verify_field);

		String clearAllCensusPW = "";
		if (PropertiesSingleton.getProperty(appName,
				AdminPreferencesActivity.KEY_CLEAR_ALL_CENSUS_PW) != null) {
			clearAllCensusPW = PropertiesSingleton.getProperty(appName,
					AdminPreferencesActivity.KEY_CLEAR_ALL_CENSUS_PW);
		}

		// populate the fields if a pw exists
		if (!clearAllCensusPW.equalsIgnoreCase("")) {
			passwordEditText.setText(clearAllCensusPW);
			passwordEditText.setSelection(passwordEditText.getText().length());
			verifyEditText.setText(clearAllCensusPW);
		}
	}

	public void okClickListener(View view) {
		String pw = passwordEditText.getText().toString();
		String ver = verifyEditText.getText().toString();

		if (!pw.equalsIgnoreCase("") && !ver.equalsIgnoreCase("")
				&& pw.equals(ver)) {
			// passwords are the same
			PropertiesSingleton.setProperty(appName,
					AdminPreferencesActivity.KEY_CLEAR_ALL_CENSUS_PW, pw);
			PropertiesSingleton.writeProperties(appName);

			Toast.makeText(ClearAllCensusPasswordDialogActivity.this,
					R.string.clear_all_census_password_changed,
					Toast.LENGTH_SHORT).show();
			finish();
		} else if (pw.equalsIgnoreCase("") && ver.equalsIgnoreCase("")) {
			PropertiesSingleton.setProperty(appName,
					AdminPreferencesActivity.KEY_CLEAR_ALL_CENSUS_PW, "");
			Toast.makeText(ClearAllCensusPasswordDialogActivity.this,
					R.string.clear_all_census_password_disabled,
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			Toast.makeText(ClearAllCensusPasswordDialogActivity.this,
					R.string.clear_all_census_password_mismatch,
					Toast.LENGTH_SHORT).show();
		}
	}

	public void cancelClickListener(View view) {
		finish();
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
}
