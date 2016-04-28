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

package org.path.episample.android.utilities;

import java.util.List;

import org.path.common.android.data.PlaceModel;
import org.path.episample.android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/*
 * @author belendia@gmail.com
 */

public class PlaceSpinnerAdapter extends BaseAdapter implements SpinnerAdapter{
    private Activity activity;
    private List<PlaceModel> placeList; 

    public PlaceSpinnerAdapter(Activity activity, List<PlaceModel> placeList){
        this.activity = activity;
        this.placeList = placeList;
    }

    public int getCount() {
        return placeList.size();
    }

    public Object getItem(int position) {
        return placeList.get(position);
    }

    public long getItemId(int position) {
    	String strId = placeList.get(position).getCode();
    	long result = -1;
    	try {
    		result = Long.parseLong(strId);
    	} catch(Exception ex) {
    		
    	}
        return result;
    }

    public View getView(int position, View view, ViewGroup parent) {

    View placeView;
    if( view == null ){
        LayoutInflater inflater = activity.getLayoutInflater();
        placeView = inflater.inflate(R.layout.spinner_row, null);
    } else {
    	placeView = view;
    }
    TextView row = (TextView) placeView.findViewById(R.id.text_row);
    
    row.setText(String.valueOf(placeList.get(position).getName()));
    return placeView;
    }
}