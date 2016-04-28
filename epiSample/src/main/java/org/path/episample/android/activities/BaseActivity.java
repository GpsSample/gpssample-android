package org.path.episample.android.activities;

import org.path.episample.android.R;

import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {
	private static final int MENU_HOME = Menu.FIRST;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		int showOption = MenuItem.SHOW_AS_ACTION_IF_ROOM;
		MenuItem item = menu.add(Menu.NONE, MENU_HOME, Menu.NONE,
				getString(R.string.main_menu));
		item.setIcon(R.drawable.ic_action_home).setShowAsAction(showOption);
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
	    actionBar.show();
	      
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_HOME) {
			homeMenuClicked();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void homeMenuClicked() {

	}
}
