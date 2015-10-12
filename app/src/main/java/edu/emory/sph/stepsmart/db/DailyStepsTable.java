package edu.emory.sph.stepsmart.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DailyStepsTable 
{
	// Database table
	public static final String TABLE = "daily_steps";
	public static final String COL_ID = "_id";
	public static final String COL_DATE = "date";
	public static final String COL_STEPS = "steps";
	public static final String COL_GOAL = "goal";
	public static final String TEMP_TABLE = "temp_"+TABLE;

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE
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

//		if (oldVersion < 2) {
//			db.execSQL(DATABASE_TABLE2);
//		}
//		if (oldVersion < 3) {
//			db.execSQL(DATABASE_TABLE2);
//		}
		  String alter_query1="alter table "+TABLE+" RENAME TO "+TEMP_TABLE+";";
		  String alter_query2="insert into "+TABLE+" select * from "+TEMP_TABLE+";";
		  String alter_query3="DROP TABLE "+TEMP_TABLE+";";

		  database.execSQL(alter_query1);
		  DailyStepsTable.onCreate(database);
		  database.execSQL(alter_query2);
		  database.execSQL(alter_query3);
	  }

}
