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

import java.io.File;

import org.path.common.android.database.PlaceNameDatabaseFactory;
import org.path.common.android.database.PlaceNameDatabaseHelper;
import org.path.common.android.utilities.ODKFileUtils;
import org.path.common.android.utilities.PlaceNameUtil;
import org.path.episample.android.R;
import org.path.episample.android.listeners.PlaceNameLoaderListener;
import org.path.episample.android.tasks.LoadPlaceTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author belendia@gmail.com
 */

public class PlaceListActivity extends Activity implements OnClickListener,
		PlaceNameLoaderListener {
	private final static int PROGRESS_DIALOG = 45689;
	private String appName = null;

	private ProgressDialog mProgressDialog;
	private LoadPlaceTask mLoadPlaceTask;

	private TextView mMessageTextView = null;

	private RelativeLayout mLoadButtonContainer = null;
	private RelativeLayout mPurgeButtonContainer = null;

	private ImageButton mLoadImageButton = null;
	private ImageButton mPurgeImageButton = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppName("survey");
		setContentView(R.layout.place_name_dialog);

		mLoadButtonContainer = (RelativeLayout) findViewById(R.id.loadButtonContainer);
		mLoadImageButton = (ImageButton) findViewById(R.id.loadImageButton);

		mPurgeButtonContainer = (RelativeLayout) findViewById(R.id.prugeButtonContainer);
		mPurgeImageButton = (ImageButton) findViewById(R.id.purgeImageButton);

		mLoadPlaceTask = (LoadPlaceTask) getLastNonConfigurationInstance();
		if (mLoadPlaceTask != null) {
			// setup dialog and upload task
			// showDialog(PROGRESS_DIALOG);

			// register this activity with the new loader task
			mLoadPlaceTask.setListener(PlaceListActivity.this);
		} else {
			mMessageTextView = (TextView) findViewById(R.id.messageTextView);

			File db_file = new File(
					ODKFileUtils.getPlaceNameDbFolder(getAppName())
							+ File.separator
							+ PlaceNameDatabaseHelper.PLACE_NAME_DATABASE_NAME);
			if (db_file.exists() == false) {
				File csv_file = new File(
						ODKFileUtils.getPlaceNameCsvFile(getAppName()));
				if (csv_file.exists() == false) {
					mMessageTextView.setText(getString(
							R.string.unable_to_find_config_file,
							ODKFileUtils.getPlaceNameDbFolder(getAppName())));

					disableButtons();
				}
			} else {
				// Count place name list loaded in the database and prepare the
				// message
				// to display accordingly.
				long count = PlaceNameUtil.getCount(this);
				if (count == 0) {
					mMessageTextView.setText(R.string.load_place_list);
				} else if (count == 1) {
					mMessageTextView.setText(R.string.one_place_list);
				} else {
					/*
					 * int resourceId = this.getResources().getIdentifier(
					 * "many_place_list", "string", this.getPackageName());
					 */
					mMessageTextView.setText(getString(
							R.string.many_place_list, String.valueOf(count)));

					// + this.getString(resourceId));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void disableButtons() {
		mLoadButtonContainer.setEnabled(false);
		mLoadImageButton.setEnabled(false);

		mPurgeButtonContainer.setEnabled(false);
		mPurgeImageButton.setEnabled(false);

		mLoadButtonContainer.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.curve_border_disabled_button));
		mPurgeButtonContainer.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.curve_border_disabled_button));

		mLoadImageButton.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.left_border_disabled_part_of_button));
		mPurgeImageButton.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.left_border_disabled_part_of_button));
	}

	@SuppressWarnings("deprecation")
	public void loadClickListener(View v) {
		File csv_file = new File(ODKFileUtils.getPlaceNameCsvFile(getAppName()));
		if (csv_file.exists() == true) {
			disableButtons();

			showDialog(PROGRESS_DIALOG);

			mLoadPlaceTask = new LoadPlaceTask();
			mLoadPlaceTask.setApplication(getApplication());
			mLoadPlaceTask.setAppName(getAppName());
			mLoadPlaceTask.setListener(PlaceListActivity.this);
			mLoadPlaceTask.execute();
		} else {
			mMessageTextView.setText(getString(
					R.string.unable_to_find_config_file,
					ODKFileUtils.getPlaceNameCsvFile(getAppName())));
		}
	}

	public void purgeClickListener(View v) {
		SQLiteDatabase db = PlaceNameDatabaseFactory.get().getDatabase(
				PlaceListActivity.this, getAppName());
		// clear place table
		PlaceNameUtil.delete(db);

		db.close();
		mMessageTextView.setText(R.string.load_place_list);
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
					mLoadPlaceTask.cancel(true);
					mLoadPlaceTask.setListener(null);
					finish();
				}
			};
			mProgressDialog.setTitle(getString(R.string.loading_place));

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
		return mLoadPlaceTask;
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		if (mLoadPlaceTask != null) {
			mLoadPlaceTask.setListener(PlaceListActivity.this);
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mLoadPlaceTask != null) {
			mLoadPlaceTask.setListener(null);
		}
		super.onDestroy();
	}

	@Override
	public void loadingComplete() {
		Toast.makeText(this, getString(R.string.place_list_loaded),
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
