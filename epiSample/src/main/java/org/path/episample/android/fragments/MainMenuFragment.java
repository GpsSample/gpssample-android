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
import org.path.episample.android.listeners.RemoveCensusDataFromPreviousDateListener;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;
import org.path.episample.android.preferences.PreferencesActivity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Fragment displaying them main modules in the app.
 * 
 * @author belendia@gmail.com
 * 
 */
public class MainMenuFragment extends Fragment implements ConfirmAlertDialog,
		CancelProgressDialog, RemoveCensusDataFromPreviousDateListener {

	@SuppressWarnings("unused")
	private static final String t = "MainMenuFragment";

	public static final int ID = R.layout.main_menu;
	private static final String DIALOG_MSG = "dialogmsg";
	private static final String PROGRESS_DIALOG_TAG = "progressDialog";
	private ProgressDialogFragment progressDialog = null;
	private Handler handler = new Handler();

	private View view;
	private View mCollectContainer;
	private View mSendReceiveContainer;
	private View mSelectContainer;
	private View mNavigateContainer;
	private View mSendFinalizedFormContainer;

	private static boolean mPreviousDateCensusDataChecked = false;
	private boolean mCleanDataFromPreviousDate = false;

	private String mAlertMsg;
	private String mInterviewerName;
	private String mDeviceName;
	private String mPhoneNumber;

	private ImageView mLogoImageView;

	private static enum DialogState {
		Progress, Alert, None
	};

	private DialogState mDialogState = DialogState.None;
	AlertDialog mAlert;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(ID, container, false);

		mCollectContainer = view.findViewById(R.id.collectContainer);
		mCollectContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				((ODKActivity) getActivity())
						.swapToFragmentView(MainMenuActivity.ScreenList.COLLECT_MODULE);
				/*
				 * Intent intent = new Intent(getActivity(),
				 * CollectFragment.class); startActivity(intent);
				 */
			}
		});

		mSendReceiveContainer = view.findViewById(R.id.sendReceiveContainer);
		mSendReceiveContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				((ODKActivity) getActivity())
						.swapToFragmentView(MainMenuActivity.ScreenList.SEND_RECEIVE_WIFI_DIRECT_MODULE);
			}
		});

		mSelectContainer = view.findViewById(R.id.selectContainer);
		mSelectContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((ODKActivity) getActivity())
						.swapToFragmentView(MainMenuActivity.ScreenList.SELECT_MODULE);
				// Intent intent = new Intent(getActivity(),
				// SelectFragment.class);
				// startActivity(intent);
			}
		});

		mNavigateContainer = view.findViewById(R.id.navigateContainer);
		mNavigateContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((ODKActivity) getActivity())
						.swapToFragmentView(MainMenuActivity.ScreenList.NAVIGATE_MODULE);
			}
		});

		mSendFinalizedFormContainer = view
				.findViewById(R.id.sendFinalizedFormContainer);
		mSendFinalizedFormContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((ODKActivity) getActivity())
						.swapToFragmentView(MainMenuActivity.ScreenList.INSTANCE_UPLOADER_TABLE_CHOOSER);
			}
		});

		mLogoImageView = (ImageView) view.findViewById(R.id.logo_info);
		mLogoImageView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				StringBuilder builder = new StringBuilder();
				builder.append(getActivity().getString(
						R.string.interviewer_name)
						+ " = " + mInterviewerName);
				builder.append("\n");
				builder.append(getActivity().getString(R.string.phone_number)
						+ " = " + mPhoneNumber);
				builder.append("\n");
				builder.append(getActivity().getString(R.string.device_name)
						+ " = " + mDeviceName);
				builder.append("\n");

				createAlertDialog(builder.toString());

				return false;
			}
		});

		if (savedInstanceState != null) {
			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.app_name);
		showHideModules();

		if (mDialogState == DialogState.Progress) {
			restoreProgressDialog();
		}

		String get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_CLEAN_DATA_FROM_PREVIOUS_DATE);
		if (get != null && get.equalsIgnoreCase("true")) {
			mCleanDataFromPreviousDate = true;
		} else {
			mCleanDataFromPreviousDate = false;
		}

		mInterviewerName = "";
		get = PropertiesSingleton.getProperty("survey",
				PreferencesActivity.KEY_INTERVIEWER_NAME);
		if (get != null) {
			mInterviewerName = get;
		} else {
			mInterviewerName = getActivity().getString(R.string.not_set);
		}

		mDeviceName = "";
		get = PropertiesSingleton.getProperty("survey",
				PreferencesActivity.KEY_DEVICE_NAME);
		if (get != null) {
			mDeviceName = get;
		} else {
			mDeviceName = getActivity().getString(R.string.not_set);
		}

		mPhoneNumber = "";
		get = PropertiesSingleton.getProperty("survey",
				PreferencesActivity.KEY_PHONE_NUMBER);
		if (get != null) {
			mPhoneNumber = get;
		} else {
			mPhoneNumber = getActivity().getString(R.string.not_set);
		}

		if (mCleanDataFromPreviousDate == true
				&& mPreviousDateCensusDataChecked == false) {
			if (CensusUtil.getCountOfCensusFromPreviousDate(getActivity()) > 0) {
				confirmCleanData4PreviousDate();
				mPreviousDateCensusDataChecked = true;
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(DIALOG_MSG, mAlertMsg);
	}

	@Override
	public void onDestroy() {
		if (mAlert != null) {
			if (mAlert.isShowing()) {
				mAlert.dismiss();
			}
		}

		super.onDestroy();
	}

	private void showHideModules() {
		String get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_COLLECT);
		if (get != null && get.equalsIgnoreCase("false")) {
			mCollectContainer.setVisibility(View.GONE);
		} else {
			mCollectContainer.setVisibility(View.VISIBLE);
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_SEND_RECEIVE);
		if (get != null && get.equalsIgnoreCase("false")) {
			mSendReceiveContainer.setVisibility(View.GONE);
		} else {
			mSendReceiveContainer.setVisibility(View.VISIBLE);
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_SELECT);
		if (get != null && get.equalsIgnoreCase("false")) {
			mSelectContainer.setVisibility(View.GONE);
		} else {
			mSelectContainer.setVisibility(View.VISIBLE);
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_NAVIGATE);
		if (get != null && get.equalsIgnoreCase("false")) {
			mNavigateContainer.setVisibility(View.GONE);

		} else {
			mNavigateContainer.setVisibility(View.VISIBLE);
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_SEND_FINALIZED);
		if (get != null && get.equalsIgnoreCase("false")) {
			mSendFinalizedFormContainer.setVisibility(View.GONE);

		} else {
			mSendFinalizedFormContainer.setVisibility(View.VISIBLE);
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

		Fragment dialog = getFragmentManager().findFragmentByTag("alertDialog");

		if (dialog != null
				&& ((AlertDialogFragment) dialog).getDialog() != null) {
			mDialogState = DialogState.Alert;
			((AlertDialogFragment) dialog).getDialog().setTitle(
					Survey.getInstance().getString(R.string.collect_data));
			((AlertDialogFragment) dialog).setMessage(mAlertMsg);

		} else {

			AlertDialogFragment f = AlertDialogFragment.newInstance(getId(),
					Survey.getInstance().getString(R.string.app_name),
					mAlertMsg);

			mDialogState = DialogState.Alert;
			f.show(getFragmentManager(), "alertDialog");
		}
	}

	private void confirmCleanData4PreviousDate() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.app_name))
				.setMessage(getString(R.string.confirm_clean_database))
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								removeCensusFromPreviousDate();
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	@Override
	public void removeCensusFromPreviousDateComplete(boolean success,
			long numOfRowsAffected) {
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
		f.clearRemoveAllCensusTask();

		if (numOfRowsAffected > 0) {
			createAlertDialog(getActivity().getString(R.string.points_removed,
					numOfRowsAffected, numOfRowsAffected > 1 ? "s" : ""));
		} else {
			createAlertDialog(getActivity().getString(
					R.string.points_not_deleted));
		}
	}

	@Override
	public void removeCensusFromPreviousDateCanceled() {
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
		f.clearRemoveCensusByDateTask();

		createAlertDialog(getActivity().getString(
				R.string.removing_census_canceled_by_user));
	}

	@Override
	public void removeCensusFromPreviousDateProgressUpdate(String message) {

	}

	public void removeCensusFromPreviousDate() {
		showProgressDialog(DialogState.Progress);
		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");

		f.removeCensusFromPreviousDate(this);
	}

	private void showProgressDialog(DialogState state) {
		mDialogState = state;
		restoreProgressDialog();
	}

	private void restoreProgressDialog() {
		if (mDialogState == DialogState.Progress) {
			Fragment dialog = getFragmentManager().findFragmentByTag(
					PROGRESS_DIALOG_TAG);

			if (dialog != null
					&& ((ProgressDialogFragment) dialog).getDialog() != null) {
				((ProgressDialogFragment) dialog).getDialog().setTitle(
						Survey.getInstance().getString(R.string.app_name));
				((ProgressDialogFragment) dialog).setMessage(Survey
						.getInstance().getString(R.string.please_wait));
			} else if (progressDialog != null
					&& progressDialog.getDialog() != null) {
				progressDialog.getDialog().setTitle(
						Survey.getInstance().getString(R.string.app_name));
				progressDialog.setMessage(Survey.getInstance().getString(
						R.string.please_wait));
			} else {
				if (progressDialog != null) {
					dismissProgressDialog();
				}
				progressDialog = ProgressDialogFragment.newInstance(getId(),
						Survey.getInstance().getString(R.string.app_name),
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
		f.cancelRemoveCensusDataFromPreviousDateTask();

	}
}
