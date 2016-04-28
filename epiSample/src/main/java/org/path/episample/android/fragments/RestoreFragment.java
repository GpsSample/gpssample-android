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

import java.util.List;

import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.fragments.ProgressDialogFragment.CancelProgressDialog;
import org.path.episample.android.listeners.JoinRestoreListener;
import org.path.episample.android.listeners.RestoreListener;
import org.path.episample.android.tasks.BackupListLoader;
import org.path.episample.android.utilities.BackupRestoreUtils;
import org.path.episample.android.utilities.BackupRestoreUtils.BackupDatabase;

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
 * Fragment displays all databases in /survey/metadata/backup folder
 * 
 * @author belendia@gmail.com
 * 
 */
public class RestoreFragment extends ListFragment implements
		JoinRestoreListener, RestoreListener,
		LoaderManager.LoaderCallbacks<List<BackupDatabase>>,
		ConfirmAlertDialog, CancelProgressDialog {

	private static final String t = "RestoreFragment";

	private static final int BACKUP_LIST_LOADER = 0x67;

	public static final int ID = R.layout.restore;
	private View mView;

	private String mAppName;

	private BackupFileListAdapter mInstances;
	private TextView mEmptyTextView;
	private TextView mTotalDatabasesTextView;
	private static final String DIALOG_MSG = "dialogmsg";
	private static final String PROGRESS_DIALOG_TAG = "progressDialog";
	private static final String SELECTED_DB = "selectedDb";
	private Handler handler = new Handler();

	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;
	private ProgressDialogFragment progressDialog = null;
	private AlertDialog mAlert;

	private static enum DialogState {
		JoinRestoreProgress, RestoreProgress, Alert, None
	};

	private RelativeLayout restoreButtonContainer;
	private RelativeLayout joinRestoreButtonContainer;
	private ImageButton restoreImageButton;
	private ImageButton joinRestoreImageButton;

	private String mSelectedDb = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.restore);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}

		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}

			if (savedInstanceState.containsKey(SELECTED_DB)) {
				mSelectedDb = savedInstanceState.getString(SELECTED_DB);
			}
		}

		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(ID, container, false);
		mEmptyTextView = (TextView) mView.findViewById(android.R.id.empty);
		mTotalDatabasesTextView = (TextView) mView
				.findViewById(R.id.totalDatabasesTextView);
		restoreButtonContainer = (RelativeLayout) mView
				.findViewById(R.id.restoreButtonContainer);
		restoreImageButton = (ImageButton) mView
				.findViewById(R.id.restoreImageButton);
		joinRestoreButtonContainer = (RelativeLayout) mView
				.findViewById(R.id.joinRestoreButtonContainer);
		joinRestoreImageButton = (ImageButton) mView
				.findViewById(R.id.joinRestoreImageButton);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		restoreButtonContainer.setOnClickListener(restoreClickListener);
		restoreImageButton.setOnClickListener(restoreClickListener);

		joinRestoreButtonContainer.setOnClickListener(joinRestoreClickListener);
		joinRestoreImageButton.setOnClickListener(joinRestoreClickListener);

		mInstances = new BackupFileListAdapter(getActivity());
		setListAdapter(mInstances);
		getListView().setEmptyView(mEmptyTextView);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		getLoaderManager().initLoader(BACKUP_LIST_LOADER, null, this);

		registerForContextMenu(getListView());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(DIALOG_MSG, mAlertMsg);
		outState.putString(SELECTED_DB, mSelectedDb);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mDialogState == DialogState.JoinRestoreProgress
				|| mDialogState == DialogState.RestoreProgress) {
			restoreProgressDialog();
		}

		ListView ls = getListView();
		for (int pos = 0; pos < ls.getCount(); pos++) {
			BackupDatabase db = (BackupDatabase) ls.getItemAtPosition(pos);
			if (mSelectedDb.equals(db.getPath())) {
				ls.setItemChecked(pos, true);
				break;
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
	public Loader<List<BackupDatabase>> onCreateLoader(int arg0, Bundle args) {
		return new BackupListLoader(getActivity(),
				((ODKActivity) getActivity()).getAppName());
	}

	@Override
	public void onLoadFinished(Loader<List<BackupDatabase>> loader,
			List<BackupDatabase> cursor) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mInstances.clear();
		mInstances.addAll(cursor);
		mTotalDatabasesTextView.setText(getActivity().getString(
				R.string.num_of_backup_files, cursor.size(),
				(cursor.size() > 1 ? "s" : "")));
	}

	@Override
	public void onLoaderReset(Loader<List<BackupDatabase>> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mInstances.clear();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		BackupDatabase selectedDb = (BackupDatabase) getListAdapter().getItem(
				position);
		if (selectedDb != null) {
			mSelectedDb = selectedDb.getPath();
		}
	}

	private OnClickListener restoreClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (mSelectedDb != null && mSelectedDb.length() > 0) {
				if (BackupRestoreUtils.dbFileExists(mSelectedDb)) {
					confirmRestoreAndReplaceDB();
				} else {
					createAlertDialog(getActivity().getString(
							R.string.db_does_not_exist));
				}
			} else {
				createAlertDialog(getActivity().getString(
						R.string.select_db_to_restore));
			}
		}
	};

	private OnClickListener joinRestoreClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (mSelectedDb != null && mSelectedDb.length() > 0) {
				if (BackupRestoreUtils.dbFileExists(mSelectedDb)) {
					confirmJoinRestore();
				} else {
					createAlertDialog(getActivity().getString(
							R.string.db_does_not_exist));
				}
			} else {
				createAlertDialog(getActivity().getString(
						R.string.select_db_to_restore));
			}
		}
	};

	private void confirmRestoreAndReplaceDB() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.restore_census))
				.setMessage(getString(R.string.restore_db))
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
								restore();
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	private void confirmJoinRestore() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.restore_census))
				.setMessage(getString(R.string.restore_db))
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
								joinRestore();
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	public String getAppName() {
		return mAppName;
	}

	private void restore() {
		showProgressDialog(DialogState.RestoreProgress);

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");

		f.restore(this, mSelectedDb);
	}

	private static class BackupFileListAdapter extends
			ArrayAdapter<BackupDatabase> {
		private final LayoutInflater mInflater;
		private Context mContext;

		public BackupFileListAdapter(Context context) {
			super(context, R.layout.restore);
			mContext = context;
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
				view = mInflater.inflate(R.layout.restore_db_list_item, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.dbNameTextView = (TextView) view
						.findViewById(R.id.dbNameTextView);
				viewHolder.createdDateTextView = (TextView) view
						.findViewById(R.id.createdDateTextView);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BackupDatabase dbFile = getItem(position);
			viewHolder.dbNameTextView.setText(dbFile.getName());
			viewHolder.createdDateTextView.setText(dbFile.getDateCreated());

			return view;
		}
	}

	private static class ViewHolder {
		TextView dbNameTextView;
		TextView createdDateTextView;
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

			AlertDialogFragment f = AlertDialogFragment
					.newInstance(getId(),
							Survey.getInstance().getString(R.string.restore),
							mAlertMsg);

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

	private void restoreProgressDialog() {
		if (mDialogState == DialogState.JoinRestoreProgress
				|| mDialogState == DialogState.RestoreProgress) {
			Fragment dialog = getFragmentManager().findFragmentByTag(
					PROGRESS_DIALOG_TAG);

			if (dialog != null
					&& ((ProgressDialogFragment) dialog).getDialog() != null) {
				((ProgressDialogFragment) dialog).getDialog().setTitle(
						Survey.getInstance().getString(R.string.restore));
				((ProgressDialogFragment) dialog).setMessage(Survey
						.getInstance().getString(R.string.please_wait));
			} else if (progressDialog != null
					&& progressDialog.getDialog() != null) {
				progressDialog.getDialog().setTitle(
						Survey.getInstance().getString(R.string.restore));
				progressDialog.setMessage(Survey.getInstance().getString(
						R.string.please_wait));
			} else {
				if (progressDialog != null) {
					dismissProgressDialog();
				}
				progressDialog = ProgressDialogFragment.newInstance(getId(),
						Survey.getInstance().getString(R.string.restore),
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

	private void showProgressDialog(DialogState state) {
		mDialogState = state;
		restoreProgressDialog();
	}

	@Override
	public void joinRestoreComplete(boolean success) {
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
		f.clearJoinRestoreTask();

		if (success == true) {
			createAlertDialog(getString(R.string.restore_success));
		} else {
			createAlertDialog(getString(R.string.restore_failed));
		}
	}

	@Override
	public void joinRestoreCanceled() {
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
		f.clearJoinRestoreTask();
		createAlertDialog(getString(R.string.restoring_points_canceled));
	}

	@Override
	public void joinRestoreProgressUpdate(String message) {

	}

	public void joinRestore() {
		showProgressDialog(DialogState.JoinRestoreProgress);

		BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
				.findFragmentByTag("background");

		f.joinRestore(this, mSelectedDb);
	}

	@Override
	public void restoreComplete(boolean success) {
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
		f.clearRestoreTask();

		if (success == true) {
			createAlertDialog(getString(R.string.restore_success));
		} else {
			createAlertDialog(getString(R.string.restore_failed));
		}
	}

	@Override
	public void restoreCanceled() {
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
		f.clearRestoreTask();

		createAlertDialog(getString(R.string.restoring_points_canceled));
	}

	@Override
	public void restoreProgressUpdate(String message) {

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
		if (tempDialogState == DialogState.JoinRestoreProgress) {
			f.cancelJoinRestoreTask();
		} else if (tempDialogState == DialogState.RestoreProgress) {
			f.cancelRestoreTask();
		}
	}
}
