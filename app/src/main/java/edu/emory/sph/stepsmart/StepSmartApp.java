package edu.emory.sph.stepsmart;
import edu.emory.sph.stepsmart.db.StepSmartDBHelper;
//
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;


// TODO: rename this class

public class StepSmartApp extends Application 
{

    private static StepSmartApp instance;

    public static StepSmartApp getInstance() {
        return instance;
    }
    
	private StepSmartDBHelper dbHelper;
//	private Thread uiThread;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
//		uiThread = Thread.currentThread();
		dbHelper = new StepSmartDBHelper(this);
		instance = this;
	}
	
	public SQLiteDatabase getDb()
	{
		// TODO: FERROL - remove main thread check for now.
//		if (Thread.currentThread().equals(uiThread))
//		{
//			throw new RuntimeException("Database opened on main thread");
//		}
		// creates the database tables if they don't exist
		return dbHelper.getWritableDatabase();
	}
	
//	public void closeDb()
//	{
//		if (null != dbHelper)
//		{
//			dbHelper.close();
//		}
//	}
	
}
