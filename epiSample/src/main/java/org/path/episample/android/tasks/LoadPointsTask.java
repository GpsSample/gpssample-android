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

package org.path.episample.android.tasks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.wink.common.internal.providers.entity.csv.CsvReader;
import org.path.common.android.data.CensusModel;
import org.path.common.android.database.CensusDatabaseFactory;
import org.path.common.android.database.DatabaseFactory;
import org.path.common.android.utilities.CensusUtil;
import org.path.common.android.utilities.ODKFileUtils;
import org.path.episample.android.application.Survey;
import org.path.episample.android.listeners.LoadPointsListener;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;

/*
 * @author belendia@gmail.com
 */

public class LoadPointsTask extends AsyncTask<Void, Void, Void> {
	private static final String t = "LoadPointsTask";
	private LoadPointsListener mLoadPointsListener;
	/*private static final char DELIMITING_CHAR = ',';
    private static final char QUOTE_CHAR = '"';
    private static final char ESCAPE_CHAR = '\0';*/
    
    private Application mAppContext;
    private String mAppName;
    
	@Override
	protected Void doInBackground(Void... arg0) {
		SQLiteDatabase webDb = DatabaseFactory.get().getDatabase(Survey.getInstance().getApplicationContext(), "survey");
		SQLiteDatabase censusDb = CensusDatabaseFactory.get().getDatabase(Survey.getInstance().getApplicationContext(),
				"survey");
		
		readPoint(webDb, censusDb);
		
		webDb.close();
		censusDb.close();
		
		return null;
	}

	private void readPoint(SQLiteDatabase webDb, SQLiteDatabase censusDb) {
		CsvReader reader = null;
		try {
			reader = new CsvReader(new InputStreamReader(new FileInputStream(ODKFileUtils.getPointCsvFile(getAppName())), "UTF-8"));
			
			//db.beginTransaction();
		
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			String currentDate = sdf.format(calendar.getTime());
				
			String [] row;
			SQLiteStatement insertSqlCensusDb = CensusUtil.getInsertStatementCensusDb(censusDb);
			SQLiteStatement updateSqlCensusDb = CensusUtil.getUpdateStatementCensusDb(censusDb);
			SQLiteStatement insertSqlWebDb = CensusUtil.getInsertStatementWebDb(webDb);
			SQLiteStatement updateSqlWebDb = CensusUtil.getUpdateStatementWebDb(webDb);
			
			censusDb.beginTransaction();
			webDb.beginTransaction();
		    while ((row = reader.readLine()) != null) {
		    	if(isCancelled() == true) {
					break;
				}					
				
				CensusModel census = new CensusModel();
				
				if(row.length > 1) {
					if(row[0].matches("uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
						census.setInstanceId(row[0]);
					} else {
						continue;
					}
				}
				
				if(row.length > 1) {
					census.setPlaceName(row[1].length() == 0 ? null : row[1]);
				} else {
					continue;
				}
				
				if(row.length >= 3) {
					census.setHouseNumber(row[2].length() == 0 ? null : row[2]);
				}
				
				if(row.length >= 4) {
					census.setHeadName(row[3].length() == 0 ? null : row[3]);
				}
				
				census.setValid(1);
				
				if(row.length >= 5) {
					double lat = 0;
					try {
						lat = Double.parseDouble(row[4]);
					} catch(Exception e) {
						
					}
					census.setLatitude(lat);
				}
				
				if(row.length >= 6) {
					double lng = 0;
					try {
						lng = Double.parseDouble(row[5]);
					} catch(Exception e) {
						
					}
					census.setLongitude(lng);
				}
				
				if(row.length >= 7) {
					double alt = 0;
					try {
						alt = Double.parseDouble(row[6]);
					} catch(Exception e) {
						
					}
					census.setAltitude(alt);
				}
				
				if(row.length >= 8) {
					int selected = 0;
					try {
						selected = Integer.parseInt(row[7]);
					} catch(Exception e) {
						
					}
					census.setSelected(selected);
				}
				
				census.setCreatedDate(currentDate);
				census.setFormName(census.getPlaceName() + " > " + census.getHeadName() + " (" + census.getHouseNumber() + ")");
				
				CensusUtil.insertOrUpdate(mAppContext, insertSqlCensusDb, updateSqlCensusDb,insertSqlWebDb, updateSqlWebDb, census, true);
		    }
		    
		    insertSqlCensusDb.close();
			updateSqlCensusDb.close();
			insertSqlWebDb.close();
			updateSqlWebDb.close();
			
		    censusDb.setTransactionSuccessful();	
		    censusDb.endTransaction();
		    
		    webDb.setTransactionSuccessful();	
		    webDb.endTransaction();
			//db.setTransactionSuccessful();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//db.endTransaction();
		}
	}
	
	@Override
	protected void onPostExecute(Void v) {
		synchronized (this) {
			if (mLoadPointsListener != null) {
				mLoadPointsListener.loadingComplete();
			}
		}
	}

	public void setListener(LoadPointsListener listener) {
		synchronized (this) {
			mLoadPointsListener = listener;
		}
	}
	
	public void setAppName(String appName) {
	    synchronized (this) {
	      this.mAppName = appName;
	    }
	  }

	  public String getAppName() {
	    return mAppName;
	  }

	  public void setApplication(Application appContext) {
	    synchronized (this) {
	      this.mAppContext = appContext;
	    }
	  }

	  public Application getApplication() {
	    return mAppContext;
	  }
}
