package edu.emory.sph.stepsmart;

import java.io.File;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {

	private Logger mLog = null;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.i("####### RebootReceiver.onReceive ######", "Phone reboot detected");

		File root = android.os.Environment.getExternalStorageDirectory(); 
		String path = root.getAbsolutePath();
//		Log.i("####### RebootReceiver.constructor ######", "right before new Logger");
//		android.os.Debug.waitForDebugger();
		mLog = new Logger(path, "outputfile.txt");
		log(context, "Reboot detected");
		//SharedPreferences SP = context.getSharedPreferences(Constants.PREFERENCES, Activity.MODE_PRIVATE);
		//SP.getString(key, defValue);
		if (!Constants.isMyServiceRunning(context))
		{
			// Start it up
			context.startService(new Intent(context, StepService.class));
		}
	}
	
//  private boolean isMyServiceRunning(Context context) {
//	ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//		// TODO: FERROL - move string to Constants.
//	    if ("edu.emory.sph.stepsmart.StepService".equals(service.service.getClassName())) {
//	        return true;
//	    }
//	}
//	return false;}  
  
  private void log (Context ctx, String str){
	  mLog.tsWrite(ctx, str);
  }
	
}
