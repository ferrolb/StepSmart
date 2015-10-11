package edu.emory.sph.stepsmart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TreeMap;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Constants {

	public final static String EXTRA_STEP_COUNT = "Extra - Step Count";
	public final static String EXTRA_DAILY_TOTAL = "Extra - Daily Total";
	public final static String EXTRA_TOTAL_COUNT = "Extra - Total Count";
	public final static String PREFERENCES = "STEP_TRACKER_PREFERENCES";
	public final static String CLEAR_DAILY_TOTAL_INTENT = "edu.emory.sph.stepsmart.clearDailyTotals";
	public final static String TOTAL_STEPS = "TOTAL_STEPS";
	public final static String DAILY_STEPS = "DAILY_STEPS";
	public final static String STEPS_DATE = "STEPS_DATE";
	public final static String DAILY_GOAL = "DAILY_GOAL";
	public final static String DAILY_GOAL_DATE = "DAILY_GOAL_DATE";
    public final static String INSTALL_DATE = "INSTALL_DATE";
	public final static String INCREASE_DAILY_GOAL = "INCREASE_DAILY_GOAL";
    //public final static double goal = 1000.0;
    
	public static TreeMap<Double, String> mNotifications;
	public static void initializeNotifications( Context context, int daily_steps )
	{
		SharedPreferences SP = context.getSharedPreferences(Constants.PREFERENCES, Activity.MODE_PRIVATE);
		double goal = (double) (SP.getInt(Constants.DAILY_GOAL, 0));
		mNotifications = new TreeMap<Double, String>();
		mNotifications.put(0.25, "You're off to a great start. Keep it up!");
		mNotifications.put(0.50, "You're half way to your daily goal!!  You're doing fantastic!");
		mNotifications.put(0.75, "You've made it 3/4 to your daily step goal!  Not far now!");
		mNotifications.put(1.00, "You've met your daily step goal!!  Congratulations!");

        // #=#=#
        double daily_ratio = 0.0;
        if (goal != 0.0)
            daily_ratio = (daily_steps/goal>1.0) ? 1.0 : daily_steps/goal;
		while (!mNotifications.isEmpty() && mNotifications.firstKey() <= daily_ratio)
			mNotifications.remove( mNotifications.firstKey() );
	}
	
	public static String getCurrentDateTime()
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("MM-dd:HH:mm");
		return df.format(c.getTime());		
	}
	
	public static String getTodaysDate()
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(c.getTime());		
	}
	
	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			// TODO: FERROL - move string to Constants.
		    if ("edu.emory.sph.stepsmart.StepService".equals(service.service.getClassName())) {
		        return true;
		    }
		}
		return false;
    }

    public static int numberOfDaysSinceDate (String date)
    {
        int num_days = -1;
        if (TextUtils.isEmpty(date))
            return num_days;

        Calendar lastIncrease = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            lastIncrease.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return num_days;
        }
        Calendar today = Calendar.getInstance();
        long diff = today.getTimeInMillis() - lastIncrease.getTimeInMillis();
        if (diff < 0)
            return num_days;
        num_days = (int) (diff / (24 * 60 * 60 * 1000));
        return num_days;
    }

}
