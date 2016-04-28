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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.path.common.android.data.CensusModel;
import org.path.common.android.data.PlaceModel;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.CensusUtil.FilterCensusList;
import org.path.common.android.utilities.CensusUtil.Sort;
import org.path.common.android.utilities.PlaceNameUtil;
import org.path.common.android.utilities.PlaceNameUtil.HirarchyOrder;
import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.FilterCensusListActivity;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.ODKActivity;
import org.path.episample.android.activities.SelectPlaceNameActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;
import org.path.episample.android.provider.DirectionProvider;
import org.path.episample.android.provider.DirectionProvider.DirectionEventListener;
import org.path.episample.android.provider.DirectionProvider.LocationEventListener;
import org.path.episample.android.tasks.CensusListLoader;
import org.path.episample.android.utilities.DistanceUtil;
import org.path.episample.android.utilities.SurveyUtil;
import org.path.episample.android.utilities.SurveyUtil.SurveyFormParameters;
import org.path.episample.android.views.CompassView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.todddavies.components.progressbar.ProgressWheel;

/**
 * Fragment displaying the navigate modules in the app.
 * 
 * @author belendia@gmail.com
 * 
 */
public class NavigateFragment extends ListFragment implements
		DirectionEventListener, LocationEventListener,
		LoaderManager.LoaderCallbacks<List<CensusModel>>, ConfirmAlertDialog {

	private static final String t = "NavigateFragment";

	private static final int CENSUS_LIST_LOADER = 0x24;

	private static final int MENU_START_QUESTIONNAIRE = Menu.FIRST;
	private static final int MENU_EDIT_POINT = Menu.FIRST + 1;
	private static final int MENU_MARK_AS_FINALIZED_BY_FRIENDS = Menu.FIRST + 2;

	public static final int ID = R.layout.navigate;
	private View mView;

	// default location accuracy
	private static final double GREEN_LOCATION_ACCURACY = 10;
	private static final double YELLOW_LOCATION_ACCURACY = 50;

	private ProgressWheel mSignalQualitySpinner;

	private TextView mHeadingTextView;
	private TextView mBearingTextView;
	private TextView mDistanceTextView;
	private TextView mFinalizedTextView;
	private TextView mIncompleteTextView;
	private TextView mNotStartedTextView;
	private TextView mEmptyTextView;
	private TextView mTotalCensusTextView;
	// private TextView mCalibrationRequiredTextView;

	private String mAppName;

	private CompassView mCompass;
	private CompassView mCensusLocation;
	private SwipeRefreshLayout mRefreshLayout;

	private CensusListAdapter mInstances;
	private static int mSelectedCensusRow = -1;
	// private CensusModel mSelectedCensus;

	private int mGPSAccuracyThresholds = 10;
	private String mFormId = "";
	private boolean mShowFilterByPlaceName = false;
	private boolean mEditSavedData = false;
	private boolean mMarkAsFinalizedByFriends = true;

	protected static final int FILTER_REQUEST_CODE = 65832;
	protected static final int PLACE_NAME_REQUEST_CODE = 61477;

	// public static final String SORT = "sort";
	public static final String CURRENT_LATITUDE = "current_latitude";
	public static final String CURRENT_LONGITUDE = "current_longitude";
	private static final String DIALOG_MSG = "dialogmsg";

	// private static final String SELECTED_CENSUS = "selected_census";

	private DirectionProvider mDirectionProvider;

	private enum Color {
		Green, Yellow, Red, Black
	};

	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;
	private AlertDialog mAlert;

	private static enum DialogState {
		Alert, None
	};

	public static ArrayList<FilterCensusList> mFilterCensusList = new ArrayList<FilterCensusList>();
	public static Sort mSort = Sort.POINT_TYPES;
	public static String mSelectedPlaceName = null;
	public static String mSelectedCode = null;
	private MenuItem mFilterByPlaceNameMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.navigate);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}

		mDirectionProvider = new DirectionProvider(getActivity());
		mDirectionProvider.setDirectionEventListener(this);
		mDirectionProvider.setLocationEventListener(this);

		setHasOptionsMenu(true);

		if (mFilterCensusList.size() == 0) {
			mFilterCensusList.add(FilterCensusList.MainPoint);
			mFilterCensusList.add(FilterCensusList.AdditionalPoint);
			mFilterCensusList.add(FilterCensusList.AlternatePoint);
		}

		if (savedInstanceState != null) {

			/*
			 * if(savedInstanceState.containsKey(SORT)) { mSort =
			 * Sort.values()[savedInstanceState.getInt(SORT)]; }
			 */

			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}

			/*
			 * if(savedInstanceState.containsKey(SELECTED_CENSUS)) {
			 * MainMenuActivity.SELECTED_CENSUS =
			 * savedInstanceState.getParcelable(SELECTED_CENSUS); }
			 */

		}

		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(ID, container, false);

		mSignalQualitySpinner = (ProgressWheel) mView
				.findViewById(R.id.signalQualitySpinner);
		mCompass = (CompassView) mView.findViewById(R.id.compass);
		mCensusLocation = (CompassView) mView.findViewById(R.id.censusLocation);

		mBearingTextView = (TextView) mView.findViewById(R.id.bearingTextView);
		mHeadingTextView = (TextView) mView.findViewById(R.id.headingTextView);
		mDistanceTextView = (TextView) mView
				.findViewById(R.id.distanceTextView);
		mFinalizedTextView = (TextView) mView
				.findViewById(R.id.finalizedTextView);
		mIncompleteTextView = (TextView) mView
				.findViewById(R.id.incompleteTextView);
		mNotStartedTextView = (TextView) mView
				.findViewById(R.id.notStartedTextView);
		mEmptyTextView = (TextView) mView.findViewById(android.R.id.empty);
		mTotalCensusTextView = (TextView) mView
				.findViewById(R.id.totalCensusTextView);
		// mCalibrationRequiredTextView = (TextView)
		// mView.findViewById(R.id.calibrationRequiredTextView);

		mRefreshLayout = (SwipeRefreshLayout) mView
				.findViewById(R.id.swipe_container);
		mRefreshLayout.setColorScheme(R.color.swip_to_refresh_color);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mDirectionProvider.isGpsProviderOn() == false
				&& mDirectionProvider.isNetworkOn() == false) {
			setSpinnerColor(Color.Black);
			Toast.makeText(getActivity(),
					getString(R.string.provider_disabled_error),
					Toast.LENGTH_SHORT).show();
		} else {
			setSpinnerColor(Color.Red);
		}
		
		mInstances = new CensusListAdapter(getActivity());
		setListAdapter(mInstances);
		mRefreshLayout.setOnRefreshListener(onRefreshListener);
		getListView().setEmptyView(mEmptyTextView);

		getLoaderManager().initLoader(CENSUS_LIST_LOADER, null, this);

		mDistanceTextView.setText(getActivity().getString(R.string.distance,
				"-"));

		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.navigate_menu, menu);
		MenuItem item = null;
		if (mSort == Sort.POINT_TYPES) {
			item = menu.findItem(R.id.point_types);
		} else if (mSort == Sort.CLOSEST) {
			item = menu.findItem(R.id.closest_points);
		} else if (mSort == Sort.FARTHEST) {
			item = menu.findItem(R.id.farthest_points);
		} else if (mSort == Sort.NEWEST_ENTRY) {
			item = menu.findItem(R.id.newest_entry);
		} else if (mSort == Sort.OLDEST_ENTRY) {
			item = menu.findItem(R.id.oldest_entry);
		} else if (mSort == Sort.ENUMERATOR_NAME) {
			item = menu.findItem(R.id.enumerator_name);
		} else if (mSort == Sort.DEVICE_ID) {
			item = menu.findItem(R.id.device_id);
		}

		if (item != null) {
			item.setChecked(true);
		}

		mFilterByPlaceNameMenu = menu.findItem(R.id.filter_by_place_name_menu);
		showHideFilterByPlaceName();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.filter_menu:
			filterCensusList();
			return true;
		case R.id.point_types:
			item.setChecked(true);
			sortCensus(Sort.POINT_TYPES);
			return true;
		case R.id.closest_points:
			if (mDirectionProvider.getCurrentLocation() != null) {
				item.setChecked(true);
				sortCensus(Sort.CLOSEST);
			} else {
				createAlertDialog(getActivity().getString(
						R.string.loc_info_not_available));
			}
			return true;
		case R.id.farthest_points:
			if (mDirectionProvider.getCurrentLocation() != null) {
				item.setChecked(true);
				sortCensus(Sort.FARTHEST);
			} else {
				createAlertDialog(getActivity().getString(
						R.string.loc_info_not_available));
			}
			return true;
		case R.id.newest_entry:
			item.setChecked(true);
			sortCensus(Sort.NEWEST_ENTRY);
			return true;
		case R.id.oldest_entry:
			item.setChecked(true);
			sortCensus(Sort.OLDEST_ENTRY);
			return true;
		case R.id.enumerator_name:
			item.setChecked(true);
			sortCensus(Sort.ENUMERATOR_NAME);
			return true;
		case R.id.device_id:
			item.setChecked(true);
			sortCensus(Sort.DEVICE_ID);
			return true;
		case R.id.filter_by_place_name_menu:
			changePlace();
			return true;
		case R.id.map_menu:

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

		mDirectionProvider.start();
		if (mDirectionProvider.isGpsProviderOn()
				|| mDirectionProvider.isNetworkOn()) {
			setSpinnerColor(Color.Red);
			mSignalQualitySpinner.spin();
		}
		
		String get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_GPS_ACCURACY_THRESHOLDS);

		mGPSAccuracyThresholds = 10;
		if (get != null) {
			try {
				mGPSAccuracyThresholds = Integer.valueOf(get);
				if (mGPSAccuracyThresholds <= 0 || mGPSAccuracyThresholds > 100) {
					mGPSAccuracyThresholds = 10;
				}
			} catch (Exception ex) {
				mGPSAccuracyThresholds = 10;
			}
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_FORM_ID);
		if (get != null) {
			mFormId = get;
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_SHOW_FILTER_BY_PLACE_NAME_ICON);
		if (get != null && get.equalsIgnoreCase("true")) {
			mShowFilterByPlaceName = true;
		} else {
			mShowFilterByPlaceName = false;
			mSelectedPlaceName = null;
			mSelectedCode = null;
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_EDIT_SAVED_DATA);
		if (get != null && get.equalsIgnoreCase("true")) {
			mEditSavedData = true;
		} else {
			mEditSavedData = false;
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_MARK_AS_FINALIZED_BY_FRIENDS);
		if (get != null && get.equalsIgnoreCase("true")) {
			mMarkAsFinalizedByFriends = true;
		} else {
			mMarkAsFinalizedByFriends = false;
		}

		showHideFilterByPlaceName();
	}

	@Override
	public void onPause() {
		super.onPause();

		mDirectionProvider.stop();

		mSignalQualitySpinner.stopSpinning();
		mSignalQualitySpinner.setText(getString(R.string.acc_value));
	}

	@Override
	public void onLocationChanged(Location location) {
		updateNotification();
		if (isAdded()) {
			updateDistance(location);
		}
	}

	public void showHideFilterByPlaceName() {
		if (mFilterByPlaceNameMenu != null) {
			mFilterByPlaceNameMenu.setVisible(mShowFilterByPlaceName);
		}
	}

	private void updateDistance(Location location) {
		if (mDirectionProvider.getDestinationLocation() != null) {
			double distance = DistanceUtil.getDistance(mDirectionProvider
					.getDestinationLocation().getLatitude(), mDirectionProvider
					.getDestinationLocation().getLongitude(), location
					.getLatitude(), location.getLongitude());
			mDistanceTextView.setText(getActivity().getString(
					R.string.distance,
					DistanceUtil.getFormatedDistance(distance)));
		}
	}

	@Override
	public void onDestroy() {
		if (mAlert != null) {
			if (mAlert.isShowing()) {
				mAlert.dismiss();
			}
		}
		super.onDestroy();
		mSelectedCensusRow = -1;
	}

	private String truncateDouble(double number, int digitsAfterDouble) {
		StringBuilder numOfDigits = new StringBuilder();
		for (int i = 0; i < digitsAfterDouble; i++) {
			numOfDigits.append("#");
		}
		DecimalFormat df = new DecimalFormat("#"
				+ (digitsAfterDouble > 0 ? "." + numOfDigits.toString() : ""));
		return df.format(number);
	}

	@Override
	public void onProviderDisabled(String provider) {

		if (mDirectionProvider.isGpsProviderOn() == false
				&& mDirectionProvider.isNetworkOn() == false) {
			updateNotification();

			mSignalQualitySpinner.stopSpinning();
			mSignalQualitySpinner.setText(Survey.getInstance().getString(
					R.string.acc_value));

			setSpinnerColor(Color.Black);
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (mDirectionProvider.isGpsProviderOn()
				|| mDirectionProvider.isNetworkOn()) {
			updateNotification();
			setSpinnerColor(Color.Red);
			mSignalQualitySpinner.spin();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		case LocationProvider.AVAILABLE:
			if (mDirectionProvider.getCurrentLocation() != null) {
				updateNotification();
			}
			break;
		case LocationProvider.OUT_OF_SERVICE:
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			break;
		}
	}

	public void filterCensusList() {
		Intent intent = new Intent(getActivity(),
				FilterCensusListActivity.class);
		startActivityForResult(intent, FILTER_REQUEST_CODE);
	}

	public void changePlace() {
		Intent intent = new Intent(getActivity(), SelectPlaceNameActivity.class);
		if (mSelectedPlaceName != null && mSelectedPlaceName.length() > 0) {
			intent.putExtra(SelectPlaceNameActivity.PLACE_NAME,
					mSelectedPlaceName);
			intent.putExtra(SelectPlaceNameActivity.PLACE_CODE, mSelectedCode);
		}
		intent.putExtra(SelectPlaceNameActivity.SHOW_CLEAR_BUTTON, true);

		startActivityForResult(intent, PLACE_NAME_REQUEST_CODE);
	}

	private void updateNotification() {
		Location location = mDirectionProvider.getCurrentLocation();
		if (isAdded() && location != null) {
			try {
				mSignalQualitySpinner.setText(truncateDouble(
						location.getAccuracy(), 2)
						+ " " + getString(R.string.meter));

				if (location.getAccuracy() > 0
						&& location.getAccuracy() <= GREEN_LOCATION_ACCURACY) {
					setSpinnerColor(Color.Green);// set the spinner to green to
													// indicate that accuracy is
													// good enough
				} else if (location.getAccuracy() > GREEN_LOCATION_ACCURACY
						&& location.getAccuracy() <= YELLOW_LOCATION_ACCURACY) {
					setSpinnerColor(Color.Yellow);
				} else if (location.getAccuracy() > YELLOW_LOCATION_ACCURACY) {
					setSpinnerColor(Color.Red);
				} else {
					setSpinnerColor(Color.Black);
				}
			} finally {

			}
		}
	}

	private void setSpinnerColor(Color color) {
		int textColor = 0;
		int barColor = 0;
		int rimColor = 0;// circle border color
		if (color == Color.Green) {
			textColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_text_color_green);
			barColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_bar_color_green);
			rimColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_rim_color_green);
		} else if (color == Color.Yellow) {
			textColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_text_color_yellow);
			barColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_bar_color_yellow);
			rimColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_rim_color_yellow);
		} else if (color == Color.Red) {
			textColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_text_color_red);
			barColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_bar_color_red);
			rimColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_rim_color_red);
		} else {
			textColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_text_color_black);
			barColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_bar_color_black);
			rimColor = Survey.getInstance().getResources()
					.getColor(R.color.spinner_rim_color_black);
		}

		mSignalQualitySpinner.setTextColor(textColor);
		mSignalQualitySpinner.setBarColor(barColor);
		mSignalQualitySpinner.setRimColor(rimColor);
	}

	@Override
	public void onHeadingToNorthChanged(float heading) {
		if (isAdded()) {
			mHeadingTextView.setText(getActivity().getString(R.string.heading,
					String.valueOf((int) (heading)),
					mDirectionProvider.getDegToGeo(heading)));
			mCompass.setDegrees(heading);
		}
	}

	@Override
	public void onBearingToCensusLocationChanged(float bearing, float heading) {
		if (isAdded()) {
			mCensusLocation.setVisibility(View.VISIBLE);
			mBearingTextView.setText(getActivity().getString(R.string.bearing,
					String.valueOf((int) (bearing)),
					mDirectionProvider.getDegToGeo(bearing)));

			float rotation = 360 - bearing + heading;

			mCensusLocation.setDegrees(rotation);
		}
	}

	@Override
	public Loader<List<CensusModel>> onCreateLoader(int arg0, Bundle args) {
		return new CensusListLoader(getActivity(),
				((ODKActivity) getActivity()).getAppName(), args);
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
		updateShortInfoAboutQuestionnaireStatus();
	}

	@Override
	public void onLoaderReset(Loader<List<CensusModel>> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mInstances.clear();
		updateShortInfoAboutQuestionnaireStatus();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		itemSelected(position);
	}

	private void itemSelected(int position) {
		mSelectedCensusRow = position;

		CensusModel census = (((CensusListAdapter) getListAdapter())
				.getItem(position));
		String instanceId = census.getInstanceId();

		Location censusLocation = new Location(instanceId);
		censusLocation.setAccuracy((float) census.getAccuracy());
		censusLocation.setAltitude(census.getAltitude());
		censusLocation.setLatitude(census.getLatitude());
		censusLocation.setLongitude(census.getLongitude());

		mDirectionProvider.setDestinationLocation(censusLocation);
		((CensusListAdapter) getListAdapter()).notifyDataSetChanged();
		if (mDirectionProvider.getCurrentLocation() != null) {
			updateDistance(mDirectionProvider.getCurrentLocation());
		} else {
			mDistanceTextView.setText(getActivity().getString(
					R.string.distance, "-"));
		}
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
				view = mInflater.inflate(R.layout.census_list_item, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.headNameTextView = (TextView) view
						.findViewById(R.id.headNameTextView);
				viewHolder.houseNoTextView = (TextView) view
						.findViewById(R.id.houseNoTextView);
				viewHolder.commentTextView = (TextView) view
						.findViewById(R.id.commentTextView);
				viewHolder.placeNameTextView = (TextView) view
						.findViewById(R.id.placeNameTextView);
				viewHolder.distanceTextView = (TextView) view
						.findViewById(R.id.distanceTextView);
				viewHolder.censusImageView = (ImageView) view
						.findViewById(R.id.censusImageView);
				viewHolder.censusInfoContainer = (RelativeLayout) view
						.findViewById(R.id.censusInfoContainer);
				viewHolder.censusInfoBaseContainer = (LinearLayout) view
						.findViewById(R.id.censusInfoBaseContainer);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			if (mSelectedCensusRow == position) {
				viewHolder.censusInfoContainer.setBackgroundColor(mContext
						.getResources().getColor(R.color.selected_row_blue));

				viewHolder.censusInfoBaseContainer
						.setBackgroundDrawable(mContext.getResources()
								.getDrawable(R.drawable.item_background_blue));
			} else {
				viewHolder.censusInfoContainer.setBackgroundColor(mContext
						.getResources().getColor(R.color.white));
				viewHolder.censusInfoBaseContainer
						.setBackgroundDrawable(mContext.getResources()
								.getDrawable(R.drawable.item_background_brown));
			}

			CensusModel census = getItem(position);

			if (census.getHeadName() != null) {
				viewHolder.headNameTextView.setText((census.getHeadName()
						.length() > 70 ? census.getHeadName().substring(0, 70)
						+ " ..." : census.getHeadName()));
			} else {
				viewHolder.headNameTextView.setText("-");
			}

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

			String distance = census.getDistance() == -1 ? "-" : String
					.valueOf(DistanceUtil.getFormatedDistance(census
							.getDistance()));
			viewHolder.distanceTextView.setText(mContext.getString(
					R.string.distance, distance));

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
		TextView distanceTextView;
		ImageView censusImageView;
		RelativeLayout censusInfoContainer;
		LinearLayout censusInfoBaseContainer;
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
		case 3:
			resourceId = R.drawable.ic_not_selected_done_by_friends;
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
		case 3:
			resourceId = R.drawable.ic_main_done_by_friends;
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
		case 3:
			resourceId = R.drawable.ic_alternate_done_by_friends;
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
		case 3:
			resourceId = R.drawable.ic_additional_done_by_friends;
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
		case 3:
			resourceId = R.drawable.ic_excluded_done_by_friends;
			break;
		}

		return resourceId;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == FILTER_REQUEST_CODE) {
			if (mFilterCensusList.size() == 0) {
				mFilterCensusList.add(FilterCensusList.MainPoint);
				mFilterCensusList.add(FilterCensusList.AdditionalPoint);
				mFilterCensusList.add(FilterCensusList.AlternatePoint);
			}
			mSelectedCensusRow = -1;
			mDirectionProvider.setDestinationLocation(null);
			mCensusLocation.setVisibility(View.INVISIBLE);
			getLoaderManager().restartLoader(CENSUS_LIST_LOADER, null, this);
		} else if (requestCode == PLACE_NAME_REQUEST_CODE) {
			if (data != null && data.hasExtra("selectedPlaceName")) {
				mSelectedPlaceName = data.getStringExtra("selectedPlaceName");
				mSelectedCode = data.getStringExtra("selectedCode");
				mSelectedCensusRow = -1;
				mDirectionProvider.setDestinationLocation(null);
				mCensusLocation.setVisibility(View.INVISIBLE);
				getLoaderManager()
						.restartLoader(CENSUS_LIST_LOADER, null, this);
			}
		}
	}

	OnRefreshListener onRefreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			if (mSort == Sort.FARTHEST || mSort == Sort.CLOSEST) {
				sortCensus(mSort);
			} else {
				int numOfCensus = (((CensusListAdapter) getListAdapter())
						.getCount());
				if (mDirectionProvider.getCurrentLocation() != null) {
					for (int i = 0; i < numOfCensus; i++) {
						CensusModel census = (((CensusListAdapter) getListAdapter())
								.getItem(i));
						census.setDistance(DistanceUtil.getDistance(
								mDirectionProvider.getCurrentLocation()
										.getLatitude(), mDirectionProvider
										.getCurrentLocation().getLongitude(),
								census.getLatitude(), census.getLongitude()));
					}

					((CensusListAdapter) getListAdapter())
							.notifyDataSetChanged();
				}
			}

			mRefreshLayout.setRefreshing(false);
		}
	};

	private void sortCensus(Sort sort) {
		mSort = sort;
		Bundle bundle = new Bundle();
		// bundle.putInt(SORT, mSort.ordinal());

		if (mDirectionProvider.getCurrentLocation() != null) {
			bundle.putDouble(CURRENT_LATITUDE, mDirectionProvider
					.getCurrentLocation().getLatitude());
			bundle.putDouble(CURRENT_LONGITUDE, mDirectionProvider
					.getCurrentLocation().getLongitude());
		}
		getLoaderManager().restartLoader(CENSUS_LIST_LOADER, bundle, this);
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
				menu.add(Menu.NONE, MENU_START_QUESTIONNAIRE, Menu.NONE,
						getActivity().getString(R.string.start_questionnaire));
				if (mEditSavedData == true) {
					menu.add(Menu.NONE, MENU_EDIT_POINT, menu.NONE,
							getActivity().getString(R.string.edit_point));
				}

				if (mMarkAsFinalizedByFriends == true
						&& MainMenuActivity.SELECTED_CENSUS.getProcessed() != 3) {
					menu.add(
							Menu.NONE,
							MENU_MARK_AS_FINALIZED_BY_FRIENDS,
							menu.NONE,
							getActivity().getString(
									R.string.mark_as_finalized_by_friends));
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_START_QUESTIONNAIRE:
			if (MainMenuActivity.SELECTED_CENSUS != null) {

				if (mFormId != null && mFormId.length() != 0) {
					SurveyUtil surveyUtil = new SurveyUtil();
					SurveyFormParameters surveyFormParameters = surveyUtil.new SurveyFormParameters(
							true, mFormId, null);

					Intent intentEdit = SurveyUtil
							.getIntentForOdkSurveyEditRow(getActivity(),
									"survey", surveyFormParameters,
									MainMenuActivity.SELECTED_CENSUS
											.getInstanceId());
					SurveyUtil.launchSurveyToEditRow(getActivity(), intentEdit,
							MainMenuActivity.SURVEY_REQUEST_CODE);
				} else {
					createAlertDialog(getActivity().getString(
							R.string.choose_form));
				}
			}
			return true;
		case MENU_EDIT_POINT:
			if (MainMenuActivity.SELECTED_CENSUS != null) {
				((ODKActivity) getActivity()).swapToCollectFragmentView();
			}
			return true;
		case MENU_MARK_AS_FINALIZED_BY_FRIENDS:
			confirmMarkAsFinalizedByFriends();
		}
		return super.onContextItemSelected(item);
	}

	public void confirmMarkAsFinalizedByFriends() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.navigate))
				.setMessage(getString(R.string.confirm_finalize_by_friends))
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
								CensusUtil.markAsFinalizedByFriends(Survey
										.getInstance().getApplicationContext(),
										MainMenuActivity.SELECTED_CENSUS);
								getLoaderManager().restartLoader(
										CENSUS_LIST_LOADER, null,
										NavigateFragment.this);
								dialog.cancel();
							}
						});
		mAlert = builder.create();
		mAlert.show();
	}

	public void updateShortInfoAboutQuestionnaireStatus() {
		int finalized = 0;
		int incomplete = 0;
		int notStarted = 0;

		int numOfCensus = (((CensusListAdapter) getListAdapter()).getCount());
		for (int i = 0; i < numOfCensus; i++) {
			CensusModel census = (((CensusListAdapter) getListAdapter())
					.getItem(i));
			switch (census.getProcessed()) {
			case 0:
				notStarted++;
				break;
			case 1:
				incomplete++;
				break;
			case 2:
			case 3:
				finalized++;
				break;
			}
		}

		mFinalizedTextView.setText(getActivity().getString(
				R.string.num_of_finalized_quest, finalized));
		mIncompleteTextView.setText(getActivity().getString(
				R.string.num_of_incomplete_quest, incomplete));
		mNotStartedTextView.setText(getActivity().getString(
				R.string.num_of_not_started_quest, notStarted));
	}

	/*
	 * @Override public void onIsCompassAccuracyLow(boolean accuracyLow) {
	 * if(isAdded()) { mCalibrationRequiredTextView.setVisibility(accuracyLow ?
	 * View.VISIBLE : View.GONE); } }
	 */
}
