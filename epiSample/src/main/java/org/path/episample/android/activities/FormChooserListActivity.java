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

import org.path.common.android.database.CensusDatabaseHelper;
import org.path.common.android.provider.FormsColumns;
import org.path.common.android.utilities.ODKDatabaseUtils;
import org.path.episample.android.R;
import org.path.episample.android.logic.PropertiesSingleton;
import org.path.episample.android.preferences.AdminPreferencesActivity;
import org.path.episample.android.provider.FormsProviderAPI;
import org.path.episample.android.utilities.VersionHidingCursorAdapter;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/*
 * @author belendia@gmail.com
 */

public class FormChooserListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String t = "FormChooserListActivity";
	private static final int FORM_CHOOSER_LIST_LOADER = 0x037;
	private CursorAdapter mInstances;
	private String mAppName;
	private FormChoiceListener mFormChoiceListener;

	public interface FormChoiceListener {
		public void onItemSelected(String formId);
	}

	void setOnItemSelected(FormChoiceListener listener) {
		mFormChoiceListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_chooser_list2);
		setAppName("survey");

		String[] data = new String[] { FormsColumns.DISPLAY_NAME,
				FormsColumns.DISPLAY_SUBTEXT, FormsColumns.FORM_VERSION };
		int[] viewParams = new int[] { R.id.text1, R.id.text2, R.id.text3 };

		// render total instance view
		mInstances = new VersionHidingCursorAdapter(FormsColumns.FORM_VERSION,
				this, R.layout.two_item, data, viewParams);
		setListAdapter(mInstances);

		getLoaderManager().initLoader(FORM_CHOOSER_LIST_LOADER, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// get uri to form
		Cursor c = (Cursor) (((SimpleCursorAdapter) getListAdapter())
				.getItem(position));
		String formId = ODKDatabaseUtils.get().getIndexAsString(c,
				c.getColumnIndex(FormsColumns.FORM_ID));
		String tableId = ODKDatabaseUtils.get().getIndexAsString(c,
				c.getColumnIndex(FormsColumns.TABLE_ID));

		PropertiesSingleton.setProperty(getAppName(),
				AdminPreferencesActivity.KEY_FORM_ID, formId);
		PropertiesSingleton.setProperty(getAppName(),
				AdminPreferencesActivity.KEY_TABLE_ID, tableId);

		PropertiesSingleton.writeProperties(getAppName());
		if (mFormChoiceListener != null) {
			mFormChoiceListener.onItemSelected(formId);
		}

		finish();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		Uri baseUri = Uri.withAppendedPath(FormsProviderAPI.CONTENT_URI,
				getAppName());

		String selection = FormsColumns.FORM_ID + "<> ? AND "
				+ FormsColumns.FORM_ID + "<> ?";
		String[] selectionArgs = { FormsColumns.COMMON_BASE_FORM_ID,
				CensusDatabaseHelper.CENSUS_DATABASES_TABLE };
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, "
				+ FormsColumns.FORM_VERSION + " DESC";
		return new CursorLoader(this, baseUri, null, selection, selectionArgs,
				sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mInstances.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mInstances.swapCursor(null);
	}

	public String getAppName() {
		return mAppName;
	}

	public void setAppName(String value) {
		mAppName = value;
	}

}
