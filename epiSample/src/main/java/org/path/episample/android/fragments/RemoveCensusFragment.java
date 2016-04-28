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

import java.util.ArrayList;
import java.util.List;

import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.fragments.ProgressDialogFragment.CancelProgressDialog;
import org.path.episample.android.listeners.RemoveAllCensusListener;
import org.path.episample.android.listeners.RemoveCensusByDateListener;
import org.path.episample.android.tasks.CensusListByDateLoader;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Fragment displays distinct created date of census database
 * 
 * @author belendia@gmail.com
 * 
 */
public class RemoveCensusFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<String>>, ConfirmAlertDialog,
		CancelProgressDialog, RemoveCensusByDateListener,
		RemoveAllCensusListener {

	private static final String t = "RemoveFragment";

	private static final int CENSUS_LIST_BY_DATE_LOADER = 0x69;

	public static final int ID = R.layout.remove;
	private View mView;

	private String mAppName;

	private CensusListByDateAdapter mInstances;
	private TextView mEmptyTextView;
	private TextView mNumOfRecordsByDateTextView;
	private static final String DIALOG_MSG = "dialogmsg";
	private static final String SELECTED = "selected";
	private static final String PROGRESS_DIALOG_TAG = "progressDialog";

	private ProgressDialogFragment progressDialog = null;
	private Handler handler = new Handler();
	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;
	private AlertDialog mAlert;

	private static enum DialogState {
		RemoveAllProgress, RemoveSelectedProgress, Alert, None
	};

	private RelativeLayout removeSelectedButtonContainer;
	private RelativeLayout removeAllButtonContainer;
	private ImageButton removeSelectedImageButton;
	private ImageButton removeAllImageButton;

	private ArrayList<String> mSelected = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.remove_census);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}

		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}

			if (savedInstanceState.containsKey(SELECTED)) {
				mSelected = savedInstanceState.getStringArrayList(SELECTED);
			}
		}

		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(ID, container, false);
		mEmptyTextView = (TextView) mView.findViewById(android.R.id.empty);
		mNumOfRecordsByDateTextView = (TextView) mView
				.findViewById(R.id.numOfRecordsByDateTextView);
		removeSelectedButtonContainer = (RelativeLayout) mView
				.findViewById(R.id.removeSelectedButtonContainer);
		removeSelectedImageButton = (ImageButton) mView
				.findViewById(R.id.removeSelectedImageButton);
		removeAllButtonContainer = (RelativeLayout) mView
				.findViewById(R.id.removeAllButtonContainer);
		removeAllImageButton = (ImageButton) mView
				.findViewById(R.id.removeAllImageButton);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		removeSelectedButtonContainer
				.setOnClickListener(removeSelectedClickListener);
		removeSelectedImageButton
				.setOnClickListener(removeSelectedClickListener);

		removeAllButtonContainer.setOnClickListener(removeAllClickListener);
		removeAllImageButton.setOnClickListener(removeAllClickListener);

		mInstances = new CensusListByDateAdapter(getActivity());
		setListAdapter(mInstances);
		getListView().setEmptyView(mEmptyTextView);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		getLoaderManager().initLoader(CENSUS_LIST_BY_DATE_LOADER, null, this);

		registerForContextMenu(getListView());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(DIALOG_MSG, mAlertMsg);
		outState.putStringArrayList(SELECTED, mSelected);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDialogState == DialogState.RemoveAllProgress
				|| mDialogState == DialogState.RemoveSelectedProgress) {
			restoreProgressDialog();
		}

		ListView ls = getListView();
		for (String row : mSelected) {
			for (int pos = 0; pos < ls.getCount(); pos++) {
				String censusByDateRow = (String) ls.getItemAtPosition(pos);
				if (row.equals(censusByDateRow)) {
					ls.setItemChecked(pos, true);
					break;
				}
			}
		}
	}

	@Override
	public void onPause() {
		dismissProgressDialog();
		super.onPause();
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

	@Override
	public Loader<List<String>> onCreateLoader(int arg0, Bundle args) {
		return new CensusListByDateLoader(getActivity(),
				((ODKActivity) getActivity()).getAppName());
	}

	@Override
	public void onLoadFinished(Loader<List<String>> loader, List<String> cursor) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mInstances.clear();
		mInstances.addAll(cursor);
		mNumOfRecordsByDateTextView.setText(getActivity().getString(
				R.string.num_of_rows, cursor.size(),
				(cursor.size() > 1 ? "s" : "")));
	}

	@Override
	public void onLoaderReset(Loader<List<String>> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mInstances.clear();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String selectedRow = (String) getListAdapter().getItem(position);

		if (mSelected.contains(selectedRow))
			mSelected.remove(selectedRow);
		else
			mSelected.add(selectedRow);
	}

	private OnClickListener removeSelectedClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (mSelected != null && mSelected.size() > 0) {
				confirmSelectedAndRemove();
			} else {
				createAlertDialog(getActivity().getString(
						R.string.select_date_to_remove));
			}
		}
	};

	private OnClickListener removeAllClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			confirmRemoveAll();
		}
	};

	private void confirmSelectedAndRemove() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.remove))
				.setMessage(getString(R.string.confirm_remove_points))
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeSelected();
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	private void confirmRemoveAll() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.remove))
				.setMessage(getString(R.string.confirm_remove_all))
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeAll();
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	public String getAppName() {
		return mAppName;
	}

	private void removeSelected() {
		showProgressDialog(DialogState.RemoveSelectedProgress);
		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");

		f.removeCensusByDate(this, mSelected);
	}

	private static class CensusListByDateAdapter extends ArrayAdapter<String> {
		private final LayoutInflater mInflater;

		public CensusListByDateAdapter(Context context) {
			super(context, R.layout.remove);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/**
		 * Populate items in the list.
		 */
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view;
			ViewHolder viewHolder;

			view = convertView;

			if (view == null) {
				view = mInflater.inflate(R.layout.remove_census_item, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.dateTextView = (TextView) view
						.findViewById(R.id.dateTextView);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			String date = getItem(position);
			viewHolder.dateTextView.setText(date);

			return view;
		}
	}

	private static class ViewHolder {
		TextView dateTextView;
	}

	private void restoreAlertDialog() {
		mDialogState = DialogState.None;

		Fragment dialog = getFragmentManager().findFragmentByTag("alertDialog");

		if (dialog != null
				&& ((AlertDialogFragment) dialog).getDialog() != null) {
			mDialogState = DialogState.Alert;
			((AlertDialogFragment) dialog).getDialog().setTitle(
					Survey.getInstance().getString(R.string.navigate));
			((AlertDialogFragment) dialog).setMessage(mAlertMsg);

		} else {

			AlertDialogFragment f = AlertDialogFragment.newInstance(getId(),
					Survey.getInstance().getString(R.string.remove), mAlertMsg);

			mDialogState = DialogState.Alert;
			f.show(getFragmentManager(), "alertDialog");
		}
	}

	@Override
	public void okAlertDialog() {
		mDialogState = DialogState.None;
	}

	private void createAlertDialog(String message) {
		mAlertMsg = message;
		restoreAlertDialog();
	}

	public void removeAll() {
		showProgressDialog(DialogState.RemoveAllProgress);
		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");

		f.removeAllCensus(this);
	}

	private void showProgressDialog(DialogState state) {
		mDialogState = state;
		restoreProgressDialog();
	}

	private void restoreProgressDialog() {
		if (mDialogState == DialogState.RemoveAllProgress
				|| mDialogState == DialogState.RemoveSelectedProgress) {
			Fragment dialog = getFragmentManager().findFragmentByTag(
					PROGRESS_DIALOG_TAG);

			if (dialog != null
					&& ((ProgressDialogFragment) dialog).getDialog() != null) {
				((ProgressDialogFragment) dialog).getDialog().setTitle(
						Survey.getInstance().getString(R.string.remove_census));
				((ProgressDialogFragment) dialog).setMessage(Survey
						.getInstance().getString(R.string.please_wait));
			} else if (progressDialog != null
					&& progressDialog.getDialog() != null) {
				progressDialog.getDialog().setTitle(
						Survey.getInstance().getString(R.string.remove_census));
				progressDialog.setMessage(Survey.getInstance().getString(
						R.string.please_wait));
			} else {
				if (progressDialog != null) {
					dismissProgressDialog();
				}
				progressDialog = ProgressDialogFragment.newInstance(getId(),
						Survey.getInstance().getString(R.string.remove_census),
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

		if (tempDialogState == DialogState.RemoveAllProgress) {
			f.cancelRemoveAllCensusTask();
		} else if (tempDialogState == DialogState.RemoveSelectedProgress) {
			f.cancelRemoveCensusByDateTask();
		}

	}

	private void resetSelection() {
		mSelected.clear();
		getListView().clearChoices(); // doesn't unset the checkboxes
		for (int i = 0; i < getListView().getCount(); ++i) {
			getListView().setItemChecked(i, false);
		}
		getLoaderManager()
				.restartLoader(CENSUS_LIST_BY_DATE_LOADER, null, this);
	}

	@Override
	public void removeCensusByDateComplete(boolean success,
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
		f.clearRemoveCensusByDateTask();

		resetSelection();
		if (numOfRowsAffected > 0) {
			createAlertDialog(getActivity().getString(R.string.points_removed,
					numOfRowsAffected, numOfRowsAffected > 1 ? "s" : ""));
		} else {
			createAlertDialog(getActivity().getString(
					R.string.points_not_deleted));
		}
	}

	@Override
	public void removeCensusByDateCanceled() {
		resetSelection();

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
	public void removeCensusByDateProgressUpdate(String message) {

	}

	@Override
	public void removeAllCensusComplete(boolean success, long numOfRowsAffected) {
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

		resetSelection();
		if (numOfRowsAffected > 0) {
			createAlertDialog(getActivity().getString(R.string.points_removed,
					numOfRowsAffected, numOfRowsAffected > 1 ? "s" : ""));
		} else {
			createAlertDialog(getActivity().getString(
					R.string.points_not_deleted));
		}
	}

	@Override
	public void removeAllCensusCanceled() {
		resetSelection();

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

		createAlertDialog(getActivity().getString(
				R.string.removing_census_canceled_by_user));
	}

	@Override
	public void removeAllCensusProgressUpdate(String message) {

	}
}
