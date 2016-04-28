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

import org.path.common.android.data.CensusModel;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.fragments.ProgressDialogFragment.CancelProgressDialog;
import org.path.episample.android.listeners.InvalidateCensusListener;
import org.path.episample.android.listeners.MarkCensusAsInvalidListener;
import org.path.episample.android.logic.PairSearch.Pair;
import org.path.episample.android.tasks.PairListLoader;
import org.path.episample.android.utilities.DistanceUtil;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Fragment displays distinct created date of census database
 * 
 * @author belendia@gmail.com
 * 
 */
public class InvalidateCensusFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<Pair>>, ConfirmAlertDialog,
		CancelProgressDialog, MarkCensusAsInvalidListener,
		InvalidateCensusListener {

	private static final String t = "MarkCensusAsInvalidFragment";

	public static final int ID = R.layout.mark_census_as_invalid_by_distance;
	private View mView;

	private static final int PAIRS_LIST_LOADER = 0x83;

	private String mAppName;

	private static final String DIALOG_MSG = "dialogmsg";
	private static final String PROGRESS_DIALOG_TAG = "progressDialog";

	private ProgressDialogFragment progressDialog = null;
	private Handler handler = new Handler();
	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;
	private AlertDialog mAlert;
	private PairListAdapter mInstances;

	private static enum DialogState {
		Progress, Alert, None
	};

	private EditText mMetersEditText;
	private TextView mTotalPairsTextView;
	private ImageButton mFindImageButton;
	private TextView mEmptyTextView;

	public enum DistanceType {
		Closest, Farthest
	};

	private DistanceType mSelectedDistanceType = DistanceType.Closest;

	private int mMeters = 0;

	public List<Pair> mPairs = new ArrayList<Pair>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.invalidate_census);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}

		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}
		}

		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(ID, container, false);

		mTotalPairsTextView = (TextView) mView
				.findViewById(R.id.totalPairsTextView);
		mMetersEditText = (EditText) mView.findViewById(R.id.metersEditText);
		mFindImageButton = (ImageButton) mView
				.findViewById(R.id.findImageButton);
		mEmptyTextView = (TextView) mView.findViewById(android.R.id.empty);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mFindImageButton.setOnClickListener(findButtonClickListener);

		mInstances = new PairListAdapter(getActivity());
		setListAdapter(mInstances);
		getListView().setEmptyView(mEmptyTextView);
		mInstances.setInvalidateCensusListener(this);

		getLoaderManager().initLoader(PAIRS_LIST_LOADER, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(DIALOG_MSG, mAlertMsg);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDialogState == DialogState.Progress) {
			restoreProgressDialog();
		}
	}

	@Override
	public void onPause() {
		dismissProgressDialog();
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.invalidate_menu, menu);
		MenuItem item = null;
		if (mSelectedDistanceType == DistanceType.Closest) {
			item = menu.findItem(R.id.close_points);
		} else if (mSelectedDistanceType == DistanceType.Farthest) {
			item = menu.findItem(R.id.distant_points);
		}

		if (item != null) {
			item.setChecked(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.close_points:
			item.setChecked(true);
			mSelectedDistanceType = DistanceType.Closest;
			resetViews();
			break;
		case R.id.distant_points:
			item.setChecked(true);
			mSelectedDistanceType = DistanceType.Farthest;
			resetViews();
			break;
		}

		return true;
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

	private void find() {
		if (checkUserInput()) {
			showProgressDialog(DialogState.Progress);
			BackgroundTaskFragment f = (BackgroundTaskFragment) getFragmentManager()
					.findFragmentByTag("background");

			f.markCensusAsInvalid(InvalidateCensusFragment.this, mMeters,
					mSelectedDistanceType.ordinal());
		} else {
			createAlertDialog(getActivity().getString(
					R.string.invalid_user_input));
		}
	}

	private OnClickListener findButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			find();
		}
	};

	private void resetViews() {
		mPairs = new ArrayList<Pair>();
		getLoaderManager().restartLoader(PAIRS_LIST_LOADER, null, this);
		mMetersEditText.setText("");
	}

	private boolean checkUserInput() {
		if (mMetersEditText.getText().toString().length() >= 0) {
			try {
				mMeters = Integer
						.parseInt(mMetersEditText.getText().toString());
				if (mMeters < 1) {
					return false;
				}
				return true;
			} catch (Exception ex) {

			}
		}
		return false;
	}

	public String getAppName() {
		return mAppName;
	}

	private static class PairListAdapter extends ArrayAdapter<Pair> {
		private final LayoutInflater mInflater;
		private InvalidateCensusListener listener;

		public PairListAdapter(Context context) {
			super(context, R.layout.mark_census_as_invalid_by_distance);
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
				view = mInflater.inflate(R.layout.point_pair_item, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.headNameTextView1 = (TextView) view
						.findViewById(R.id.headNameTextView1);
				viewHolder.houseNoTextView1 = (TextView) view
						.findViewById(R.id.houseNoTextView1);
				viewHolder.headNameTextView2 = (TextView) view
						.findViewById(R.id.headNameTextView2);
				viewHolder.houseNoTextView2 = (TextView) view
						.findViewById(R.id.houseNoTextView2);
				viewHolder.distanceTextView = (TextView) view
						.findViewById(R.id.distanceTextView);
				viewHolder.invalidateImageButton1 = (ImageButton) view
						.findViewById(R.id.invalidateImageButton1);
				viewHolder.invalidateImageButton2 = (ImageButton) view
						.findViewById(R.id.invalidateImageButton2);

				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			Pair pair = getItem(position);

			viewHolder.headNameTextView1
					.setText(pair.getPoint1().getHeadName());
			viewHolder.houseNoTextView1.setText(Survey.getInstance().getString(
					R.string.house_number, pair.getPoint1().getHouseNumber()));
			viewHolder.headNameTextView2
					.setText(pair.getPoint2().getHeadName());
			viewHolder.houseNoTextView2.setText(Survey.getInstance().getString(
					R.string.house_number, pair.getPoint2().getHouseNumber()));
			viewHolder.distanceTextView.setText(Survey.getInstance().getString(
					R.string.distance,
					DistanceUtil.getFormatedDistance(pair.getDistance())));

			viewHolder.invalidateImageButton1
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if (listener != null) {
								Pair pair = getItem(position);
								if (pair != null) {
									listener.invalidateButtonClicked(pair
											.getPoint1());
								}
							}
						}
					});

			viewHolder.invalidateImageButton2
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if (listener != null) {
								Pair pair = getItem(position);
								if (pair != null) {
									listener.invalidateButtonClicked(pair
											.getPoint2());
								}
							}
						}
					});

			return view;
		}

		public void setInvalidateCensusListener(
				InvalidateCensusListener listener) {
			this.listener = listener;
		}
	}

	private static class ViewHolder {
		TextView headNameTextView1;
		TextView houseNoTextView1;
		TextView headNameTextView2;
		TextView houseNoTextView2;
		TextView distanceTextView;
		ImageButton invalidateImageButton1;
		ImageButton invalidateImageButton2;
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
					Survey.getInstance().getString(R.string.invalidate_census),
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
						Survey.getInstance().getString(
								R.string.invalidate_census));
				((ProgressDialogFragment) dialog).setMessage(Survey
						.getInstance().getString(R.string.please_wait));
			} else if (progressDialog != null
					&& progressDialog.getDialog() != null) {
				progressDialog.getDialog().setTitle(
						Survey.getInstance().getString(
								R.string.invalidate_census));
				progressDialog.setMessage(Survey.getInstance().getString(
						R.string.please_wait));
			} else {
				if (progressDialog != null) {
					dismissProgressDialog();
				}
				progressDialog = ProgressDialogFragment.newInstance(
						getId(),
						Survey.getInstance().getString(
								R.string.invalidate_census), Survey
								.getInstance().getString(R.string.please_wait));
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

		f.cancelMarkCensusAsInvalidTask();

	}

	@Override
	public void markAsInvalidCanceled() {
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
		f.clearMarkCensusAsInvalidTask();

		createAlertDialog(getActivity().getString(
				R.string.removing_census_canceled_by_user));
	}

	@Override
	public void markAsInvalidProgressUpdate(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markAsInvalidComplete(List<Pair> pairs) {
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
		f.clearMarkCensusAsInvalidTask();

		mPairs = pairs;
		getLoaderManager().restartLoader(PAIRS_LIST_LOADER, null, this);
		if (mPairs == null || (mPairs != null && mPairs.size() == 0)) {
			if (mSelectedDistanceType == DistanceType.Farthest) {
				createAlertDialog(getActivity().getString(
						R.string.no_distant_points, mMeters));
			} else {
				createAlertDialog(getActivity().getString(
						R.string.no_close_points, mMeters));
			}
		}
	}

	@Override
	public Loader<List<Pair>> onCreateLoader(int arg0, Bundle arg1) {

		return new PairListLoader(getActivity(),
				((ODKActivity) getActivity()).getAppName(), mPairs);
	}

	@Override
	public void onLoadFinished(Loader<List<Pair>> loader, List<Pair> cursor) {
		mInstances.clear();
		mInstances.addAll(cursor);
		mTotalPairsTextView.setText(getActivity().getString(
				R.string.num_of_pairs, cursor.size(),
				(cursor.size() > 1 ? "s" : "")));
	}

	@Override
	public void onLoaderReset(Loader<List<Pair>> arg0) {
		mInstances.clear();
	}

	@Override
	public void invalidateButtonClicked(final CensusModel point) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.invalidate_census))
				.setMessage(
						getActivity().getString(
								R.string.confirm_mark_point_as_invalid))
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
								if (point != null) {
									CensusUtil.invalidateCensus(getActivity(),
											point.getInstanceId());
									removePoints(point);
									getLoaderManager().restartLoader(
											PAIRS_LIST_LOADER, null,
											InvalidateCensusFragment.this);
								}
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	private void removePoints(CensusModel census) {
		if (mPairs != null) {
			for (int i = mPairs.size() - 1; i >= 0; i--) {
				if (mPairs.get(i).getPoint1().getInstanceId()
						.equals(census.getInstanceId())
						|| mPairs.get(i).getPoint2().getInstanceId()
								.equals(census.getInstanceId())) {
					mPairs.remove(i);
				}
			}
		}
	}
}
