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

import org.path.common.android.utilities.CensusUtil.FilterCensusList;
import org.path.episample.android.R;
import org.path.episample.android.fragments.NavigateFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/*
 * @author belendia@gmail.com
 */

public class FilterCensusListActivity extends Activity {
	private CheckBox mMainPointCheckBox;
	private CheckBox mAdditionalPointCheckBox;
	private CheckBox mAlternatePointCheckBox;
	private CheckBox mExcludedPointCheckBox;
	private CheckBox mPointNotSelectedCheckBox;
	private CheckBox mFinalizedPointsCheckBox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_census_list);

		mMainPointCheckBox = (CheckBox) findViewById(R.id.mainPointCheckBox);
		if (NavigateFragment.mFilterCensusList
				.contains(FilterCensusList.MainPoint)) {
			mMainPointCheckBox.setChecked(true);
		}

		mMainPointCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value) {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.MainPoint) == false) {
								NavigateFragment.mFilterCensusList
										.add(FilterCensusList.MainPoint);
							}
						} else {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.MainPoint)) {
								NavigateFragment.mFilterCensusList
										.remove(FilterCensusList.MainPoint);
							}
						}
					}
				});

		mAdditionalPointCheckBox = (CheckBox) findViewById(R.id.additionalPointCheckBox);
		if (NavigateFragment.mFilterCensusList
				.contains(FilterCensusList.AdditionalPoint)) {
			mAdditionalPointCheckBox.setChecked(true);
		}

		mAdditionalPointCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value) {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.AdditionalPoint) == false) {
								NavigateFragment.mFilterCensusList
										.add(FilterCensusList.AdditionalPoint);
							}
						} else {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.AdditionalPoint)) {
								NavigateFragment.mFilterCensusList
										.remove(FilterCensusList.AdditionalPoint);
							}
						}
					}
				});

		mAlternatePointCheckBox = (CheckBox) findViewById(R.id.alternatePointCheckBox);
		if (NavigateFragment.mFilterCensusList
				.contains(FilterCensusList.AlternatePoint)) {
			mAlternatePointCheckBox.setChecked(true);
		}

		mAlternatePointCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value) {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.AlternatePoint) == false) {
								NavigateFragment.mFilterCensusList
										.add(FilterCensusList.AlternatePoint);
							}
						} else {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.AlternatePoint)) {
								NavigateFragment.mFilterCensusList
										.remove(FilterCensusList.AlternatePoint);
							}
						}
					}
				});

		mExcludedPointCheckBox = (CheckBox) findViewById(R.id.excludedPointCheckBox);
		if (NavigateFragment.mFilterCensusList
				.contains(FilterCensusList.ExcludedPoint)) {
			mExcludedPointCheckBox.setChecked(true);
		}

		mExcludedPointCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value) {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.ExcludedPoint) == false) {
								NavigateFragment.mFilterCensusList
										.add(FilterCensusList.ExcludedPoint);
							}
						} else {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.ExcludedPoint)) {
								NavigateFragment.mFilterCensusList
										.remove(FilterCensusList.ExcludedPoint);
							}
						}
					}
				});

		mPointNotSelectedCheckBox = (CheckBox) findViewById(R.id.pointNotSelectedCheckBox);
		if (NavigateFragment.mFilterCensusList
				.contains(FilterCensusList.PointNotSelected)) {
			mPointNotSelectedCheckBox.setChecked(true);
		}

		mPointNotSelectedCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value) {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.PointNotSelected) == false) {
								NavigateFragment.mFilterCensusList
										.add(FilterCensusList.PointNotSelected);
							}
						} else {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.PointNotSelected)) {
								NavigateFragment.mFilterCensusList
										.remove(FilterCensusList.PointNotSelected);
							}
						}
					}
				});

		mFinalizedPointsCheckBox = (CheckBox) findViewById(R.id.finalizedPointsCheckBox);
		if (NavigateFragment.mFilterCensusList
				.contains(FilterCensusList.ProcessedPoint)) {
			mFinalizedPointsCheckBox.setChecked(true);
		}

		mFinalizedPointsCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value) {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.ProcessedPoint) == false) {
								NavigateFragment.mFilterCensusList
										.add(FilterCensusList.ProcessedPoint);
							}
						} else {
							if (NavigateFragment.mFilterCensusList
									.contains(FilterCensusList.ProcessedPoint)) {
								NavigateFragment.mFilterCensusList
										.remove(FilterCensusList.ProcessedPoint);
							}
						}
					}
				});
	}

	public void selectClickListener(View view) {
		Intent returnIntent = new Intent();
		setResult(RESULT_OK, returnIntent);
		finish();
	}
}