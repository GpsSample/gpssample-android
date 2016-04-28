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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.path.common.android.data.CensusModel;
import org.path.common.android.logic.PropertyManager;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.PlaceNameUtil;
import org.path.common.android.utilities.WebLogger;
import org.path.episample.android.R;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.activities.SelectPlaceNameActivity;
import org.path.episample.android.application.Survey;
import org.path.episample.android.fragments.AlertDialogFragment.ConfirmAlertDialog;
import org.path.episample.android.logic.DynamicPropertiesCallback;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;
import org.path.episample.android.preferences.PreferencesActivity;
import org.path.episample.android.utilities.TimeUtil;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.todddavies.components.progressbar.ProgressWheel;

/**
 * Fragment displaying them main modules in the app.
 * 
 * @author belendia@gmail.com
 * 
 */
public class CollectFragment extends Fragment implements LocationListener,
		ConfirmAlertDialog {

	@SuppressWarnings("unused")
	private static final String t = "CollectFragment";
	public static final int ID = R.layout.collect;
	private View view;

	private static final String DIALOG_MSG = "dialogmsg";
	private static final String PLACE_NAME_SELECTED = "placeNameSelected";
	private static final String POSITION = "position";
	private static final String NEW_RECORD = "newRecord";
	private static final String DONT_FREEZE = "dontFreeze";

	private String mAlertMsg;
	private DialogState mDialogState = DialogState.None;

	// default location accuracy
	private static final double GREEN_LOCATION_ACCURACY = 10;
	private static final double YELLOW_LOCATION_ACCURACY = 50;
	protected static final int REQUEST_CODE = 67432;

	private ProgressWheel mSignalQualitySpinner;
	private ProgressWheel mLargeSignalQualitySpinner;
	private ProgressWheel mSmallSignalQualitySpinner;

	private TextView mLatValTextView;
	private TextView mLngValTextView;
	private TextView mAltValTextView;

	private TextView mPlaceNameTextView;
	private TextView mHouseNumberTextView;
	private TextView mHeadNameTextView;
	private TextView mCommentTextView;

	private TextView mPositionTextView;

	private TextView mTotalValidTextView;
	private TextView mTotalInvalidTextView;
	private TextView mTotalExcludedTextView;

	private TextView mDelayCounterTextView;

	private RelativeLayout mSaveButtonContainer;
	private RelativeLayout mDelayContainer;
	private ScrollView mDataEntryContainer;

	// private RelativeLayout mExcludeButtonContainer;

	private ImageView mLatImageView;
	private ImageView mLngImageView;
	private ImageView mAltImageView;

	private ImageButton mPreviousImageButton;
	private ImageButton mNextImageButton;
	private ImageButton mLastPlusOneButton;
	private ImageButton mSaveImageButton;
	// private ImageButton mExcludeImageButton;
	private ImageButton mPlaceNameImageButton;

	private CheckBox mExcludeCheckBox;

	private LocationManager mLocationManager;
	private Location mLocation;
	private String mAppName;
	private String mInterviewerName;
	private String mDeviceName;
	private String mPhoneNumber;
	private boolean mIsGPSOn = false;
	private boolean mIsNetworkOn = false;
	private boolean mInvalidateCursor = true;
	private boolean mNewRecord = true;
	private boolean mEditSavedData = false;
	private boolean mDeviceHasLowQualityGPS = false;
	private Freeze mDontFreeze = Freeze.False;
	private int mMinutes = 0;
	private int mSeconds = 0;

	// private boolean mExcludePoints = false;
	private boolean mAutoIncrementHouseNumber = true;
	private boolean mCommentMandatory = false;

	private PropertyManager mPropertyManager;
	private DynamicPropertiesCallback mCB;

	ArrayList<CensusModel> mCensuses = new ArrayList<CensusModel>();
	int mPosition = 0;

	double mLatitude = 0;
	double mLongitude = 0;
	double mAltitude = 0;
	double mAccuracy = 0;
	int mGPSAccuracyThresholds = 10;

	Timer mCountDownTimer;

	AlertDialog mAlert;

	String mPlaceNameCode;

	private enum Color {
		Green, Yellow, Red, Black
	};

	private static enum DialogState {
		Alert, None
	};

	private static enum Freeze {
		False, FreezeNext, True
	};
	
	private ArrayList<String> mGeoHierarchy = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppName = getActivity().getIntent().getStringExtra(
				MainMenuActivity.APP_NAME);
		getActivity().setTitle(R.string.collect_data);
		if (mAppName == null || mAppName.length() == 0) {
			mAppName = "Survey";
		}
		mPropertyManager = new PropertyManager(getActivity()
				.getApplicationContext());
		mCB = new DynamicPropertiesCallback(getActivity(), mAppName, null, null);

		mGeoHierarchy = PlaceNameUtil.getAllHierarchies(getActivity()
				.getApplicationContext());
		
		WebLogger.getLogger(mAppName).i(t, t + ".onCreate appName=" + mAppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(ID, container, false);

		mLargeSignalQualitySpinner = (ProgressWheel) view
				.findViewById(R.id.largeSignalQualitySpinner);
		mSmallSignalQualitySpinner = (ProgressWheel) view
				.findViewById(R.id.smallSignalQualitySpinner);
		String get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_DEVICE_HAS_LOW_QUALITY_GPS);
		if (get != null && get.equalsIgnoreCase("true")) {
			mSignalQualitySpinner = mSmallSignalQualitySpinner;
		} else {
			mSignalQualitySpinner = mLargeSignalQualitySpinner;
		}

		mDataEntryContainer = (ScrollView) view
				.findViewById(R.id.dataEntryContainer);
		mDelayContainer = (RelativeLayout) view
				.findViewById(R.id.delayContainer);

		mLatValTextView = (TextView) view.findViewById(R.id.latValTextView);
		mLngValTextView = (TextView) view.findViewById(R.id.lngValTextView);
		mAltValTextView = (TextView) view.findViewById(R.id.altValTextView);

		mPlaceNameTextView = (TextView) view
				.findViewById(R.id.placeNameTextView);
		mPlaceNameTextView.setOnClickListener(selectPlaceNameClickListener);

		mHouseNumberTextView = (TextView) view
				.findViewById(R.id.houseNumberEditText);
		mHeadNameTextView = (TextView) view.findViewById(R.id.headNameEditText);
		mCommentTextView = (TextView) view.findViewById(R.id.commentEditText);

		mPositionTextView = (TextView) view.findViewById(R.id.positionTextView);
		mTotalValidTextView = (TextView) view
				.findViewById(R.id.totalValidTextView);
		mTotalInvalidTextView = (TextView) view
				.findViewById(R.id.totalInalidTextView);
		mTotalExcludedTextView = (TextView) view
				.findViewById(R.id.totalExcludedTextView);

		mDelayCounterTextView = (TextView) view
				.findViewById(R.id.delayCounterTextView);
		Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/digital.ttf");
		mDelayCounterTextView.setTypeface(font);

		mPreviousImageButton = (ImageButton) view
				.findViewById(R.id.previousButton);
		mNextImageButton = (ImageButton) view.findViewById(R.id.nextButton);
		mLastPlusOneButton = (ImageButton) view
				.findViewById(R.id.lastPlusOneButton);
		mSaveImageButton = (ImageButton) view
				.findViewById(R.id.saveImageButton);
		mSaveImageButton.setOnClickListener(saveButtonClickListener);

		mExcludeCheckBox = (CheckBox) view.findViewById(R.id.excludeCheckBox);
		/*
		 * mExcludeImageButton = (ImageButton) view
		 * .findViewById(R.id.excludeImageButton);
		 * mExcludeImageButton.setOnClickListener(excludeButtonClickListener);
		 */

		mPlaceNameImageButton = (ImageButton) view
				.findViewById(R.id.placeNameImageButton);
		mPlaceNameImageButton.setOnClickListener(selectPlaceNameClickListener);

		mSaveButtonContainer = (RelativeLayout) view
				.findViewById(R.id.saveButtonContainer);
		mSaveButtonContainer.setOnClickListener(saveButtonClickListener);

		/*
		 * mExcludeButtonContainer = (RelativeLayout) view
		 * .findViewById(R.id.excludeButtonContainer);
		 * mExcludeButtonContainer.setOnClickListener
		 * (excludeButtonClickListener);
		 */

		mPreviousImageButton.setOnClickListener(previousButtonClickListener);
		mNextImageButton.setOnClickListener(nextButtonClickListener);
		mLastPlusOneButton.setOnClickListener(lastPlusOneClickListener);

		mLatImageView = (ImageView) view.findViewById(R.id.latImageView);
		mLngImageView = (ImageView) view.findViewById(R.id.lngImageView);
		mAltImageView = (ImageView) view.findViewById(R.id.altImageView);

		if (savedInstanceState != null) {
			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}

			// restore place name textview
			if (savedInstanceState.containsKey(PLACE_NAME_SELECTED)) {
				mPlaceNameTextView.setText(savedInstanceState
						.getString(PLACE_NAME_SELECTED));
			}

			// restore position
			if (savedInstanceState.containsKey(POSITION)) {
				mPosition = savedInstanceState.getInt(POSITION);
				mInvalidateCursor = false;
			}

			if (savedInstanceState.containsKey(NEW_RECORD)) {
				mNewRecord = savedInstanceState.getBoolean(NEW_RECORD);
			}

			if (savedInstanceState.containsKey(DONT_FREEZE)) {
				mDontFreeze = (Freeze) savedInstanceState
						.getSerializable(DONT_FREEZE);
			}
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mLocationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		mCensuses = CensusUtil.getAll(getActivity());

		// display the selected point in navigate module to edit
		if (MainMenuActivity.EDIT_POINT == true) {
			MainMenuActivity.EDIT_POINT = false;
			if (MainMenuActivity.SELECTED_CENSUS != null) {
				int tempPosition = getPosition(MainMenuActivity.SELECTED_CENSUS
						.getInstanceId());
				if (tempPosition > -1) {
					mPosition = tempPosition;
					mNewRecord = false;
					mInvalidateCursor = false;
					displayContent();
				}
			}
		}

		// make sure we have a good location provider before continuing
		List<String> providers = mLocationManager.getProviders(true);

		for (String provider : providers) {
			if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
				mIsGPSOn = true;
			} else if (provider
					.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
				mIsNetworkOn = true;
			}
		}

		if (mIsGPSOn == false && mIsNetworkOn == false) {
			setSpinnerColor(Color.Black);
			setColorLocation(Color.Black);
			Toast.makeText(getActivity(),
					getString(R.string.provider_disabled_error),
					Toast.LENGTH_SHORT).show();
		} else {
			setSpinnerColor(Color.Red);
			setColorLocation(Color.Red);
		}

		if (mNewRecord) {
			displayNewRecordInPositionTextView(); // display new record in
													// position textview
		} else {
			displayPosition(); // display current position in position textview
		}

		// setEnableExcludeButton(false);
		refreshTotalShortInfo();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DIALOG_MSG, mAlertMsg);
		outState.putString(PLACE_NAME_SELECTED, mPlaceNameTextView.getText()
				.toString());
		outState.putInt(POSITION, mPosition);
		outState.putBoolean(NEW_RECORD, mNewRecord);
		outState.putSerializable(DONT_FREEZE, mDontFreeze);
	}

	@Override
	public void onResume() {
		super.onResume();

		String get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_DEVICE_HAS_LOW_QUALITY_GPS);
		if (get != null && get.equalsIgnoreCase("true")) {
			mDeviceHasLowQualityGPS = true;
		} else {
			mDeviceHasLowQualityGPS = false;
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_DELAY);
		if (get != null) {
			mMinutes = TimeUtil.getMinutes(get);
			mSeconds = TimeUtil.getSeconds(get);
		} else {
			mMinutes = 0;
			mSeconds = 30;
		}

		if (mDeviceHasLowQualityGPS == true && mDontFreeze == Freeze.False) {
			mSignalQualitySpinner = mSmallSignalQualitySpinner;
			mDelayCounterTextView.setText(String.format("%02d", mMinutes) + ":"
					+ String.format("%02d", mSeconds));
			mDelayContainer.setVisibility(View.VISIBLE);
			mDataEntryContainer.setVisibility(View.GONE);
			mCountDownTimer = new Timer(TimeUtil.convertToMilliSeconds(
					mMinutes, mSeconds), 1000);
			mCountDownTimer.start();
		} else {
			mSignalQualitySpinner = mLargeSignalQualitySpinner;
			mDataEntryContainer.setVisibility(View.VISIBLE);
			mDelayContainer.setVisibility(View.GONE);
		}

		if (mDontFreeze == Freeze.FreezeNext)
			mDontFreeze = Freeze.False;

		startSpin();

		if (mIsGPSOn) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
		}
		if (mIsNetworkOn) {
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_AUTO_INCREMENT_HOUSE_NUMBER);
		if (get != null && get.equalsIgnoreCase("false")) {
			mAutoIncrementHouseNumber = false;
		} else {
			mAutoIncrementHouseNumber = true;
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_EDIT_SAVED_DATA);
		if (get != null && get.equalsIgnoreCase("true")) {
			mEditSavedData = true;
		} else {
			mEditSavedData = false;
		}

		if (mNewRecord == false) {
			setEnableSaveButton(mEditSavedData);
		}

		get = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_COMMENT_MANDATORY);
		if (get != null && get.equalsIgnoreCase("true")) {
			mCommentMandatory = true;
		} else {
			mCommentMandatory = false;
		}

		get = PropertiesSingleton.getProperty("survey",
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

		mInterviewerName = "";
		get = PropertiesSingleton.getProperty("survey",
				PreferencesActivity.KEY_INTERVIEWER_NAME);
		if (get != null) {
			mInterviewerName = get;
		}

		mDeviceName = "";
		get = PropertiesSingleton.getProperty("survey",
				PreferencesActivity.KEY_DEVICE_NAME);
		if (get != null) {
			mDeviceName = get;
		}

		mPhoneNumber = "";
		get = PropertiesSingleton.getProperty("survey",
				PreferencesActivity.KEY_PHONE_NUMBER);
		if (get != null) {
			mPhoneNumber = get;
		}

	}

	private void startSpin() {
		if (mIsGPSOn == true || mIsNetworkOn == true) {
			mSignalQualitySpinner.spin();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// stops the GPS. Note that this will turn off the GPS if the screen
		// goes to sleep.
		mLocationManager.removeUpdates(this);
		mAccuracy = 0;
		mLatitude = 0;
		mLongitude = 0;
		mAltitude = 0;

		if (mIsGPSOn == true || mIsNetworkOn == true) {
			mSignalQualitySpinner.stopSpinning();
			mSignalQualitySpinner.setText(getString(R.string.acc_value));
			mLatValTextView.setText(getString(R.string.lat_value));
			mLngValTextView.setText(getString(R.string.lng_value));
			mAltValTextView.setText(getString(R.string.alt_value));
			setSpinnerColor(Color.Red);
			setColorLocation(Color.Red);
		}

		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
		}
	}

	public OnClickListener selectPlaceNameClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(getActivity(),
					SelectPlaceNameActivity.class);
			if (mPlaceNameCode != null && mPlaceNameCode.length() > 0) {				
				intent.putExtra(SelectPlaceNameActivity.PLACE_CODE,
						mPlaceNameCode);
				intent.putExtra(SelectPlaceNameActivity.PLACE_NAME,
						mPlaceNameTextView.getText().toString());
			}
			mDontFreeze = Freeze.True;
			startActivityForResult(intent, REQUEST_CODE);
		}
	};

	public OnClickListener saveButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (validateUserInput()) {
				if (!(mAccuracy > 0 && mAccuracy <= mGPSAccuracyThresholds)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setTitle(getString(R.string.collect_data))
							.setMessage(
									getString(R.string.invalid_gps_data,
											truncateDouble(mAccuracy, 2)))
							.setCancelable(false)
							.setIcon(android.R.drawable.ic_dialog_info)
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									})
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											save();
											dialog.cancel();
										}
									});
					mAlert = builder.create();
					mAlert.show();
				} else {
					save();
				}
			}
		}
	};

	private void save() {
		CensusModel census = new CensusModel();
		if (mNewRecord == false) {
			if (mPosition < mCensuses.size() && mPosition != -1)
				census = mCensuses.get(mPosition);
		} else {
			census.setProcessed(0);
		}

		census.setPlaceName(mPlaceNameCode);
		census.setHouseNumber(mHouseNumberTextView.getText().toString());
		census.setHeadName(mHeadNameTextView.getText().toString());
		census.setComment(mCommentTextView.getText().toString().length() > 0 ? mCommentTextView
				.getText().toString() : null);
		census.setExcluded(mExcludeCheckBox.isChecked() == true ? 1 : 0);
		census.setInterviewerName(mInterviewerName != null
				&& mInterviewerName.length() > 0 ? mInterviewerName : null);

		census.setLatitude(mLatitude);
		census.setLongitude(mLongitude);
		census.setAltitude(mAltitude);
		census.setAccuracy(mAccuracy);

		census.setValid(census.getAccuracy() > 0
				&& census.getAccuracy() <= mGPSAccuracyThresholds ? 1 : 2);

		census.setInterviewerName(mInterviewerName != null
				&& mInterviewerName.length() > 0 ? mInterviewerName : null);
		census.setDeviceName(mDeviceName != null && mDeviceName.length() > 0 ? mDeviceName
				: null);

		String deviceId = mPropertyManager.getSingularProperty(
				PropertyManager.DEVICE_ID_PROPERTY, mCB);
		census.setDeviceId(deviceId != null && deviceId.length() > 0 ? deviceId
				: null);

		census.setPhoneNumber(mPhoneNumber != null && mPhoneNumber.length() > 0 ? mPhoneNumber
				: null);// mPropertyManager.getSingularProperty(PropertyManager.PHONE_NUMBER_PROPERTY,
						// mCB));
		census.setFormName(census.getPlaceName() + " > " + census.getHeadName()
				+ " (" + census.getHouseNumber() + ")");

		try {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.SSS");
			String currentDate = sdf.format(calendar.getTime());

			if (mNewRecord == true) {
				census.setInstanceId("uuid:" + UUID.randomUUID().toString());
				census.setCreatedDate(currentDate);
			} else {
				census.setUpdatedDate(currentDate);
			}

			CensusUtil.insertOrUpdate(getActivity(), census, true);
			if (mNewRecord == false) {
				int messageId = census.getValid() == 1 ? R.string.updated_successfully
						: R.string.invalid_data_saved;
				createAlertDialog(getString(messageId));
			} else {
				int messageId = census.getValid() == 1 ? R.string.saved_successfully
						: R.string.invalid_data_saved;
				createAlertDialog(getString(messageId));
				updateScreenWithLastPlusOneResult();
				displayNewRecordInPositionTextView();

				mInvalidateCursor = true;
				// setEnableExcludeButton(false);
			}

			refreshData();
			refreshTotalShortInfo();

			mHeadNameTextView.requestFocus();
			// if(mNewRecord == true) mNewRecord = false;
		} finally {
		}
	}

	private void displayNewRecordInPositionTextView() {
		mPositionTextView.setText(getString(R.string.new_record));
		mPositionTextView.setTextColor(getResources().getColor(R.color.black));
	}

	/*
	 * private void excludeFromSample() { if (mPosition >= 0) { CensusModel
	 * census = mCensuses.get(mPosition);
	 * CensusUtil.excludeFromSample(getActivity(), census.getInstanceId());
	 * refreshData();
	 * 
	 * //if (mPosition > 0) { // movePrevious(); //} else if (mPosition == 0 &&
	 * mCensuses.size() > 0) { // mInvalidateCursor = true; // moveNext(); //}
	 * else { // lastPlusOneClicked(); //}
	 * 
	 * refreshTotalShortInfo(); displayPosition(); } }
	 */

	private OnClickListener previousButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			movePrevious();
		}
	};

	private void movePrevious() {
		if (mInvalidateCursor == true) {
			mInvalidateCursor = false;
			mPosition = mCensuses.size();
		}
		mPosition--;
		if (mPosition > -1) {
			displayContent();
			displayPosition();
		} else {
			mPosition = 0;
		}

		if (mNewRecord == true)
			mNewRecord = false;

		setEnableSaveButton(mEditSavedData);
		/*
		 * if (mCensuses.size() > 0) { setEnableExcludeButton(mExcludePoints); }
		 */
	}

	private OnClickListener nextButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			moveNext();
		}
	};

	private void moveNext() {
		if (mInvalidateCursor == true) {
			mInvalidateCursor = false;
			mPosition = -1;
		}
		mPosition++;
		if (mPosition < mCensuses.size()) {
			displayContent();
			displayPosition();
		} else {
			mPosition = mCensuses.size() - 1;
		}

		// if save is clicked on the currently showing record, it will not
		// be considered a new record.
		if (mNewRecord == true)
			mNewRecord = false;

		setEnableSaveButton(mEditSavedData);
		/*
		 * if (mCensuses.size() > 0) { setEnableExcludeButton(mExcludePoints); }
		 */
	}

	private OnClickListener lastPlusOneClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			lastPlusOneClicked();
		}
	};

	private void lastPlusOneClicked() {
		if (mAutoIncrementHouseNumber == false) {
			clearDisplay();
		} else {
			updateScreenWithLastPlusOneResult();
		}

		displayNewRecordInPositionTextView();
		// mark the current record which is going to be entered as new.
		if (mNewRecord == false)
			mNewRecord = true;
		mPosition = mCensuses.size();
		setEnableSaveButton(true);
		// setEnableExcludeButton(false);
	}

	private void updateScreenWithLastPlusOneResult() {
		clearDisplay();
		CensusModel census = CensusUtil
				.getLastRecordHouseNumberPlusOne(getActivity());
		if (census != null) {
			int houseNo = 0;
			try {
				houseNo = Integer.parseInt(census.getHouseNumber());
			} catch (Exception ex) {
			}
			houseNo++;
			mHouseNumberTextView.setText(String.valueOf(houseNo));
			mPlaceNameCode = census.getPlaceName();
			
			if (mGeoHierarchy != null && mGeoHierarchy.size() > 0
					&& census.getPlaceName() != null) {
				String name = PlaceNameUtil.getName(getActivity()
						.getApplicationContext(), mGeoHierarchy.get(mGeoHierarchy
						.size() - 1), census.getPlaceName());
				mPlaceNameTextView.setText(name);
			}
		}
	}

	private void displayContent() {
		CensusModel census = mCensuses.get(mPosition);
		if (mGeoHierarchy != null && mGeoHierarchy.size() > 0
				&& census.getPlaceName() != null) {
			String name = PlaceNameUtil.getName(getActivity()
					.getApplicationContext(), mGeoHierarchy.get(mGeoHierarchy
					.size() - 1), census.getPlaceName());
			mPlaceNameTextView.setText(name);
			mPlaceNameCode = census.getPlaceName();
		}
		
		mHouseNumberTextView.setText(census.getHouseNumber());
		mHeadNameTextView.setText(census.getHeadName());
		mCommentTextView.setText(census.getComment());
		mExcludeCheckBox.setChecked(census.getExcluded() == 1 ? true : false);
	}

	private void displayPosition() {
		if (mPosition < mCensuses.size() && mCensuses.size() > 0
				&& mPosition >= 0) {
			CensusModel census = mCensuses.get(mPosition);
			String status = "";
			int color = getResources().getColor(R.color.black);
			if (census.getExcluded() == 1) {
				status = "E";
				color = getResources().getColor(R.color.total_info);
			} else if (census.getValid() == 1) {
				status = "V";
				color = getResources().getColor(
						R.color.spinner_text_color_green);
			} else if (census.getValid() == 2) {
				status = "I";
				color = getResources().getColor(R.color.spinner_text_color_red);
			}

			mPositionTextView.setText(getString(R.string.census_record,
					mPosition + 1, mCensuses.size(), status));
			mPositionTextView.setTextColor(color);
		} else {// if no points are collected
			mPositionTextView.setText(getString(R.string.census_record,
					mPosition, mCensuses.size(), "Empty"));
		}
	}

	private void clearDisplay() {
		mHouseNumberTextView.setText("");
		mHeadNameTextView.setText("");
		mCommentTextView.setText("");
		mExcludeCheckBox.setChecked(false);
	}

	private void refreshTotalShortInfo() {
		mTotalValidTextView.setText(String.valueOf(CensusUtil
				.getTotalValid(getActivity())));
		mTotalInvalidTextView.setText(String.valueOf(CensusUtil
				.getTotalInvalid(getActivity())));
		mTotalExcludedTextView.setText(String.valueOf(CensusUtil
				.getTotalExcluded(getActivity())));
	}

	@SuppressWarnings("deprecation")
	private void setEnableSaveButton(boolean value) {
		Drawable imagePart = null;
		Drawable textPart = null;
		if (value == true) {
			imagePart = getResources().getDrawable(
					R.drawable.left_border_blue_part_of_button);
			textPart = getResources().getDrawable(
					R.drawable.curve_border_blue_button);
		} else {
			imagePart = getResources().getDrawable(
					R.drawable.left_border_disabled_part_of_button);
			textPart = getResources().getDrawable(
					R.drawable.curve_border_disabled_button);
		}

		mSaveImageButton.setBackgroundDrawable(imagePart);
		mSaveButtonContainer.setBackgroundDrawable(textPart);

		mSaveButtonContainer.setEnabled(value);
		mSaveImageButton.setEnabled(value);
	}

	/*
	 * @SuppressWarnings("deprecation") private void
	 * setEnableExcludeButton(boolean value) { Drawable imagePart = null;
	 * Drawable textPart = null; if (value == true) { imagePart =
	 * getResources().getDrawable( R.drawable.left_border_blue_part_of_button);
	 * textPart = getResources().getDrawable(
	 * R.drawable.curve_border_blue_button); } else { imagePart =
	 * getResources().getDrawable(
	 * R.drawable.left_border_disabled_part_of_button); textPart =
	 * getResources().getDrawable( R.drawable.curve_border_disabled_button); }
	 * 
	 * mExcludeImageButton.setBackgroundDrawable(imagePart);
	 * mExcludeButtonContainer.setBackgroundDrawable(textPart);
	 * 
	 * mExcludeImageButton.setEnabled(value);
	 * mExcludeButtonContainer.setEnabled(value); }
	 */

	private boolean validateUserInput() {
		if (mPlaceNameTextView.getText().toString().trim().length() == 0) {
			createAlertDialog(getString(
					R.string.user_input_validation_error_message,
					getString(R.string.place_name)));
			mHouseNumberTextView.requestFocus();
			return false;
		}

		if (mHouseNumberTextView.getText().toString().trim().length() == 0) {
			createAlertDialog(getString(
					R.string.user_input_validation_error_message,
					getString(R.string.house_no)));
			mHouseNumberTextView.requestFocus();
			return false;
		}

		if (mHouseNumberTextView.getText().toString().trim().length() >= 0) {
			try {
				int hno = Integer.parseInt(mHouseNumberTextView.getText()
						.toString().trim());
				if (hno == 0) {
					createAlertDialog(getString(R.string.house_no_cant_be_zero,
							getString(R.string.house_no)));
					mHouseNumberTextView.requestFocus();
					return false;
				}
			} catch (Exception ex) {
				createAlertDialog(getString(R.string.enter_numeric_value,
						getString(R.string.house_no)));
				mHouseNumberTextView.requestFocus();
				return false;
			}
		}

		if (mHeadNameTextView.getText().toString().trim().length() == 0) {
			createAlertDialog(getString(
					R.string.user_input_validation_error_message,
					getString(R.string.head_name)));
			mHeadNameTextView.requestFocus();
			return false;
		}

		if (mCommentMandatory == true) {
			if (mCommentTextView.getText().toString().trim().length() == 0) {
				createAlertDialog(getString(
						R.string.user_input_validation_error_message,
						getString(R.string.comment)));
				mCommentTextView.requestFocus();
				return false;
			}
		}

		return true;
	}

	private void refreshData() {
		mCensuses = CensusUtil.getAll(getActivity());
	}

	@Override
	public void onLocationChanged(Location location) {
		mLocation = location;
		updateNotification();
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
		if (provider.equals("gps")) {
			mIsGPSOn = false;
		} else if (provider.equals("network")) {
			mIsNetworkOn = false;
		}

		if (mIsGPSOn == false && mIsNetworkOn == false) {
			updateNotification();

			mSignalQualitySpinner.stopSpinning();
			mSignalQualitySpinner.setText(Survey.getInstance().getString(
					R.string.acc_value));
			mLatValTextView.setText(Survey.getInstance().getString(
					R.string.lat_value));
			mLngValTextView.setText(Survey.getInstance().getString(
					R.string.lng_value));
			mAltValTextView.setText(Survey.getInstance().getString(
					R.string.alt_value));
			setSpinnerColor(Color.Black);
			setColorLocation(Color.Black);
			resetLocationInfoToZero();
		}
	}

	private void resetLocationInfoToZero() {
		mLatitude = 0;
		mLongitude = 0;
		mAltitude = 0;
		mAccuracy = 0;
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (provider.equals("gps")) {
			mIsGPSOn = true;
		} else if (provider.equals("network")) {
			mIsNetworkOn = true;
		}

		if (mIsGPSOn == true || mIsNetworkOn == true) {
			updateNotification();
			setSpinnerColor(Color.Red);
			setColorLocation(Color.Red);
			mSignalQualitySpinner.spin();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		case LocationProvider.AVAILABLE:
			if (mLocation != null) {
				updateNotification();
			}
			break;
		case LocationProvider.OUT_OF_SERVICE:
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			break;
		}
	}

	private void updateNotification() {
		if (isAdded() && mLocation != null) {
			try {
				mSignalQualitySpinner.setText(truncateDouble(
						mLocation.getAccuracy(), 2)
						+ " " + getString(R.string.meter));
				mLatValTextView.setText(truncateDouble(mLocation.getLatitude(),
						4));
				mLngValTextView.setText(truncateDouble(
						mLocation.getLongitude(), 4));
				mAltValTextView.setText(truncateDouble(mLocation.getAltitude(),
						2) + " " + getString(R.string.meter));

				setColorBasedOnAccuracy();

				mLatitude = mLocation.getLatitude();
				mLongitude = mLocation.getLongitude();
				mAltitude = mLocation.getAltitude();
				mAccuracy = mLocation.getAccuracy();
			} finally {

			}
		}
	}

	private void setColorBasedOnAccuracy() {
		if (mLocation != null) {
			if (mLocation.getAccuracy() > 0
					&& mLocation.getAccuracy() <= GREEN_LOCATION_ACCURACY) {
				setSpinnerColor(Color.Green);// set the spinner to green to
												// indicate that accuracy is
												// good enough
				setColorLocation(Color.Green);
			} else if (mLocation.getAccuracy() > GREEN_LOCATION_ACCURACY
					&& mLocation.getAccuracy() <= YELLOW_LOCATION_ACCURACY) {
				setSpinnerColor(Color.Yellow);
				setColorLocation(Color.Yellow);
			} else if (mLocation.getAccuracy() > YELLOW_LOCATION_ACCURACY) {
				setSpinnerColor(Color.Red);
				setColorLocation(Color.Red);
			} else {
				setSpinnerColor(Color.Black);
				setColorLocation(Color.Black);
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

	@SuppressWarnings("deprecation")
	private void setColorLocation(Color color) {
		int colorResourceId = 0;
		int backgroundColorResourceId = 0;
		Drawable backStyleResourceId = null;
		if (color == Color.Green) {
			colorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.text_black);
			backgroundColorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.light_green);
			backStyleResourceId = Survey.getInstance().getResources()
					.getDrawable(R.drawable.styled_green_button);
		} else if (color == Color.Yellow) {
			colorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.text_black);
			backgroundColorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.light_yellow);
			backStyleResourceId = Survey.getInstance().getResources()
					.getDrawable(R.drawable.styled_yellow_button);
		} else if (color == Color.Red) {
			colorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.text_black);
			backgroundColorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.light_red);
			backStyleResourceId = Survey.getInstance().getResources()
					.getDrawable(R.drawable.styled_red_button);
		} else {
			colorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.white_color);
			backgroundColorResourceId = Survey.getInstance().getResources()
					.getColor(R.color.light_black);
			backStyleResourceId = Survey.getInstance().getResources()
					.getDrawable(R.drawable.styled_black_button);
		}

		mLatValTextView.setTextColor(colorResourceId);
		mLngValTextView.setTextColor(colorResourceId);
		mAltValTextView.setTextColor(colorResourceId);

		mLatValTextView.setBackgroundColor(backgroundColorResourceId);
		mLngValTextView.setBackgroundColor(backgroundColorResourceId);
		mAltValTextView.setBackgroundColor(backgroundColorResourceId);

		mLatImageView.setBackgroundDrawable(backStyleResourceId);
		mLngImageView.setBackgroundDrawable(backStyleResourceId);
		mAltImageView.setBackgroundDrawable(backStyleResourceId);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE) {
			if (resultCode == -1) {// RESULT_OK){
				mPlaceNameTextView.setText(data
						.getStringExtra("selectedPlaceName"));
				mPlaceNameCode = data.getStringExtra("selectedCode");
			}

			mDontFreeze = Freeze.FreezeNext;
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
					Survey.getInstance().getString(R.string.collect_data),
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

	private int getPosition(String instanceId) {
		int totalCensus = mCensuses.size();
		for (int i = 0; i < totalCensus; i++) {
			if (mCensuses.get(i).getInstanceId().equals(instanceId)) {
				return i;
			}
		}

		return -1;
	}

	class Timer extends CountDownTimer {

		public Timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);

		}

		@Override
		public void onFinish() {
			mSignalQualitySpinner = mLargeSignalQualitySpinner;

			mDataEntryContainer.setVisibility(View.VISIBLE);
			mDelayContainer.setVisibility(View.GONE);
			startSpin();
			setColorBasedOnAccuracy();
			if (mLocation != null) {
				mSignalQualitySpinner.setText(truncateDouble(
						mLocation.getAccuracy(), 2)
						+ " " + getString(R.string.meter));
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mDelayCounterTextView.setText(TimeUtil
					.convertToMinuteAndSecond(millisUntilFinished));
		}
	}
}