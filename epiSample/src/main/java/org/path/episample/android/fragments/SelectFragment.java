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

import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.fragments.ProgressDialogFragment.CancelProgressDialog;
import org.path.episample.android.listeners.PointSelectionListener;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*
 * @author belendia@gmail.com
 */

public class SelectFragment extends Fragment implements PointSelectionListener,
		CancelProgressDialog, ConfirmAlertDialog {
	private static final String t = "SelectFragment";
	public static final int ID = R.layout.select;
	private View view;

	private static enum DialogState {
		Progress, Alert, None
	};

	private static final String PROGRESS_DIALOG_TAG = "progressDialog";

	// key for the data being retained
	private static final String DIALOG_STATE = "dialogState";
	private static final String DIALOG_MSG = "dialogmsg";
	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;

	private ProgressDialogFragment progressDialog = null;
	private Handler handler = new Handler();
	private TextView mTotalPointsTextView;
	private TextView mValidPointsTextView;

	private int mFixedNoMainPoints;
	private int mFixedNoAdditionalPoints;
	private int mFixedNoAlternatePoints;
	private int mNumberOfValidPoints;

	private EditText mMainPointsEditText;
	private EditText mAdditionalPointsEditText;
	private EditText mAlternatePointsEditText;

	private String mAppName;

	private RelativeLayout selectContainer;
	private ImageButton selectImageButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.select_points);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}
		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(ID, container, false);
		mTotalPointsTextView = (TextView) view
				.findViewById(R.id.totalPointsTextView);
		mValidPointsTextView = (TextView) view
				.findViewById(R.id.validPointsTextView);

		mMainPointsEditText = (EditText) view
				.findViewById(R.id.mainPointsEditText);
		mAdditionalPointsEditText = (EditText) view
				.findViewById(R.id.additionalPointsEditText);
		mAlternatePointsEditText = (EditText) view
				.findViewById(R.id.alternatePointsEditText);

		selectContainer = (RelativeLayout) view
				.findViewById(R.id.selectButtonContainer);
		selectImageButton = (ImageButton) view
				.findViewById(R.id.selectImageButton);

		selectContainer.setOnClickListener(selectPlaceNameClickListener);
		selectImageButton.setOnClickListener(selectPlaceNameClickListener);

		if (savedInstanceState != null) {
			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_STATE)) {
				mDialogState = DialogState.valueOf(savedInstanceState
						.getString(DIALOG_STATE));
			}

			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		refreshTotalShortInfo();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DIALOG_STATE, mDialogState.name());
		outState.putString(DIALOG_MSG, mAlertMsg);
	}

	private void refreshTotalShortInfo() {
		mTotalPointsTextView.setText(String.valueOf(CensusUtil
				.getTotalPointsOtherThanExcluded(getActivity())));
		mNumberOfValidPoints = CensusUtil.getTotalValid(getActivity());
		mValidPointsTextView.setText(String.valueOf(mNumberOfValidPoints));
	}

	private void initializeSelectParameters() {
		boolean freezeEditText = false;
		String get = PropertiesSingleton.getProperty(mAppName,
				AdminPreferencesActivity.KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS);
		if (get != null) {
			try {
				mFixedNoMainPoints = Integer.valueOf(get);
				mMainPointsEditText.setText(get);
				freezeEditText = true;
			} catch (Exception ex) {
				mFixedNoMainPoints = 0;
			}

		} else {
			mFixedNoMainPoints = 0;
		}

		get = PropertiesSingleton
				.getProperty(
						mAppName,
						AdminPreferencesActivity.KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS);
		if (get != null) {
			try {
				mFixedNoAdditionalPoints = Integer.valueOf(get);
				mAdditionalPointsEditText.setText(get);
				freezeEditText = true;
			} catch (Exception ex) {
				mFixedNoAdditionalPoints = 0;
			}

		} else {
			mFixedNoAdditionalPoints = 0;
		}

		get = PropertiesSingleton
				.getProperty(
						mAppName,
						AdminPreferencesActivity.KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS);
		if (get != null) {
			try {
				mFixedNoAlternatePoints = Integer.valueOf(get);
				mAlternatePointsEditText.setText(get);
				freezeEditText = true;
			} catch (Exception ex) {
				mFixedNoAlternatePoints = 0;
			}

		} else {
			mFixedNoAlternatePoints = 0;
		}

		if (freezeEditText == true) {
			freezeEditText();
		}
	}

	private void freezeEditText() {
		mMainPointsEditText.setFocusable(false);
		mAdditionalPointsEditText.setFocusable(false);
		mAlternatePointsEditText.setFocusable(false);

		Drawable disabledBackground = Survey.getInstance().getResources()
				.getDrawable(R.drawable.disabled_round_border);
		mMainPointsEditText.setBackgroundDrawable(disabledBackground);
		mAdditionalPointsEditText.setBackgroundDrawable(disabledBackground);
		mAlternatePointsEditText.setBackgroundDrawable(disabledBackground);

		mMainPointsEditText.setOnClickListener(pointSettingClickListener);
		mAdditionalPointsEditText.setOnClickListener(pointSettingClickListener);
		mAlternatePointsEditText.setOnClickListener(pointSettingClickListener);
	}

	private OnClickListener pointSettingClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			createAlertDialog(getString(R.string.sampling_points_fixed));
		}
	};

	private boolean validateUserInput() {
		if (mMainPointsEditText.getText().toString().trim().length() == 0) {
			createAlertDialog(getString(R.string.main_point_numeric));
			mMainPointsEditText.requestFocus();
			return false;
		} else if (mNumberOfValidPoints == 0) {
			createAlertDialog(getString(R.string.no_valid_data_points_to_select));
			return false;
		}

		return true;
	}

	private void getUserInput() {
		try {
			mFixedNoMainPoints = Integer.valueOf(mMainPointsEditText.getText()
					.toString());
		} catch (Exception ex) {
			mFixedNoMainPoints = 0;
		}

		try {
			mFixedNoAdditionalPoints = Integer
					.valueOf(mAdditionalPointsEditText.getText().toString());
		} catch (Exception ex) {
			mFixedNoAdditionalPoints = 0;
		}

		try {
			mFixedNoAlternatePoints = Integer.valueOf(mAlternatePointsEditText
					.getText().toString());
		} catch (Exception ex) {
			mFixedNoAlternatePoints = 0;
		}
	}

	private OnClickListener selectPlaceNameClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (validateUserInput()) {
				getUserInput();
				int totalPointsToSelect = mFixedNoMainPoints
						+ mFixedNoAdditionalPoints + mFixedNoAlternatePoints;

				if (totalPointsToSelect > mNumberOfValidPoints
						|| totalPointsToSelect == 0) {
					createAlertDialog(getString(R.string.enter_valid_number_of_points));
					mMainPointsEditText.requestFocus();
				} else {
					selectPoints();
				}
			}
		}
	};

	public String getAppName() {
		return mAppName;
	}

	@Override
	public void onResume() {
		super.onResume();
		initializeSelectParameters();

		if (mDialogState == DialogState.Progress) {
			restoreProgressDialog();
		}
	}

	@Override
	public void onPause() {

		dismissProgressDialog();
		super.onPause();
	}

	private void selectPoints() {
		showProgressDialog();

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");

		f.selectPoints(getAppName(), this, mFixedNoMainPoints,
				mFixedNoAdditionalPoints, mFixedNoAlternatePoints);
	}

	private void restoreProgressDialog() {
		if (mDialogState == DialogState.Progress) {
			Fragment dialog = getFragmentManager().findFragmentByTag(
					PROGRESS_DIALOG_TAG);

			if (dialog != null
					&& ((ProgressDialogFragment) dialog).getDialog() != null) {
				((ProgressDialogFragment) dialog).getDialog().setTitle(
						Survey.getInstance().getString(R.string.select_points));
				((ProgressDialogFragment) dialog).setMessage(Survey
						.getInstance().getString(R.string.please_wait));
			} else if (progressDialog != null
					&& progressDialog.getDialog() != null) {
				progressDialog.getDialog().setTitle(
						Survey.getInstance().getString(R.string.select_points));
				progressDialog.setMessage(Survey.getInstance().getString(
						R.string.please_wait));
			} else {
				if (progressDialog != null) {
					dismissProgressDialog();
				}
				progressDialog = ProgressDialogFragment.newInstance(getId(),
						Survey.getInstance().getString(R.string.select_points),
						Survey.getInstance().getString(R.string.please_wait));
				progressDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
			}
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

	private void showProgressDialog() {
		mDialogState = DialogState.Progress;
		restoreProgressDialog();
	}

	@Override
	public void selectionComplete(boolean success) {
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
		f.clearSelectPointsTask();

		if (success == true) {
			createAlertDialog(getString(R.string.selected_successfully));
		} else {
			createAlertDialog(getString(R.string.unable_to_select_points));
		}
	}

	@Override
	public void cancelProgressDialog() {
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
		f.cancelSelectPointsTask();
	}

	private void restoreAlertDialog() {
		mDialogState = DialogState.None;
		dismissProgressDialog();

		Fragment dialog = getFragmentManager().findFragmentByTag("alertDialog");

		if (dialog != null
				&& ((AlertDialogFragment) dialog).getDialog() != null) {
			mDialogState = DialogState.Alert;
			((AlertDialogFragment) dialog).getDialog().setTitle(
					Survey.getInstance().getString(R.string.select_points));
			((AlertDialogFragment) dialog).setMessage(mAlertMsg);

		} else {

			AlertDialogFragment f = AlertDialogFragment.newInstance(getId(),
					Survey.getInstance().getString(R.string.select_points),
					mAlertMsg);

			mDialogState = DialogState.Alert;
			f.show(getFragmentManager(), "alertDialog");
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

	@Override
	public void selectionCanceled() {
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
		f.clearSelectPointsTask();

		createAlertDialog(getString(R.string.selection_canceled));
	}
}
