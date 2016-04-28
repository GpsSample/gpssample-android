/*
 * Copyright (C) 2011-2013 University of Washington
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
 */

package org.path.episample.android.preferences;

import org.path.episample.android.R;
import org.path.episample.android.activities.ClearAllActivity;
import org.path.episample.android.activities.MainMenuActivity;
import org.path.episample.android.logic.PropertiesSingleton;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class AdminPreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener{

  public static final String ADMIN_PREFERENCES = "admin_prefs";

  // key for this preference screen
  public static final String KEY_ADMIN_PW = "admin_pw";
  public static final String KEY_CLEAR_ALL_CENSUS_PW = "celar_all_census_pw";

  // keys for each preference
  // main menu
  public static final String KEY_COLLECT = "collect";
  public static final String KEY_SEND_RECEIVE = "send_receive";
  public static final String KEY_SEND_RECEIVE_BLUETOOTH = "send_receive_bluetooth";
  public static final String KEY_SELECT = "select";
  public static final String KEY_NAVIGATE = "navigate";
  public static final String KEY_BACKUP_CENSUS = "backup_census";
  public static final String KEY_RESTORE_CENSUS = "restore_census";
  public static final String KEY_EDIT_CENSUS = "edit_census";
  public static final String KEY_REMOVE_CENSUS = "remove_census";
  public static final String KEY_INVALIDATE_CENSUS = "invalidate_census";
  
  public static final String KEY_EDIT_SAVED = "edit_saved";
  public static final String KEY_SEND_FINALIZED = "send_finalized";
  public static final String KEY_GET_BLANK = "get_blank";
  public static final String KEY_MANAGE_FORMS = "delete_saved";
  // server
  public static final String KEY_CHANGE_SERVER = "change_server";
  public static final String KEY_CHANGE_USERNAME = "change_username";
  public static final String KEY_CHANGE_PASSWORD = "change_password";
  public static final String KEY_CHANGE_GOOGLE_ACCOUNT = "change_google_account";
  // client
  public static final String KEY_CHANGE_FONT_SIZE = "change_font_size";
  public static final String KEY_SHOW_SPLASH_SCREEN = "show_splash_screen";
  public static final String KEY_SELECT_SPLASH_SCREEN = "select_splash_screen";
  //interviewer name/ device info
  public static final String KEY_INTERVIEWER_NAME = "change_interviewer_name";
  public static final String KEY_PHONE_NUMBER = "change_phone_number";
  public static final String KEY_DEVICE_NAME = "change_device_name";
  
  // form entry
  public static final String KEY_ACCESS_SETTINGS = "access_settings";
  public static final String KEY_CHANGE_LANGUAGE = "change_language";
  public static final String KEY_JUMP_TO = "jump_to";

  //collect module
  public static final String KEY_GPS_ACCURACY_THRESHOLDS = "gps_accuracy_thresholds";
  public static final String KEY_COMMENT_MANDATORY = "comment_mandatory";
  public static final String KEY_AUTO_INCREMENT_HOUSE_NUMBER = "auto_increment_house_number";
  public static final String KEY_EDIT_SAVED_DATA = "edit_saved_data";
  public static final String KEY_EXCLUDE_POINTS = "exclude_points";
  public static final String KEY_DEVICE_HAS_LOW_QUALITY_GPS = "device_has_low_quality_gps";
  public static final String KEY_DELAY = "delay";
  public static final String KEY_CLEAN_DATA_FROM_PREVIOUS_DATE = "clean_data_from_previous_date";
  
  //send receive module
  public static final String KEY_DEVICE_BELONG_TO_TEAM_LEAD = "team_lead_device";
  public static final String KEY_TURN_ON_OFF_WIFI_AUTOMATICALLY = "wifi_state_control";
  
  //select module
  public static final String KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS = "set_fixed_number_of_main_points";
  public static final String KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS = "set_fixed_number_of_additional_points";
  public static final String KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS = "set_fixed_number_of_alternate_points";
  public static final String KEY_CLEAR_ALL_DATA = "clear_all_data";
  
  //navigate module
  public static final String KEY_FORM_ID = "selected_form_id";
  public static final String KEY_TABLE_ID = "selected_table_id";
  public static final String KEY_SHOW_FILTER_BY_PLACE_NAME_ICON = "show_filter_by_place_name";
  public static final String KEY_MARK_AS_FINALIZED_BY_FRIENDS = "show_mark_as_finalized_by_friends_menu";
  
  private EditTextPreference mGPSAccuracyThresholdsPreference;    
  private EditTextPreference mFixedNoMainPointsPreference;  
  private EditTextPreference mFixedNoAdditionalPointsPreference;  
  private EditTextPreference mFixedNoAlternatePointsPreference;  
  
  private Preference mFormIdPreference;
  private MinuteSecondPickerPreference mDelayPreference;
  
  private static final int SAVE_TO_DISK_MENU = Menu.FIRST;
  
  private String mAppName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mAppName = this.getIntent().getStringExtra(MainMenuActivity.APP_NAME);
    if ( mAppName == null || mAppName.length() == 0 ) {
    	mAppName = "survey";
    }

    setTitle(getString(R.string.admin_preferences));

    addPreferencesFromResource(R.xml.admin_preferences);

    PreferenceScreen prefScreen = this.getPreferenceScreen();

    initializeCheckBoxPreference(prefScreen);
    
    mGPSAccuracyThresholdsPreference = (EditTextPreference) findPreference(KEY_GPS_ACCURACY_THRESHOLDS);
    if (PropertiesSingleton.containsKey(mAppName, KEY_GPS_ACCURACY_THRESHOLDS)) {
      String accuracyThresholds = PropertiesSingleton.getProperty(mAppName, KEY_GPS_ACCURACY_THRESHOLDS);
      mGPSAccuracyThresholdsPreference.setSummary(accuracyThresholds);
      mGPSAccuracyThresholdsPreference.setText(accuracyThresholds);
    }

    mGPSAccuracyThresholdsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        	
        	String value = newValue.toString();
        	try {
        		int intVal = Integer.parseInt(value);
        		if(intVal<=0 || intVal>100) {
        			value="10";
        		}
        	} catch(Exception ex) {
        		value = "10";
        	}
        	mGPSAccuracyThresholdsPreference.setSummary(value);
        	PropertiesSingleton.setProperty(mAppName, KEY_GPS_ACCURACY_THRESHOLDS, value);
          	return true;
        }
      });
    
    //Set fixed number of main points
    mFixedNoMainPointsPreference = (EditTextPreference) findPreference(KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS);
    if (PropertiesSingleton.containsKey(mAppName, KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS)) {
      String mainPoints = PropertiesSingleton.getProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS);
      mFixedNoMainPointsPreference.setSummary(mainPoints);
      mFixedNoMainPointsPreference.setText(mainPoints);
    }
    mFixedNoMainPointsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        	
        	String value = newValue.toString();
        	int intValue = 0;
        	try {
        		intValue = Integer.parseInt(value);
        		mFixedNoMainPointsPreference.setSummary(String.valueOf(intValue));
        		PropertiesSingleton.setProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS, String.valueOf(intValue));
        	} catch(Exception ex) {
        		mFixedNoMainPointsPreference.setSummary("");
        		PropertiesSingleton.setProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_MAIN_POINTS, "");
        	}
          	return true;
        }
      });
    
    //Set fixed number of additional points
    mFixedNoAdditionalPointsPreference = (EditTextPreference) findPreference(KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS);
    if (PropertiesSingleton.containsKey(mAppName, KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS)) {
      String additionalPoints = PropertiesSingleton.getProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS);
      mFixedNoAdditionalPointsPreference.setSummary(additionalPoints);
      mFixedNoAdditionalPointsPreference.setText(additionalPoints);
    }
    mFixedNoAdditionalPointsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        	
        	String value = newValue.toString();
        	int intValue = 0;
        	try {
        		intValue = Integer.parseInt(value);
        		mFixedNoAdditionalPointsPreference.setSummary(String.valueOf(intValue));
        		PropertiesSingleton.setProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS, String.valueOf(intValue));
        	} catch(Exception ex) {
        		mFixedNoAdditionalPointsPreference.setSummary("");
        		PropertiesSingleton.setProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_ADDITIONAL_POINTS, "");
        	}
          	return true;
        }
      });
    
    //Set fixed number of alternate points
    mFixedNoAlternatePointsPreference = (EditTextPreference) findPreference(KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS);
    if (PropertiesSingleton.containsKey(mAppName, KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS)) {
      String alternatePoints = PropertiesSingleton.getProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS);
      mFixedNoAlternatePointsPreference.setSummary(alternatePoints);
      mFixedNoAlternatePointsPreference.setText(alternatePoints);
    }
    mFixedNoAlternatePointsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        	
        	String value = newValue.toString();
        	int intValue = 0;
        	try {
        		intValue = Integer.parseInt(value);
        		mFixedNoAlternatePointsPreference.setSummary(String.valueOf(intValue));
            	PropertiesSingleton.setProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS, String.valueOf(intValue));
        	} catch(Exception ex) {
        		mFixedNoAlternatePointsPreference.setSummary("");
            	PropertiesSingleton.setProperty(mAppName, KEY_SET_FIXED_NUMBER_OF_ALTERNATE_POINTS, "");
        	}
        	
          	return true;
        }
      });
    
    
    Preference pref = findPreference(KEY_CLEAR_ALL_DATA);
    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
  
       @Override
       public boolean onPreferenceClick(Preference preference) {
    	   createPasswordDialog();
          return false;
       }
    });

    mDelayPreference = (MinuteSecondPickerPreference) findPreference(KEY_DELAY);
    mDelayPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference arg0, Object delayTime) {
			PropertiesSingleton.setProperty(mAppName, AdminPreferencesActivity.KEY_DELAY, String.valueOf(delayTime));
        	PropertiesSingleton.writeProperties(mAppName);
			mDelayPreference.setSummary(String.valueOf(delayTime));	
			return false;
		}
	});
    
    String delayTime = PropertiesSingleton.getProperty(mAppName, AdminPreferencesActivity.KEY_DELAY);
    if(delayTime == null) delayTime = "0:30";
    mDelayPreference.setSummary(delayTime);
  }
  
  @Override
  public void onResume() {
    super.onResume();
    
    mFormIdPreference = (Preference) findPreference(KEY_FORM_ID);
    if (PropertiesSingleton.containsKey(mAppName, KEY_FORM_ID)) {
      String formId = PropertiesSingleton.getProperty(mAppName, KEY_FORM_ID);
      mFormIdPreference.setSummary(formId);
    }
  }

  protected void initializeCheckBoxPreference(PreferenceGroup prefGroup) {

    for ( int i = 0; i < prefGroup.getPreferenceCount(); i++ ) {
      Preference pref = prefGroup.getPreference(i);
      Class c = pref.getClass();
      if (c == CheckBoxPreference.class) {
        CheckBoxPreference checkBoxPref = (CheckBoxPreference)pref;
        if (PropertiesSingleton.containsKey(mAppName, checkBoxPref.getKey())) {
          String checked = PropertiesSingleton.getProperty(mAppName, checkBoxPref.getKey());
          if (checked.equals("true")) {
            checkBoxPref.setChecked(true);
          } else {
            checkBoxPref.setChecked(false);
          }
        }
        // Set the listener
        checkBoxPref.setOnPreferenceChangeListener(this);
      } else if (c == PreferenceCategory.class) {
        // Find CheckBoxPreferences in this category
        PreferenceCategory prefCat = (PreferenceCategory)pref;
        initializeCheckBoxPreference(prefCat);
      }
    }
  }

  /**
   * Generic listener that sets the summary to the newly selected/entered value
   */
  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    PropertiesSingleton.setProperty(mAppName, preference.getKey(), newValue.toString());
    return true;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    PropertiesSingleton.writeProperties(mAppName);
  }

  @Override
  public void finish() {
	  PropertiesSingleton.writeProperties(mAppName);
    super.finish();
  }
  
  
  protected void createPasswordDialog() {
	    final AlertDialog passwordDialog = new AlertDialog.Builder(this).create();

	    passwordDialog.setTitle(getString(R.string.enter_password));
	    final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
	    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
	    passwordDialog.setView(input, 20, 10, 20, 10);

	    passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
	        new DialogInterface.OnClickListener() {
	          public void onClick(DialogInterface dialog, int whichButton) {
	        	  
	        	String password = PropertiesSingleton.getProperty(mAppName, AdminPreferencesActivity.KEY_CLEAR_ALL_CENSUS_PW);
	        	if(password == null || password.trim().length() == 0) {
	        		password = "path14$";
	        	}
	            String value = input.getText().toString();
	            if(value.equals(password)) {
	            	Intent intent = new Intent(AdminPreferencesActivity.this, ClearAllActivity.class);
	            	startActivity(intent);
	            } else {
	            	Toast.makeText(AdminPreferencesActivity.this, getString(R.string.password_incorrect),
	  	                  Toast.LENGTH_SHORT).show();
	            }
	          }
	        });

	    passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
	        new DialogInterface.OnClickListener() {

	          public void onClick(DialogInterface dialog, int which) {
	            input.setText("");
	            return;
	          }
	        });

	    passwordDialog.getWindow().setSoftInputMode(
	        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	    passwordDialog.show();
	  }
}