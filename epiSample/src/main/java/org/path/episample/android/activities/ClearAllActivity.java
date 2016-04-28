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

import org.path.common.android.utilities.CensusUtil;
import org.path.episample.android.R;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.ClearAllCensusListener;
import org.path.episample.android.tasks.ClearAllCensusTask;
import org.path.episample.android.utilities.BackupRestoreUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author belendia@gmail.com
 */

public class ClearAllActivity extends Activity implements OnClickListener,
		ClearAllCensusListener {
	private final static int PROGRESS_DIALOG = 47783;
	private String appName = null;

	private ProgressDialog mProgressDialog;
	private ClearAllCensusTask mClearAllCensusTask;

	private TextView mMessageTextView = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppName("survey");
		setContentView(R.layout.clear_all_dialog);

		mClearAllCensusTask = (ClearAllCensusTask) getLastNonConfigurationInstance();
		if (mClearAllCensusTask != null) {
			// setup dialog and upload task
			// showDialog(PROGRESS_DIALOG);

			// register this activity with the new loader task
			mClearAllCensusTask.setListener(ClearAllActivity.this);
		} else {
			mMessageTextView = (TextView) findViewById(R.id.messageTextView);
			mMessageTextView.setText(getString(R.string.census_exist,
					CensusUtil.getCount(this)));
		}
	}

	@SuppressWarnings("deprecation")
	public void clearAllClickListener(View v) {
		showDialog(PROGRESS_DIALOG);

		BackupRestoreUtils.backupCensus(Survey.getInstance()
				.getApplicationContext(), "survey");
		mClearAllCensusTask = new ClearAllCensusTask();
		mClearAllCensusTask.setApplication(getApplication());
		mClearAllCensusTask.setAppName(getAppName());
		mClearAllCensusTask.setListener(ClearAllActivity.this);
		mClearAllCensusTask.execute();
	}

	public void changePasswordClickListener(View v) {
		Intent intent = new Intent(ClearAllActivity.this,
				ClearAllCensusPasswordDialogActivity.class);
		startActivity(intent);
	}

	public void exitClickListener(View v) {
		finish();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:

			mProgressDialog = new ProgressDialog(this);
			DialogInterface.OnClickListener loadingButtonListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mClearAllCensusTask.cancel(true);
					mClearAllCensusTask.setListener(null);
					finish();
				}
			};
			mProgressDialog
					.setTitle(getString(R.string.clearing_census_database));

			int resourceId = this.getResources().getIdentifier("please_wait",
					"string", this.getPackageName());
			mProgressDialog.setMessage(this.getString(resourceId));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(getString(R.string.cancel),
					loadingButtonListener);
			return mProgressDialog;
		}
		return null;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mClearAllCensusTask;
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		if (mClearAllCensusTask != null) {
			mClearAllCensusTask.setListener(ClearAllActivity.this);
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mClearAllCensusTask != null) {
			mClearAllCensusTask.setListener(null);
		}
		super.onDestroy();
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return this.appName;
	}

	@Override
	public void complete() {
		Toast.makeText(this, getString(R.string.census_database_cleared),
				Toast.LENGTH_LONG).show();
		finish();

	}
}
