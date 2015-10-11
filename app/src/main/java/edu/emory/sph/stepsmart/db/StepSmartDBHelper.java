package edu.emory.sph.stepsmart.db;

import edu.emory.sph.stepsmart.db.DailyStepsTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StepSmartDBHelper extends SQLiteOpenHelper {

    public static final String DB_FILE = "stepsmart.db";
    public static final String DB_PATH = "//data/data/edu.emory.sph.stepsmart/databases/" + DB_FILE;
    public static final int VERSION = 1;

	public StepSmartDBHelper(Context context) {
		super(context, DB_FILE, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		DailyStepsTable.onCreate(db);
		Log.i("StepSmartDBHelper.onCreate()", "Database created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

}
