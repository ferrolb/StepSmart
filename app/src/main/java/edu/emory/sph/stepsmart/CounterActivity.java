package edu.emory.sph.stepsmart;

import com.todddavies.components.progressbar.ProgressWheel;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.emory.sph.stepsmart.R;
import edu.emory.sph.stepsmart.authentication.AccountHelper;

public class CounterActivity extends Activity 
{

    private TextView mSteps;
    private ProgressBar mProgressBar;
	private TextView mGoalView;
	private Button mSettingsBtn;
    private TextView mVersion;
    boolean mActivityRunning;
	private	SharedPreferences mSP;
	private double goal = 0.0;
    private Context _context = null;
    private Account _account = null;
    private GetAuthTokenTask _getAuthTokenTask = null;
//	private ProgressWheel mProgressWheel;

    private ImageView iv;
    private Animation animation;
    
    // =====================
    // Life cycle methods
    // =====================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.app_name);


        PackageInfo pInfo = null;
        String version = "0.0.0";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            mVersion = (TextView) findViewById(R.id.lblVersion);
            mVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mSteps = (TextView) findViewById(R.id.txtSteps);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mProgressBar.setProgress(0);
        mSteps.setText("0");
//        mProgressWheel = (ProgressWheel) findViewById(R.id.pw_spinner);
//        mProgressWheel.setProgress(0);

        mSP = this.getSharedPreferences(Constants.PREFERENCES, Activity.MODE_PRIVATE);

        mSettingsBtn = (Button) findViewById(R.id.settingsBtn);
        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CounterActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        iv = (ImageView) findViewById(R.id.imageView);

        // #=#=#
        if (!isInstallDateSaved()) {
            saveInstallDateAndInitDailyGoal();
        }

        // Start the Step Service
        this.startService(new Intent(this, StepService.class));

        if (AccountHelper.getInstance(this).accountExists()) {

            _account = AccountHelper.getInstance(_context).getAccount();

            // Uncomment this line to test invalidated token.
            // AccountHelper.getInstance(_context).invalidateAuthToken(AccountHelper.ACCOUNT_TYPE, AccountHelper.AUTH_TOKEN);

            // TODO: FERROL - uncomment this to have user login
            // startUserLoginActivity();

        } else
            startUserRegistrationActivity();


    }

    private boolean isInstallDateSaved() {
        boolean retval = true;
        String install_date = mSP.getString(Constants.INSTALL_DATE, "");
        if (TextUtils.isEmpty(install_date))
            retval = false;
        return retval;
    }

    private void saveInstallDateAndInitDailyGoal () {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putString(Constants.INSTALL_DATE, Constants.getTodaysDate());
        editor.putInt(Constants.DAILY_GOAL, 0);
        editor.apply();
    }

    @Override
    protected void onStop() {
    	super.onStop();
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		// Unbind from service
    	this.unbindService(mServiceConnection);
    }
    
    @Override
    protected void onResume() {
        super.onResume();


        if(AccountHelper.getInstance(this).accountExists()) {

            _account = AccountHelper.getInstance(_context).getAccount();

            // Uncomment this line to test invalidated token.
            // AccountHelper.getInstance(_context).invalidateAuthToken(AccountHelper.ACCOUNT_TYPE, AccountHelper.AUTH_TOKEN);

            // Get Auth Token.  This will either get a token if the current token is not invalidated
            // or present the UserLogin Activity to re-authenticate the user and set a new Auth Token.
            _getAuthTokenTask = new GetAuthTokenTask(this);
            _getAuthTokenTask.execute();

        }

	    goal = (double) (mSP.getInt(Constants.DAILY_GOAL, 0));
        mGoalView = (TextView) findViewById(R.id.textGoal);
        mGoalView.setText(""+((int) goal));

	    
	    int daily_total = mSP.getInt(Constants.DAILY_STEPS, 0);

        // #=#=#
        double daily_ratio = 0.0;
        if (goal!=0.0)
            daily_ratio = (daily_total / goal > 1.0) ? 100.0 : (daily_total / goal)*100.0;

        mProgressBar.setProgress((int) daily_ratio);
        mSteps.setText(daily_total + "");
//        int percent = (int) (daily_ratio * 360.0);
//        mProgressWheel.setProgress(percent);
//        mProgressWheel.setText(daily_total + "");


        //animation = AnimationUtils.loadAnimation(this, R.anim.translate);
        //iv.startAnimation(animation);

        animateFigure( daily_ratio, goal, false );
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
 
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
      	      new IntentFilter("edu.emory.sph.stepsmart.broadcast_steps"));
    	
        // Bind to the Step Service
        this.bindService(new Intent(this, StepService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void animateFigure(double in_daily_ratio, double in_goal, boolean fromSteps) {

        // create set of animations
        AnimationSet replaceAnimation = new AnimationSet(false);

        // animations should be applied on the finish line
        replaceAnimation.setFillAfter(true);
        replaceAnimation.setFillBefore(false);

        // create translation animation
        in_daily_ratio = (in_daily_ratio==0.0) ? 100.0 : in_daily_ratio;

        int starting_point = TranslateAnimation.RELATIVE_TO_PARENT;
        if (fromSteps)
            starting_point = TranslateAnimation.RELATIVE_TO_SELF;

        TranslateAnimation trans = new TranslateAnimation(  starting_point, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, (float) (in_daily_ratio/100.0),
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);
        trans.setDuration(4000);

        if (in_goal==0.0)
            trans.setRepeatCount(5);

        // add new animations to the set
        replaceAnimation.addAnimation(trans);

        // start our animation
        iv.startAnimation(replaceAnimation);
    }

	// Handler for received Intents. 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    // Get extra data included in the Intent
	    //int steps = intent.getIntExtra(Constants.EXTRA_STEP_COUNT, 0);
	    // Load totals from Preferences
	    int daily_total = mSP.getInt(Constants.DAILY_STEPS, 0);
        mSteps.setText(daily_total+"");
//	    mProgressWheel.setText(daily_total+"");
        // #=#=#
        double daily_ratio = 0.0;
        if (goal!=0.0)
	        daily_ratio = (daily_total/goal>1.0) ? 100.0 : (daily_total/goal)*100.0;
	    int percent = (int) (daily_ratio);
//	    mProgressWheel.setProgress(percent);
        mProgressBar.setProgress(percent);

//         if (daily_ratio>0.0) {
//             // TODO: move to onCreate
//             Display display = getWindowManager().getDefaultDisplay();
//             Point size = new Point();
//             display.getSize(size);
//
//             iv.setX( size.x * (float) (daily_ratio/100.0) );
//         }

//        if (goal!=0.0)
//            animateFigure( daily_ratio, goal, true );

	  }
	};

    // =====================
    // Service methods
    // =====================
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//stepServiceRef = ((StepService.StepBinder)service).getService();
			//Log.i("##### CounterActivity #####", "onServiceConnected");
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			//stepServiceRef = null;
			//Log.i("##### CounterActivity #####", "onServiceDisconnected");
		}
    };



    /**
     * Starts the UserRegistrationActivity Activity.
     */
    private void startUserRegistrationActivity() {


        Intent myIntent = new Intent(this, UserRegistrationActivity.class);
        this.startActivityForResult(myIntent, REGISTRATION_ACTIVITY);
    }

    /**
     * Starts the UserLogin Activity.
     */
    static final int LOGIN_ACTIVITY = 1;
    static final int REGISTRATION_ACTIVITY = 2;
    private void startUserLoginActivity() {

        Intent myIntent = new Intent(this, UserLoginActivity.class);
        this.startActivityForResult(myIntent, LOGIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOGIN_ACTIVITY) {

            if (resultCode == RESULT_OK) {
                Log.i("### WelcomeActivity ###", "Login Activity returned okay");
            } else {
                Log.i("### WelcomeActivity ###", "Login Activity returned error");
                startUserLoginActivity();
            }
        } else if (requestCode == REGISTRATION_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                Log.i("### WelcomeActivity ###", "Registration Activity returned okay");
            } else {
                Log.i("### WelcomeActivity ###", "Registration Activity returned error");
                startUserRegistrationActivity();
            }
        }
    }


    /**
     * Callback for onPostExecute() of GetAuthTokenTask
     * @param result The result of the call to get the Auth Token.
     */
    public void onGetAuthTokenResult(Bundle result) {

        String METHOD_TAG;
        METHOD_TAG = "CounterActivity" + ".onGetAuthTokenResult()";

        Log.d(METHOD_TAG, "Get Auth Token task is complete.");

        // Our task is complete, so clear it out.
        _getAuthTokenTask = null;

        // Check the bundle for the token.  If one is found, go to app home.
        if(result != null && result.getString(AccountManager.KEY_AUTHTOKEN) != null ) {

            Log.d(METHOD_TAG, "Retrieval of Auth Token was successful. Starting ApplicationHome Activity.");

        }
        else {

            // TODO: remove this when the UserLogin Activity can be started by the system in getAuthToken in the Authenticator.
            Log.d(METHOD_TAG, "Retrieval of Auth Token was unsuccessful. Starting UserLogin Activity.");

            startUserLoginActivity();
        }

    }

    /**
     * Callback for onCancelled() of GetAuthTokenTask
     */
    public void onGetAuthTokenCancel() {

        String METHOD_TAG;
        METHOD_TAG = "CounterActivity" + ".onGetAuthTokenCancel()";

        Log.d(METHOD_TAG, "Get Auth Token task was cancelled.");

        // Our task is complete, so clear it out.
        _getAuthTokenTask = null;

    }

    /**
     * Class Name: GetAuthTokenTask
     * Description: Represents an asynchronous task used to get an auth token.
     */
    private class GetAuthTokenTask extends AsyncTask<Void, Void, Bundle> {


        private Activity _activity = null;

        static final String CLASS_TAG = "GetAuthTokenTask";


        public GetAuthTokenTask(Activity a) {

            _activity = a;
        }

        @Override
        protected Bundle doInBackground(Void... params) {

            String METHOD_TAG;
            METHOD_TAG = CLASS_TAG + ".doInBackground()";

            // Get Auth Token using the AccountHelper class.
            try {

                return AccountHelper.getInstance(_context).getAuthToken(_account, AccountHelper.AUTHTOKEN_TYPE_FULL_ACCESS, null, _activity, null, null);

            } catch (Exception e) {

                Log.e(METHOD_TAG, "Failed to get Auth Token.");
                Log.e(METHOD_TAG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bundle result) {

            String METHOD_TAG;
            METHOD_TAG = CLASS_TAG + ".onPostExecute()";

            Log.d(METHOD_TAG, "Call to get Auth Token successful. Returning result to UI thread.");

            // Return the get Auth Token result to the Activity.
            onGetAuthTokenResult(result);
        }

        @Override
        protected void onCancelled() {

            // If the action was canceled , then call back into the
            // Activity to let it know.
            onGetAuthTokenCancel();
        }

    }

}
