package edu.emory.sph.stepsmart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


import edu.emory.sph.stepsmart.db.Aggregate;
import edu.emory.sph.stepsmart.db.DailyStepsTable;
import edu.emory.sph.stepsmart.db.DatabaseQueries;
import edu.emory.sph.stepsmart.db.StepSmartDBHelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity implements OnSeekBarChangeListener {
	
	
	private SeekBar mSeekBar = null;
	private TextView mSeekValue = null;
	private TextView mLogView = null;
	private TextView mWeekTotalView = null;
	private TextView mMonthTotalView = null;
	private TextView mWeekAvgView = null;
	private TextView mMonthAvgView = null;	
	private CheckBox mIncreaseGoal = null;
	
//	private EditText mLowLimit = null;
//	private EditText mHighLimit = null;
	private final int mLow = 0;//1000;
	private final int mHigh = 20000;
	private Button mResetBtn;
	private Button mSaveBtn;
	private	SharedPreferences mSP;
	
	// TODO: FERROL - remove this test data method
	private void addTestData () {
		SQLiteDatabase db =  ((StepSmartApp)getApplication()).getDb();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(2014, Calendar.OCTOBER, 1);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		int steps = 500;
		for (int i = 0; i < 120; ++i)
		{
			String day = df.format(c.getTime());
			Log.i("###SettingsActivity###", "Day: " + day + " Steps: " + steps);
			ContentValues cv = new ContentValues();
			cv.put(DailyStepsTable.COL_DATE, day);
			cv.put(DailyStepsTable.COL_STEPS, steps);
            cv.put(DailyStepsTable.COL_GOAL, mSP.getInt(Constants.DAILY_GOAL, mLow));
			db.insert(DailyStepsTable.TABLE, null, cv);
			c.add(Calendar.DAY_OF_MONTH, 1);
			steps++;
		}
		db.close();

	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setTitle(R.string.app_name); 
        
        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        // Remove user's ability to set the step goal for now
//        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setEnabled(false);

        mSeekValue = (TextView) findViewById(R.id.seekValue);
        mLogView = (TextView) findViewById(R.id.tvDebugLog);
        mLogView.setMovementMethod(new ScrollingMovementMethod());
        mWeekTotalView = (TextView) findViewById(R.id.txtWeekTotal);
        mMonthTotalView = (TextView) findViewById(R.id.txtMonthTotal);
        mWeekAvgView = (TextView) findViewById(R.id.txtWeekAvg);
        mMonthAvgView = (TextView) findViewById(R.id.txtMonthAvg);
        mIncreaseGoal = (CheckBox) findViewById(R.id.cbIncreaseGoal);
        
//        addTestData();
        DatabaseQueries dbq = new DatabaseQueries();
        mWeekTotalView.setText(dbq.getWeekTotal()+"");
    	mMonthTotalView.setText(dbq.getMonthTotal()+"");
    	mWeekAvgView.setText(dbq.getWeeklyAverage()+"");
    	mMonthAvgView.setText(dbq.getMonthlyAverage()+"");
        
        
		File root = android.os.Environment.getExternalStorageDirectory(); 
		String path = root.getAbsolutePath();
        
//        Logger.loadFileToTextView(mLogView, path, "outputfile.txt");
        loadPreviousWeeksDataToTextView( mLogView );
        
	    mSP = this.getSharedPreferences(Constants.PREFERENCES, Activity.MODE_PRIVATE);	
	    mIncreaseGoal.setChecked(mSP.getBoolean(Constants.INCREASE_DAILY_GOAL, true));
        // #=#=#
	    int goal = mSP.getInt(Constants.DAILY_GOAL, mLow);
        if (goal == 0) {
            int daysTillGoalIsSet = 7 - Constants.numberOfDaysSinceDate( mSP.getString(Constants.INSTALL_DATE,"") );
            if (daysTillGoalIsSet==1)
                mSeekValue.setText("Goal will be set in 1 day");
            else
                mSeekValue.setText("Goal will be set in "+daysTillGoalIsSet+" days");
            mSeekBar.setProgress(0);
        }
        else {
            mSeekValue.setText("" + goal);
            goal = getGoalPercent(goal);
            mSeekBar.setProgress(goal);
        }
        mResetBtn = (Button) findViewById(R.id.resetBtn);
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetDailySteps();
            }
        });
        
        mSaveBtn = (Button) findViewById(R.id.btnSave);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Editor editor = mSP.edit();
                int seekVal = 0;
                try {
                  seekVal = Integer.parseInt(mSeekValue.getText().toString());
                }
                catch (NumberFormatException e)
                {

                }
                editor.putInt(Constants.DAILY_GOAL, seekVal);
                editor.putString(Constants.DAILY_GOAL_DATE, Constants.getTodaysDate());
                boolean increase_goal = false;
                if (mIncreaseGoal.isChecked())
                	increase_goal = true;
                else
                	increase_goal = false;
                editor.putBoolean(Constants.INCREASE_DAILY_GOAL, increase_goal);
                editor.apply();
//        		mLow = Integer.parseInt(mLowLimit.getText().toString());
//        		mHigh = Integer.parseInt(mHighLimit.getText().toString());
            }
        });
        
		final Button copyDb = (Button) findViewById(R.id.copyDbBtn);
		copyDb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                InputStream in = null;
                OutputStream out = null;
                try {
                  in = new FileInputStream(StepSmartDBHelper.DB_PATH);
                  out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() +"/" + StepSmartDBHelper.DB_FILE);
                  copyFile(in, out);
                  in.close();
                  in = null;
                  out.flush();
                  out.close();
                  out = null;
                } catch(Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        });        
        
        
    }
    private void loadPreviousWeeksDataToTextView( TextView logView ){
        DatabaseQueries dbg = new DatabaseQueries();
        ArrayList<Aggregate> aggs = dbg.getWeeklyData();
        for (Aggregate agg : aggs) {
            String line = "Week of: "+ agg.date + " Goal: "+ agg.goal + " Sum: " + agg.sum_steps + " Avg: " + agg.avg_steps+"\n";
            logView.append(line);
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
    
    private int getGoalPercent(int goalNumber)
    {
    	int percent = 0;
        if (goalNumber <= 0)
            return 0;
    	percent = (int) (((double)(goalNumber - mLow))/((double)(mHigh - mLow)) * 100.0);
    	return percent;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    private void resetDailySteps()
    {
    	 SharedPreferences.Editor sp_Editor = mSP.edit();
    	 sp_Editor.putInt(Constants.DAILY_STEPS, 0);
    	 sp_Editor.putString(Constants.STEPS_DATE, Constants.getTodaysDate());
    	 sp_Editor.apply();   
    	 Constants.initializeNotifications(getApplicationContext(), 0);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
		int value = mLow + (int) (((double)progress)/100.0 * (mHigh - mLow)  );
		mSeekValue.setText(""+value);
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
