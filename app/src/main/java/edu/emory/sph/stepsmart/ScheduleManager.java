package edu.emory.sph.stepsmart;

import java.io.File;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleManager extends BroadcastReceiver {
	
	private Context mContext;
	private Logger mLog;
	
	private void log (String str)
	{
		if (null == mLog)
			createLogger();
		mLog.tsWrite(mContext, str);
	}
	
	private void log (Context ctx, String str)
	{
		if (ctx == null)
			log(str);
		else 
		{
			mContext = ctx;
			log(str);
		}
	}
	
	private void createLogger()
	{
		File root = android.os.Environment.getExternalStorageDirectory(); 
		String path = root.getAbsolutePath();
		mLog = new Logger(path, "outputfile.txt");		
	}
	
	public ScheduleManager(Context context)
	{
		mContext = context;
		
		createLogger();
	}
	
	public ScheduleManager()
	{
		
	}
	
	public void setAlarm()
	{
		log("Alarm set");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.add(Calendar.DAY_OF_MONTH, 1); 
		
	    Intent intent = new Intent(Constants.CLEAR_DAILY_TOTAL_INTENT); 
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (24 * 1000 * 60 * 60), alarmIntent);		
//		PendingIntent pi = ;
//		am.cancel(pi); // cancel any existing alarms
//		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//		    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
//		    AlarmManager.INTERVAL_DAY, pi);		
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		log(context, "Alarm triggered");

		// Start it up
		context.startService(new Intent(context, StepService.class));
	}

}
