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
import org.path.common.android.data.PlaceModel;
import org.path.common.android.database.CensusDatabaseHelper;
import org.path.common.android.utilities.PlaceNameUtil;
import org.path.common.android.utilities.PlaceNameUtil.HirarchyOrder;
import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.tasks.CensusListToEditLoader;
import org.path.episample.android.utilities.SurveyUtil;
import org.path.episample.android.utilities.SurveyUtil.SurveyFormParameters;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment displaying all points to edit using the web app
 * 
 * @author belendia@gmail.com
 * 
 */
public class EditCensusFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<CensusModel>>, ConfirmAlertDialog {

	private static final String t = "EditDataFragment";

	private static final int CENSUS_LIST_LOADER = 0x24;

	private static final int MENU_EDIT_POINT = Menu.FIRST;

	public static final int ID = R.layout.edit_census;
	private View mView;

	private String mAppName;

	private CensusListAdapter mInstances;
	private boolean mEditSavedData = false;
	private TextView mEmptyTextView;
	private TextView mTotalCensusTextView;
	private static final String DIALOG_MSG = "dialogmsg";

	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;
	private AlertDialog mAlert;

	private static enum DialogState {
		Alert, None
	};

	public static String mSelectedPlaceName = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.edit_census);
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
		mEmptyTextView = (TextView) mView.findViewById(android.R.id.empty);
		mTotalCensusTextView = (TextView) mView
				.findViewById(R.id.totalCensusTextView);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mInstances = new CensusListAdapter(getActivity());
		setListAdapter(mInstances);
		getListView().setEmptyView(mEmptyTextView);

		getLoaderManager().initLoader(CENSUS_LIST_LOADER, null, this);

		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.filter_menu:

			return true;
		default:
			break;
		}

		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.putInt(SORT, mSort.ordinal());
		outState.putString(DIALOG_MSG, mAlertMsg);
		// outState.putParcelable(SELECTED_CENSUS, mSelectedCensus);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
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
	public Loader<List<CensusModel>> onCreateLoader(int arg0, Bundle args) {
		return new CensusListToEditLoader(getActivity(),
				((ODKActivity) getActivity()).getAppName());
	}

	@Override
	public void onLoadFinished(Loader<List<CensusModel>> loader,
			List<CensusModel> cursor) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mInstances.clear();
		mInstances.addAll(cursor);
		mTotalCensusTextView.setText(getActivity().getString(
				R.string.num_of_census, cursor.size(),
				(cursor.size() > 1 ? "s" : "")));
	}

	@Override
	public void onLoaderReset(Loader<List<CensusModel>> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mInstances.clear();
	}

	private static class CensusListAdapter extends ArrayAdapter<CensusModel> {
		private final LayoutInflater mInflater;
		private Context mContext;

		public CensusListAdapter(Context context) {
			super(context, R.layout.census_list_item);
			mContext = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/**
		 * Populate items in the list.
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder viewHolder;

			view = convertView;

			if (view == null) {
				view = mInflater.inflate(R.layout.census_list_to_edit_item,
						parent, false);
				viewHolder = new ViewHolder();
				viewHolder.headNameTextView = (TextView) view
						.findViewById(R.id.headNameTextView);
				viewHolder.houseNoTextView = (TextView) view
						.findViewById(R.id.houseNoTextView);
				viewHolder.commentTextView = (TextView) view
						.findViewById(R.id.commentTextView);
				viewHolder.placeNameTextView = (TextView) view
						.findViewById(R.id.placeNameTextView);
				viewHolder.censusImageView = (ImageView) view
						.findViewById(R.id.censusImageView);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			CensusModel census = getItem(position);
			viewHolder.headNameTextView
					.setText((census.getHeadName().length() > 70 ? census
							.getHeadName().substring(0, 70) + " ..." : census
							.getHeadName()));
			viewHolder.houseNoTextView.setText(mContext.getString(
					R.string.house_number, census.getHouseNumber()));

			int commentLength = census.getComment() != null ? census
					.getComment().length() : 0;

			if (commentLength == 0) {
				viewHolder.commentTextView.setVisibility(View.GONE);
			} else {
				viewHolder.commentTextView.setVisibility(View.VISIBLE);
				viewHolder.commentTextView.setText((census.getComment()
						.length() > 70 ? census.getComment().substring(0, 70)
						+ " ..." : census.getComment()));
			}

			ArrayList<PlaceModel> placeNames = PlaceNameUtil
					.getHierarchiesDetail(mContext, census.getPlaceName(),
							HirarchyOrder.LowerToHigher);
			String placeName = "";
			if (placeNames != null && placeNames.size() > 1) {
				placeName = ((PlaceModel) placeNames.get(1)).getName() + ", "
						+ ((PlaceModel) placeNames.get(0)).getName();
			} else {
				placeName = census.getPlaceName();
			}

			viewHolder.placeNameTextView.setText(mContext.getString(
					R.string.place_name_hierarchy, placeName));

			int resourceId = getNotSelectedPointIcon(census);
			if (census.getSelected() == CensusModel.MAIN_POINT) {
				resourceId = getMainPointIcon(census);
			} else if (census.getSelected() == CensusModel.ALTERNATE_POINT) {
				resourceId = getAlternatePointIcon(census);
			} else if (census.getSelected() == CensusModel.ADDITIONAL_POINT) {
				resourceId = getAdditionalPointIcon(census);
			} else if (census.getExcluded() == 1) {
				resourceId = getExcludedPointIcon(census);
			}

			viewHolder.censusImageView.setBackgroundResource(resourceId);

			return view;
		}
	}

	private static class ViewHolder {
		TextView headNameTextView;
		TextView houseNoTextView;
		TextView commentTextView;
		TextView placeNameTextView;
		ImageView censusImageView;
	}

	private static int getNotSelectedPointIcon(CensusModel census) {
		int resourceId = R.drawable.ic_not_selected;
		switch (census.getProcessed()) {
		case 1:
			resourceId = R.drawable.ic_not_selected_pending;
			break;
		case 2:
			resourceId = R.drawable.ic_not_selected_done;
			break;
		}

		return resourceId;
	}

	private static int getMainPointIcon(CensusModel census) {
		int resourceId = R.drawable.ic_main;
		switch (census.getProcessed()) {
		case 1:
			resourceId = R.drawable.ic_main_pending;
			break;
		case 2:
			resourceId = R.drawable.ic_main_done;
			break;
		}

		return resourceId;
	}

	private static int getAlternatePointIcon(CensusModel census) {
		int resourceId = R.drawable.ic_alternate;
		switch (census.getProcessed()) {
		case 1:
			resourceId = R.drawable.ic_alternate_pending;
			break;
		case 2:
			resourceId = R.drawable.ic_alternate_done;
			break;
		}

		return resourceId;
	}

	private static int getAdditionalPointIcon(CensusModel census) {
		int resourceId = R.drawable.ic_additional;
		switch (census.getProcessed()) {
		case 1:
			resourceId = R.drawable.ic_additional_pending;
			break;
		case 2:
			resourceId = R.drawable.ic_additional_done;
			break;
		}

		return resourceId;
	}

	private static int getExcludedPointIcon(CensusModel census) {
		int resourceId = R.drawable.ic_excluded;
		switch (census.getProcessed()) {
		case 1:
			resourceId = R.drawable.ic_excluded_pending;
			break;
		case 2:
			resourceId = R.drawable.ic_excluded_done;
			break;
		}

		return resourceId;
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
					Survey.getInstance().getString(R.string.navigate),
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			MainMenuActivity.SELECTED_CENSUS = (((CensusListAdapter) getListAdapter())
					.getItem(info.position));

			if (MainMenuActivity.SELECTED_CENSUS != null) {
				menu.setHeaderTitle(MainMenuActivity.SELECTED_CENSUS
						.getHeadName()
						+ " ("
						+ MainMenuActivity.SELECTED_CENSUS.getHouseNumber()
						+ ")");
				menu.add(Menu.NONE, MENU_EDIT_POINT, Menu.NONE, getActivity()
						.getString(R.string.edit_point));
				if (mEditSavedData == true) {
					menu.add(Menu.NONE, MENU_EDIT_POINT, menu.NONE,
							getActivity().getString(R.string.edit_point));
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EDIT_POINT:
			if (MainMenuActivity.SELECTED_CENSUS != null) {
				SurveyUtil surveyUtil = new SurveyUtil();
				SurveyFormParameters surveyFormParameters = surveyUtil.new SurveyFormParameters(
						true, CensusDatabaseHelper.CENSUS_DATABASES_TABLE, null);

				Intent intentEdit = SurveyUtil.getIntentForOdkSurveyEditRow(
						getActivity(), "survey", surveyFormParameters,
						MainMenuActivity.SELECTED_CENSUS.getInstanceId());
				SurveyUtil.launchSurveyToEditRow(getActivity(), intentEdit,
						MainMenuActivity.CENSUS_REQUEST_CODE);
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
