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
package org.path.episample.android.preferences;

import java.io.File;

import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.ODKFileUtils;
import org.path.episample.android.R;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.LoadPointsListener;
import org.path.episample.android.tasks.LoadPointsTask;
import org.path.episample.android.utilities.BackupRestoreUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author belendia@gmail.com
 */

public class LoadPointsActivity extends Activity implements OnClickListener,
		LoadPointsListener {
	private final static int PROGRESS_DIALOG = 45689;
	private String appName = null;

	private ProgressDialog mProgressDialog;
	private LoadPointsTask mLoadPointsTask;

	private TextView mMessageTextView = null;

	private RelativeLayout mLoadButtonContainer = null;

	private ImageButton mLoadImageButton = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppName("survey");
		setContentView(R.layout.load_points_dialog);

		mLoadButtonContainer = (RelativeLayout) findViewById(R.id.loadButtonContainer);
		mLoadImageButton = (ImageButton) findViewById(R.id.loadImageButton);

		mLoadPointsTask = (LoadPointsTask) getLastNonConfigurationInstance();
		if (mLoadPointsTask != null) {
			// setup dialog and upload task
			// showDialog(PROGRESS_DIALOG);

			// register this activity with the new loader task
			mLoadPointsTask.setListener(LoadPointsActivity.this);
		} else {
			mMessageTextView = (TextView) findViewById(R.id.messageTextView);

			File importFile = new File(
					ODKFileUtils.getPointCsvFile(getAppName()));
			if (importFile.exists() == false) {
				mMessageTextView.setText(getString(
						R.string.unable_to_find_import_file,
						ODKFileUtils.getPointCsvFile(getAppName())));

				disableButtons();
			} else {
				// Count censuses loaded in the database and prepare the
				// message
				// to display accordingly.
				long count = CensusUtil.getCount(this);
				if (count == 0) {
					mMessageTextView.setText(R.string.load_points);
				} else if (count == 1) {
					mMessageTextView.setText(R.string.one_point);
				} else {
					mMessageTextView.setText(getString(R.string.many_points,
							String.valueOf(count)));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void disableButtons() {
		mLoadButtonContainer.setEnabled(false);
		mLoadImageButton.setEnabled(false);

		mLoadButtonContainer.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.curve_border_disabled_button));
		mLoadImageButton.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.left_border_disabled_part_of_button));
	}

	@SuppressWarnings("deprecation")
	public void loadClickListener(View v) {
		File importFile = new File(ODKFileUtils.getPointCsvFile(getAppName()));
		if (importFile.exists() == true) {
			disableButtons();

			BackupRestoreUtils.backupCensus(Survey.getInstance()
					.getApplicationContext(), "survey");
			showDialog(PROGRESS_DIALOG);

			mLoadPointsTask = new LoadPointsTask();
			mLoadPointsTask.setApplication(getApplication());
			mLoadPointsTask.setAppName(getAppName());
			mLoadPointsTask.setListener(LoadPointsActivity.this);
			mLoadPointsTask.execute();
		} else {
			mMessageTextView.setText(getString(
					R.string.unable_to_find_import_file,
					ODKFileUtils.getPointCsvFile(getAppName())));
		}
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
					mLoadPointsTask.cancel(true);
					mLoadPointsTask.setListener(null);
					finish();
				}
			};
			mProgressDialog.setTitle(getString(R.string.loading_points));

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
		return mLoadPointsTask;
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		if (mLoadPointsTask != null) {
			mLoadPointsTask.setListener(LoadPointsActivity.this);
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mLoadPointsTask != null) {
			mLoadPointsTask.setListener(null);
		}
		super.onDestroy();
	}

	@Override
	public void loadingComplete() {
		Toast.makeText(this, getString(R.string.points_loaded_successfully),
				Toast.LENGTH_LONG).show();
		finish();
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return this.appName;
	}
}
