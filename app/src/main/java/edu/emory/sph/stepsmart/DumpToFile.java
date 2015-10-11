package edu.emory.sph.stepsmart;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;



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

public class DumpToFile
{
	private String 			mPath 		= "";
	private String 			mFilename 	= "";
	private File 			mFile 		= null;
	private BufferedWriter 	mFileWriter = null;
	
	public String getPath() {return mPath;};
	public String getFilename() {return mFilename;};
	
	public DumpToFile( String path, String filename )
	{
		mPath 		= path;
		mFilename 	= filename;
	}
	
	
	public void open()
	{
		// Open file and initialize variables
		mFile = new File( mPath, mFilename );
		try
		{
			mFile.createNewFile();
			mFileWriter 		= new BufferedWriter( new FileWriter(mFile, true) );
		} 
		
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	

	
	public void write( String str )
	{		
		if ( (null == str) || (str.length()<=0) )
			return;
		
		try 
		{
			mFileWriter.write(str);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
		
	public void close()
	{
		// Close open file after writing
		try
		{
			mFileWriter.flush();
			mFileWriter.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public File getAlbumStorageDir(String albumName) {
	    // Get the directory for the user's public pictures directory.
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.e("DumpToFile", "Directory not created");
	    }
	    return file;
	}
}
