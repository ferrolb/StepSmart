package edu.emory.sph.stepsmart.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DailyStepsTable 
{
	  // Database table
	  public static final String TABLE_NAME = "daily_steps";
	  public static final String COL_ID = "_id";
	  public static final String COL_DATE = "date";
	  public static final String COL_STEPS = "steps";
	  public static final String COL_GOAL = "goal";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement, " 
	      + COL_DATE + " text not null, " 
	      + COL_STEPS + " integer not null,"
		  + COL_GOAL + " integer not null"
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(DailyStepsTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(database);
	  }

}
