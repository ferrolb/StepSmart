package edu.emory.sph.stepsmart;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.emory.sph.stepsmart.Constants;
import edu.emory.sph.stepsmart.db.DailyStepsTable;
import edu.emory.sph.stepsmart.db.DatabaseQueries;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;

public class StepService extends Service implements SensorEventListener {
	
	
	boolean 				mRunning 							= false;
	private 				SensorManager mSensorManager 		= null;
	Thread 					mThread 							= null;
	LocalBroadcastManager 	mBroadcaster						= null;
//	private					int mTotalDailySteps				= 0;
	private 				int mTotalSteps						= 0;
	private					int mInitTotalDailySteps			= 0;
	private 				int mInitTotalSteps					= 0;
	private 				int mFirstValue						= 0;
	private					SharedPreferences mSP				= null;
	private					SharedPreferences.Editor mSP_Editor = null;
//	private 				double goal							= 1000.0;
	private 				Logger mLog;
	
	private void log (String str)
	{
		mLog.tsWrite(getApplicationContext(), str);
	}
	
	public StepService() 
	{
		File root = android.os.Environment.getExternalStorageDirectory(); 
		String path = root.getAbsolutePath();
		mLog = new Logger(path, "outputfile.txt");		
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		mSensorManager 	= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mBroadcaster 	= LocalBroadcastManager.getInstance(this);	
	    mSP 			= this.getSharedPreferences(Constants.PREFERENCES, Activity.MODE_PRIVATE);
	    mSP_Editor		= mSP.edit();
	}
	
	
	@Override
	public int onStartCommand ( Intent intent, int flags, int startId )
	{
		// Set alarm
	    ScheduleManager sm = new ScheduleManager(StepService.this);
	    sm.setAlarm();
	    
		// if today is not equal to last stored date, store the previous daily steps and reset daily steps
		boolean sameAsToday = isTodaySameAsLastDateStored();
		if (!sameAsToday)
		{
			if ( weekHasPassedSinceGoalSet() ) {
                // #=#=#
                int goal = mSP.getInt(Constants.DAILY_GOAL, 0);
                // if the goal hasn't been set and a week has passed, set it to the week's average.
                if (goal==0 && weekHasPassedSinceDate(mSP.getString(Constants.DAILY_GOAL_DATE, "2009-01-01")))
                    setDailyGoalToDailyAverage();
                else
                    increaseDailyGoal();
            }
			storeCurrentDailyStepsIntoDatabase();
			clearDailySteps();
		}	
		
		if (mThread != null)
		{
			if (!mRunning)
			{
				//log("StepService.onStartCommand - thread not running - starting");
				mThread.start();
			}
		}
		else
		{
			//log("StepService.onStartCommand - thread not created - creating");
			mThread 	= createThread();
			mRunning 	= false;
			mThread.start();
		}
		return Service.START_STICKY;
	}
	
	
	Thread createThread()
	{
		return new Thread(new Runnable()
		{
			public void run()
			{
		        mRunning = true;
			    
		        mFirstValue = 0;
		        
		        // Load steps from Preferences
			    mInitTotalDailySteps = mSP.getInt(Constants.DAILY_STEPS, 0);
			    mInitTotalSteps = mSP.getInt(Constants.TOTAL_STEPS, 0);
			    
		        Constants.initializeNotifications( getApplicationContext(), mInitTotalDailySteps );
		        
		        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		        if (countSensor != null) {
		        	mSensorManager.unregisterListener(StepService.this);
		        	mSensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_UI);
		        } else {
		        	Log.i("##### StepService #####", "run() - countSensor == null");
		        }	
			
			}
		});
	}
	
    private void setDailyGoalToDailyAverage()
    {
        DatabaseQueries dbq = new DatabaseQueries();
        mSP_Editor.putInt(Constants.DAILY_GOAL, dbq.getWeeklyAverage());
        mSP_Editor.putString(Constants.DAILY_GOAL_DATE, Constants.getTodaysDate());
        mSP_Editor.apply();
    }
	
	private void testAndSendNotifications(int daily_steps)
	{
		double goal = (double) mSP.getInt(Constants.DAILY_GOAL, 0);
        // #=#=#
        double daily_ratio = 0.0;
        if (goal != 0)
		    daily_ratio = (daily_steps/goal>1.0) ? 1.0 : daily_steps/goal;
		if (null != Constants.mNotifications && !Constants.mNotifications.isEmpty())
		{
			if (Constants.mNotifications.firstKey() <= daily_ratio)
			{
				// Send Notification
				sendNotification( Constants.mNotifications.firstEntry().getValue() );
				Constants.mNotifications.remove( Constants.mNotifications.firstKey() );
			}
		}
	}
	
	private void sendNotification(String msg) {
		// Prepare intent which is triggered if the
		// notification is selected
		Intent intent = new Intent(this, CounterActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		// Build notification
		long[] pattern = {500,500,500,500,500,500,500,500,500};
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// Actions are just fake
		Notification noti = new Notification.Builder(this)
		    .setContentTitle("Step Smart Notification")
		    .setStyle(new Notification.BigTextStyle().bigText(msg))
		    .setContentText(msg).setSmallIcon(R.drawable.walking_icon_m)
			.setLights(Color.BLUE, 500, 500)
			.setVibrate(pattern)
			.setSound(alarmSound)		
		    .setContentIntent(pIntent).build();
//		    .addAction(R.drawable.icon, "Call", pIntent)
//		    .addAction(R.drawable.icon, "More", pIntent)
//		    .addAction(R.drawable.icon, "And more", pIntent).build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, noti);
		
		Toast.makeText(getApplicationContext(), "StepSmart milestone reached!", Toast.LENGTH_LONG).show();
		
	}

	
	private void sendResult(int steps/*, int total_steps, int daily_total*/) {
	    Intent intent = new Intent("edu.emory.sph.stepsmart.broadcast_steps");

        intent.putExtra(Constants.EXTRA_STEP_COUNT, steps);

	    mBroadcaster.sendBroadcast(intent);
	}
	
	// ===================
	// Service binding methods
	// ===================
    public class StepBinder extends Binder 
    {
    	StepService getService()
    	{
    		return StepService.this;
    	}
    }
    private IBinder binder = new StepBinder();
    @Override
    public IBinder onBind(Intent intent)
    {
    	return binder;
    }
    
	// ===================
	// Step Sensor methods
	// ===================    
	@Override
	public void onSensorChanged(SensorEvent event) {
      if (mRunning) {
    	  
    	  int steps = (int) event.values[0];
    	  if (mFirstValue<1)
    	  {
    		  mFirstValue = steps;
//              // TODO: FERROL - remove
//              log("mFirstVale<1, steps:"+steps);
    	  }
    	  else
    	  {
    		  int daily_steps = mSP.getInt(Constants.DAILY_STEPS, 0);
    		  if (0==daily_steps)
    		  {
    			  mFirstValue = steps-1;
    			  mInitTotalDailySteps = 0;
    			  Constants.initializeNotifications( getApplicationContext(), mInitTotalDailySteps );
    		  }
    		  int diff = steps - mFirstValue;
    		  mTotalSteps = diff + mInitTotalSteps;
    		  daily_steps = diff + mInitTotalDailySteps; 		
            
  	        mSP_Editor.putInt(Constants.TOTAL_STEPS, mTotalSteps);
  	        mSP_Editor.putInt(Constants.DAILY_STEPS, daily_steps);
  	        mSP_Editor.putString(Constants.STEPS_DATE, Constants.getTodaysDate());
  	        mSP_Editor.apply();
      	        
      	    testAndSendNotifications(daily_steps);
//            // TODO: FERROL - remove
//            log("steps:"+steps+ " mTotalSteps:"+mTotalSteps + " daily_steps:"+daily_steps);
    	  }

    	  sendResult(steps/*, mTotalSteps, mTotalDailySteps*/);
      }
      else
      {
    	  log("StepService.onSensorChanged - Received sensor event, but thread not running");
      }
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	private void storeCurrentDailyStepsIntoDatabase()
	{
		// Get database
		SQLiteDatabase db =  ((StepSmartApp)getApplication()).getDb();
		ContentValues cv = new ContentValues();
		cv.put(DailyStepsTable.COL_DATE, mSP.getString(Constants.STEPS_DATE, Constants.getTodaysDate()));
		cv.put(DailyStepsTable.COL_STEPS, mSP.getInt(Constants.DAILY_STEPS, 0));
		cv.put(DailyStepsTable.COL_GOAL, mSP.getInt(Constants.DAILY_GOAL, 0));
		db.insert(DailyStepsTable.TABLE_NAME, null, cv);
		db.close();	
		log("Storing:"+mSP.getString(Constants.STEPS_DATE, Constants.getTodaysDate())+", "+mSP.getInt(Constants.DAILY_STEPS, 0));
	}
	
	private boolean isTodaySameAsLastDateStored()
	{	
		String storedDate = mSP.getString(Constants.STEPS_DATE, "");
		return storedDate.equals( Constants.getTodaysDate() );
	}	


    private boolean weekHasPassedSinceDate(String date) {
        if (TextUtils.isEmpty(date))
            return false;
        return (Constants.numberOfDaysSinceDate(date) >= 7) ? true : false;
    }


	private boolean weekHasPassedSinceGoalSet()
	{
        return weekHasPassedSinceDate( mSP.getString(Constants.DAILY_GOAL_DATE, "2009-01-01") );
	}
	
	private void increaseDailyGoal()
	{
		if (mSP.getBoolean(Constants.INCREASE_DAILY_GOAL, true))
		{
			int goal = mSP.getInt(Constants.DAILY_GOAL, 0);
			goal *= 1.05;
			mSP_Editor.putInt(Constants.DAILY_GOAL, goal);
			mSP_Editor.putString(Constants.DAILY_GOAL_DATE, Constants.getTodaysDate());
			mSP_Editor.apply();
		}
	}
	
	private void clearDailySteps()
	{
        mSP_Editor.putInt(Constants.DAILY_STEPS, 0);
        mSP_Editor.putString(Constants.STEPS_DATE, Constants.getTodaysDate());
        mSP_Editor.apply();		
	}	
}
