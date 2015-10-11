package edu.emory.sph.stepsmart.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import edu.emory.sph.stepsmart.StepSmartApp;

public class DatabaseQueries {


	public ArrayList<Aggregate> getWeeklyData() {
		ArrayList<Aggregate> retval = null;
		// Get database
		SQLiteDatabase db =  StepSmartApp.getInstance().getDb();
		Cursor c = db.rawQuery("SELECT strftime('%W', date) AS week, goal, date, sum(steps) as sum_steps, avg(steps) as avg_steps FROM daily_steps GROUP BY week order by week asc", null);
		if (null != c) {
			if (c.moveToFirst()) {
				retval = new ArrayList<Aggregate>();
				do {
					Aggregate aggregate = new Aggregate();
					aggregate.week = c.getInt(c.getColumnIndex("week"));
					aggregate.goal = c.getInt(c.getColumnIndex(DailyStepsTable.COL_GOAL));
					aggregate.date = c.getString(c.getColumnIndex(DailyStepsTable.COL_DATE));
					aggregate.sum_steps = c.getInt(c.getColumnIndex("sum_steps"));
					aggregate.avg_steps = c.getInt(c.getColumnIndex("avg_steps"));
					retval.add(aggregate);
				} while (c.moveToNext());
			}
			c.close();
		}
		db.close();
		return retval;
	}

	public int getWeekTotal() {
		int retval = 0;
		// Get database
		SQLiteDatabase db =  StepSmartApp.getInstance().getDb();

		// TODO: FERROL -change this query to be the total for all days from Sunday till now.
		Cursor c = db.rawQuery("SELECT strftime('%W', date) AS week, sum(steps) FROM daily_steps GROUP BY week order by week", null);
		if (null != c) {
			if (c.moveToLast()) {
				retval = c.getInt(1);
			}
			c.close();
		}
		db.close();	
		return retval;
	}
	
	public int getMonthTotal() {
		int retval = 0;	
		SQLiteDatabase db =  StepSmartApp.getInstance().getDb();

		// TODO: FERROL -change this query to be the total for all days from beginning of month till now.
		Cursor c = db.rawQuery("SELECT strftime('%m', date) AS month, sum(steps) FROM daily_steps GROUP BY month order by month", null);
		if (null != c) {
			if (c.moveToLast()) {
				retval = c.getInt(1);
			}
			c.close();
		}
		db.close();			
		return retval;
	}
	
//	public int getDailyAverage() {
//		int retval = 0;
//		// Get database
//		SQLiteDatabase db =  StepSmartApp.getInstance().getDb();
//
//		Cursor c = db.rawQuery("SELECT strftime('%W', date) theweek, avg(steps) FROM daily_steps GROUP BY strftime('%W', date)", null);
//		if (null != c) {
//			if (c.moveToLast()) {
//				retval = c.getInt(1);
//			}
//			c.close();
//		}
//		db.close();			
//		return retval;
//	}
	
	public int getWeeklyAverage() {
		int retval = 0;
		// Get database
		SQLiteDatabase db =  StepSmartApp.getInstance().getDb();

		Cursor c = db.rawQuery("SELECT strftime('%W', date) theweek, avg(steps) FROM daily_steps GROUP BY strftime('%W', date)", null);
		if (null != c) {
			if (c.moveToLast()) {
				retval = c.getInt(1);
			}
			c.close();
		}
		db.close();			
		return retval;
	}
	
	public int getMonthlyAverage() {
		int retval = 0;
		// Get database
		SQLiteDatabase db =  StepSmartApp.getInstance().getDb();

		Cursor c = db.rawQuery("SELECT strftime('%m', date) themonth, avg(steps) FROM daily_steps GROUP BY strftime('%m', date)", null);
		if (null != c) {
			if (c.moveToLast()) {
				retval = c.getInt(1);
			}
			c.close();
		}
		db.close();			
		return retval;
	}
}

