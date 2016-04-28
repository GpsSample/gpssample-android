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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.path.common.android.data.PlaceModel;
import org.path.common.android.utilities.PlaceNameUtil;
import org.path.common.android.utilities.PlaceNameUtil.HirarchyOrder;
import org.path.episample.android.R;
import org.path.episample.android.utilities.PlaceSpinnerAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author belendia@gmail.com
 */

public class SelectPlaceNameActivity extends Activity {
	private static final String t = "SelectPlaceNameActivity";

	public static String PLACE_NAME = "placeName";
	public static String PLACE_CODE = "placeCode";
	public static String SHOW_CLEAR_BUTTON = "showClearButton";

	private LinearLayout mParentLinearLayout;
	private ArrayList<PlaceView> mPlaceViews = new ArrayList<PlaceView>();

	private ArrayList<String> mHierarchies;
	private ArrayList<PlaceModel> mPreviouslySelectedHierarchy;
	private ArrayList<String> mPlaceNameValueFromInstanceState;

	private RelativeLayout mButtonContainer;
	private RelativeLayout mNoPlaceNameContainer;
	private RelativeLayout mClearButtonContainer;
	private RelativeLayout mSelectButtonContainer;

	private String mPlaceNameCode = null;
	private String mPlaceHierarchy = "";

	private boolean mShowClearButton = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_select_place_name);

		mParentLinearLayout = (LinearLayout) findViewById(R.id.parentLinearLayout);
		mButtonContainer = (RelativeLayout) findViewById(R.id.buttonContainer);
		mNoPlaceNameContainer = (RelativeLayout) findViewById(R.id.noPlaceNameContainer);
		mClearButtonContainer = (RelativeLayout) findViewById(R.id.clearButtonContainer);
		mSelectButtonContainer = (RelativeLayout) findViewById(R.id.selectButtonContainer);

		if (savedInstanceState != null) {
			mPlaceNameValueFromInstanceState = savedInstanceState
					.getStringArrayList(PLACE_NAME);
			mShowClearButton = savedInstanceState.getBoolean(SHOW_CLEAR_BUTTON);
		} else {
			mPlaceNameCode = getIntent().getStringExtra(PLACE_CODE);
			if (mPlaceNameCode != null) {
				mPreviouslySelectedHierarchy = PlaceNameUtil
						.getHierarchiesDetail(this, mPlaceNameCode,
								HirarchyOrder.HigherToLower);
			}

			mShowClearButton = getIntent().getBooleanExtra(SHOW_CLEAR_BUTTON,
					false);
		}

		if (mShowClearButton == false) {
			mClearButtonContainer.setVisibility(View.GONE);
			mSelectButtonContainer.setPadding(0, 0, 0, 0);
			mButtonContainer.setGravity(Gravity.CENTER);
			mButtonContainer.setVisibility(View.INVISIBLE);
		} else {

			mButtonContainer.setVisibility(View.VISIBLE);
			mSelectButtonContainer.setVisibility(View.INVISIBLE);
		}

		mHierarchies = PlaceNameUtil.getAllHierarchies(this);

		for (int i = 0; i < mHierarchies.size(); i++) {
			createHierarchy(mHierarchies.get(i), i);
		}

		// if no hierarchies are defined then show to user to load place name in
		// the admin setting.
		if (mHierarchies.size() == 0) {
			mButtonContainer.setVisibility(View.GONE);
			mNoPlaceNameContainer.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPlaceNameValueFromInstanceState = new ArrayList<String>();
		for (PlaceView view : mPlaceViews) {
			mPlaceNameValueFromInstanceState.add(String.valueOf(view
					.getSpinner().getSelectedItemId()));
		}
		outState.putStringArrayList(PLACE_NAME,
				mPlaceNameValueFromInstanceState);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void createHierarchy(String name, int position) {
		PlaceView placeView = new PlaceView();
		RelativeLayout relativeLayout = new RelativeLayout(this);
		relativeLayout.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.item_background_state));
		// Defining the RelativeLayout layout parameters.
		// In this case I want to fill its parent
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 3, 0, 0);

		relativeLayout.setLayoutParams(layoutParams);
		placeView.setRelativeLayout(relativeLayout);

		// Creating a TextView
		TextView textView = new TextView(this);

		RelativeLayout.LayoutParams textViewLayoutParam = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(textViewLayoutParam);

		// textView.setPadding(5, 0, 0, 0);
		// textView.setTextAppearance(this,
		// android.R.style.TextAppearance_DeviceDefault_Small);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		textView.setText(name);
		// textView.setTextColor(Color.BLACK);
		textView.setTextColor(getResources().getColor(
				R.color.spinner_text_color));
		// textView.setTypeface(null, Typeface.BOLD);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			textView.setId(ViewIdUtils.generateViewId());
		} else {
			textView.setId(View.generateViewId());
		}

		relativeLayout.addView(textView);

		// Create a spinner
		Spinner spinner = new Spinner(this);
		RelativeLayout.LayoutParams spinnerLayoutParam = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		spinnerLayoutParam.addRule(RelativeLayout.BELOW, textView.getId());
		spinner.setLayoutParams(spinnerLayoutParam);
		spinner.setTag(position);
		spinner.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.spinner_background));
		spinner.setOnItemSelectedListener(spinnerItemSelectedListener);
		relativeLayout.addView(spinner);
		placeView.setSpinner(spinner);

		relativeLayout.setPadding(10, 10, 10, 20);

		mPlaceViews.add(placeView);

		// Setting the RelativeLayout as our content view
		mParentLinearLayout.addView(relativeLayout);

		if (position == 0) {
			List<PlaceModel> placeNameList = PlaceNameUtil.getAll(this, name,
					null);
			PlaceSpinnerAdapter placeNameSpinnerAdapter = new PlaceSpinnerAdapter(
					SelectPlaceNameActivity.this, placeNameList);
			spinner.setAdapter(placeNameSpinnerAdapter);
			setDefaultValue(spinner, position);// select the hierarchy if it was
												// previously selected.
		} else {
			relativeLayout.setVisibility(View.INVISIBLE);
		}
	}

	private void setDefaultValue(Spinner spinner, int position) {
		// select the hierarchy if it was previously selected.
		if (mPreviouslySelectedHierarchy != null
				&& mPreviouslySelectedHierarchy.size() > position) {
			try {
				long value = Long.parseLong(mPreviouslySelectedHierarchy.get(
						position).getCode());
				selectPlaceNameItem(spinner, value);
			} finally {

			}
		} else if (mPlaceNameValueFromInstanceState != null
				&& mPlaceNameValueFromInstanceState.size() > position) {
			try {
				long value = Long.parseLong(mPlaceNameValueFromInstanceState
						.get(position));
				if (value > -1) {
					selectPlaceNameItem(spinner, value);
				}
			} finally {

			}
		}
	}

	private void selectPlaceNameItem(Spinner spinner, long value) {
		PlaceSpinnerAdapter placeAdapter = (PlaceSpinnerAdapter) spinner
				.getAdapter();
		if (placeAdapter != null) {
			for (int pos = 0; pos < placeAdapter.getCount(); pos++) {
				if (placeAdapter.getItemId(pos) == value) {
					spinner.setSelection(pos);
					return;
				}
			}
		}
	}

	OnItemSelectedListener spinnerItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parentView, View v,
				int position, long id) {

			if (parentView.getTag() != null) {
				String selectedSpinnerIndexString = parentView.getTag()
						.toString();

				try {
					int selectedSpinnerIndex = Integer
							.parseInt(selectedSpinnerIndexString);
					if (position == 0) {
						setControlVisibility(View.INVISIBLE,
								selectedSpinnerIndex, id);
					} else {
						setControlVisibility(View.VISIBLE,
								selectedSpinnerIndex, id);
					}
				} catch (Exception ex) {
					Toast.makeText(SelectPlaceNameActivity.this,
							"Unable to show the next hierarchy",
							Toast.LENGTH_LONG).show();
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {

		}

	};

	private void setControlVisibility(int visibility, int position,
			long selectedId) {
		if (visibility == View.INVISIBLE) {
			for (int i = position + 1; i < mPlaceViews.size(); i++) {
				mPlaceViews.get(i).getRelativeLayout()
						.setVisibility(visibility);
			}

			if (mShowClearButton == false) {
				mButtonContainer.setVisibility(View.INVISIBLE);
			} else {
				mSelectButtonContainer.setVisibility(View.INVISIBLE);
			}

		} else if (visibility == View.VISIBLE) {
			if (position + 1 < mPlaceViews.size()) {
				List<PlaceModel> placeNameList = PlaceNameUtil.getAll(
						SelectPlaceNameActivity.this,
						mHierarchies.get(position + 1),
						String.valueOf(selectedId));
				PlaceSpinnerAdapter placeNameSpinnerAdapter = new PlaceSpinnerAdapter(
						SelectPlaceNameActivity.this, placeNameList);
				mPlaceViews.get(position + 1).getSpinner()
						.setAdapter(placeNameSpinnerAdapter);

				setDefaultValue(mPlaceViews.get(position + 1).getSpinner(),
						position + 1);
				mPlaceViews.get(position + 1).getRelativeLayout()
						.setVisibility(View.VISIBLE);
			}
			for (int i = position + 2; i < mPlaceViews.size(); i++) {
				mPlaceViews.get(i).getRelativeLayout()
						.setVisibility(View.INVISIBLE);
			}
			if (position == mPlaceViews.size() - 1) {
				if (mShowClearButton == false) {
					mButtonContainer.setVisibility(View.VISIBLE);
				} else {
					mSelectButtonContainer.setVisibility(View.VISIBLE);
				}

				mPlaceNameCode = String.valueOf(selectedId);
				mPlaceHierarchy = mHierarchies.get(position);
			} else {
				if (mShowClearButton == false) {
					mButtonContainer.setVisibility(View.INVISIBLE);
				} else {
					mSelectButtonContainer.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	// select place name click event listener.
	public void selectPlaceNameClickListener(View view) {
		Intent returnIntent = new Intent();
		String selectedCode = "";
		String selectedPlaceName = "";
		if (mPlaceNameCode != null) {
			selectedCode = String.valueOf(mPlaceNameCode);
			selectedPlaceName = PlaceNameUtil.getName(this, mPlaceHierarchy,
					mPlaceNameCode);
		}
		returnIntent.putExtra("selectedCode", selectedCode);
		returnIntent.putExtra("selectedPlaceName", selectedPlaceName);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	// select place name click event listener.
	public void clearPlaceNameClickListener(View view) {
		Intent returnIntent = new Intent();
		String selectedCode = "";
		String selectedPlaceName = "";
		returnIntent.putExtra("selectedCode", selectedCode);
		returnIntent.putExtra("selectedPlaceName", selectedPlaceName);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private class PlaceView {
		private RelativeLayout relativeLayout;

		public RelativeLayout getRelativeLayout() {
			return relativeLayout;
		}

		public void setRelativeLayout(RelativeLayout value) {
			relativeLayout = value;
		}

		private Spinner spinner;

		public Spinner getSpinner() {
			return spinner;
		}

		public void setSpinner(Spinner value) {
			spinner = value;
		}
	}

	// taken from
	// http://stackoverflow.com/questions/1714297/android-view-setidint-id-programmatically-how-to-avoid-id-conflicts
	private static class ViewIdUtils {
		private static final AtomicInteger sNextGeneratedId = new AtomicInteger(
				1);

		/**
		 * Generate a value suitable for use in {@link #setId(int)}. This value
		 * will not collide with ID values generated at build time by aapt for
		 * R.id.
		 * 
		 * @return a generated ID value
		 */
		public static int generateViewId() {
			for (;;) {
				final int result = sNextGeneratedId.get();
				// aapt-generated IDs have the high byte nonzero; clamp to the
				// range under that.
				int newValue = result + 1;
				if (newValue > 0x00FFFFFF)
					newValue = 1; // Roll over to 1, not 0.
				if (sNextGeneratedId.compareAndSet(result, newValue)) {
					return result;
				}
			}
		}
	}
}
