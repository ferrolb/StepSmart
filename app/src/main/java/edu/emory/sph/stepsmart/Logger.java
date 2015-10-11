package edu.emory.sph.stepsmart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
//File root = android.os.Environment.getExternalStorageDirectory(); 
//String path = root.getAbsolutePath();
//Log.i("##### Main Activity ######", "path =" + path);
//DumpToFile dtf = new DumpToFile(path, "outputfile.txt");
//Log.i("##### Main Activity ######", "External readable: " + dtf.isExternalStorageReadable());
//Log.i("##### Main Activity ######", "External writable: " + dtf.isExternalStorageWritable());
//File file = dtf.getAlbumStorageDir("TestAlbum");
//dtf.open();
//String r = "Rawson is a butthead";
//dtf.write(r);
//dtf.close();
public class Logger extends DumpToFile {
	
	private					SharedPreferences mSP				= null;
	public Logger(String path, String filename) {
		super(path, filename);
		// TODO Auto-generated constructor stub
	}
	
//	synchronized public void threadSafeWrite ( DumpToFile dumpToFile )
//	{
//		FileInputStream in = new FileInputStream(file);
//		try {
//		    java.nio.channels.FileLock lock = in.getChannel().lock();
//		    try {
//		        Reader reader = new InputStreamReader(in, charset);
//		        ...
//		    } finally {
//		        lock.release();
//		    }
//		} finally {
//		    in.close();
//		}
//	}

	synchronized public void tsWrite( Context ctx, String str )
	{
		open();
		mSP = ctx.getSharedPreferences(Constants.PREFERENCES, Activity.MODE_PRIVATE);
	    int steps = mSP.getInt(Constants.DAILY_STEPS, 0);
	    String date = mSP.getString(Constants.STEPS_DATE, "");
		write(Constants.getCurrentDateTime() + " [" +steps+ "," +date+ "] " +str+"\n");
		close();
	}	
	
	public static void loadFileToTextView( TextView v, String path, String filename)
	{
		if (v==null || path==null || filename==null ||
			(path.length() <=0) || (filename.length() <=0) ) 
			return;
		
		
		//FileReader fr = null;
		File file = new File( path, filename );
		try {
			
			//FileInputStream in = new FileInputStream(file);
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			java.nio.channels.FileChannel channel = raf.getChannel();
			java.nio.channels.FileLock lock = channel.lock();
			try {
				//Reader reader = new InputStreamReader(in);

			
		    //fr = new FileReader(file);
		    
			    //BufferedReader br = new BufferedReader(/*fr*/reader);
			    //String line = br.readLine();
				String line = raf.readLine();
			    while (null != line) {
			        v.append(line);
			        v.append("\n");
			        line = raf.readLine();
			    }
			    //raf.close();
			    
			} finally {lock.release(); raf.close();}

		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}		
	}
	
}
