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

package org.path.episample.android.preferences;

import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.utilities.TimeUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.TextView;

/*
 * @author belendia@gmail.com
 */

public class MinuteSecondPickerPreference extends DialogPreference {

	private static final String DEFAULT_MINUTE_SECOND = "00:30";
	private int mMinute = 0;
	private int mSecond = 0;

	private NumberPicker mMinuteNumberPicker;
	private NumberPicker mSecondNumberPicker;
	private TextView mSeparatorTextView;

	public MinuteSecondPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setPositiveButtonText("Set");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView() {
		mMinuteNumberPicker = new NumberPicker(getContext());
		mMinuteNumberPicker.setMinValue(0);
		mMinuteNumberPicker.setMaxValue(59);
		mMinuteNumberPicker.setWrapSelectorWheel(false);

		mSecondNumberPicker = new NumberPicker(getContext());
		mSecondNumberPicker.setMinValue(0);
		mSecondNumberPicker.setMaxValue(59);
		mSecondNumberPicker.setWrapSelectorWheel(false);

		LinearLayout.LayoutParams pickerParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// pickerParams.gravity = Gravity.CENTER;
		mMinuteNumberPicker.setLayoutParams(pickerParams);
		mSecondNumberPicker.setLayoutParams(pickerParams);

		mSeparatorTextView = new TextView(getContext());
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textParams.gravity = Gravity.CENTER_VERTICAL;
		mSeparatorTextView.setLayoutParams(textParams);
		mSeparatorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		mSeparatorTextView.setTypeface(Typeface.DEFAULT_BOLD);
		mSeparatorTextView.setText(":");
		mSeparatorTextView.setPadding(30, 0, 30, 0);

		LinearLayout layout = new LinearLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(params);
		layout.setGravity(Gravity.CENTER);

		layout.addView(mMinuteNumberPicker);
		layout.addView(mSeparatorTextView);
		layout.addView(mSecondNumberPicker);

		return layout;

		// return numberPicker;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		mMinuteNumberPicker.setValue(mMinute);
		mSecondNumberPicker.setValue(mSecond);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mMinute = mMinuteNumberPicker.getValue();
			mSecond = mSecondNumberPicker.getValue();

			String time = String.valueOf(mMinute) + ":"
					+ String.valueOf(mSecond);

			callChangeListener(time);

			/*
			 * if (callChangeListener(time)) {
			 * PropertiesSingleton.setProperty("survey",
			 * AdminPreferencesActivity.KEY_DELAY, time);
			 * PropertiesSingleton.writeProperties("survey"); }
			 */
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time = PropertiesSingleton.getProperty("survey",
				AdminPreferencesActivity.KEY_DELAY);
		if (time == null)
			time = DEFAULT_MINUTE_SECOND;

		mMinute = TimeUtil.getMinutes(time);
		mSecond = TimeUtil.getSeconds(time);
	}

}