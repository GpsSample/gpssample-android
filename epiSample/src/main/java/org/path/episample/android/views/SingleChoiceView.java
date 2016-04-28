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

package org.path.episample.android.views;

/*
 * @author belendia@gmail.com
 */

import org.path.episample.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class SingleChoiceView extends RelativeLayout implements Checkable {

  public SingleChoiceView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public SingleChoiceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SingleChoiceView(Context context) {
    super(context);
  }

  @Override
  public boolean isChecked() {
    RadioButton r = (RadioButton) findViewById(R.id.radiobutton);
    return r.isChecked();
  }

  @Override
  public void setChecked(boolean checked) {
	  RadioButton r = (RadioButton) findViewById(R.id.radiobutton);
      r.setChecked(checked);
  }

  @Override
  public void toggle() {
	  RadioButton r = (RadioButton) findViewById(R.id.radiobutton);
      r.setChecked(!r.isChecked());
  }

}
