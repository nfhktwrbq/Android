package ru.andrew.meteostation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mPlanetTitles;

    private SeekBar seekBar;
    private SeekBar colorBar;
    private Button ExampleButton;
    //Variable to store brightness value

    private int brightness;
    //Content resolver used as a handle to the system's settings

    private int color = 255;

    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window

    private Window window;
    TextView txtPerc;



    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    //public boolean meteoDataReceived = false;
    public Handler mHandler;
    public int i;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 2;
    public static final int REQUEST_ENABLE_BT = 3;
    public static final int REQUEST_DEMOTEXT_COLOR = 4;

    public boolean changeColorDemoText = false;

    // Layout Views
    private TextView mReadTextView;
    private TextView mTextViewTO;
    private TextView mTextViewTI ;
    private TextView mTextViewHum;
    private TextView mTextViewPress;
    private TextView mTextViewTime;
    private Button ModeButton;
    private Button SyncButton;
    private ActionBar actionBar;
    private View decorView;
    private RelativeLayout layout;
   // private RelativeLayout.LayoutParams params1;
    private RelativeLayout.LayoutParams params2;
   // private RelativeLayout.LayoutParams params3;
    private RelativeLayout.LayoutParams params4;
    private DisplayMetrics dm = new DisplayMetrics();
    private short temp_in = 0;
    private short temp_out = 0;
    private short hum = 0;
    private short press = 0;
    private short CS = 0;
    private byte[] readBuf;
    private byte[] StartDemo = new byte[1];
    public String[] Data =  {" "," "," "," "};
    private char sign='+';
    private byte counter=0;
    private StringBuilder sb = new StringBuilder();

    //protected boolean flagStart = false;

    byte[] dateTime = new byte[8];

    Button GetTempButton;

    boolean flagGetTemp = false;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    public BluetoothChatService mChatService = null;
    // Member object to form string from BT for handler



    private String currentDateTimeString = null;



    /* private Handler DemoHandler = new Handler();
   private Runnable DemoRunnable = new Runnable() {
        @Override
        public void run() {
            //mTextViewDemo.setText(String.valueOf(hum++));
            dateTime[7] = 0x04; // for get temp_in
            if(mChatService!=null) {
                sendBytes(dateTime);
            }
            DemoHandler.postDelayed(this, 5000);

        }
    };*/

    private Handler TimeHandler = new Handler();
    private Runnable TimeRunnable = new Runnable() {
        @Override
        public void run() {
            //mTextViewDemo.setText(String.valueOf(hum++));
            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            currentDateTimeString = dateFormat.format(c.getTime());
            mTextViewTime.setText(currentDateTimeString);
            TimeHandler.postDelayed(this, 1000);
          //  if( decorView.getSystemUiVisibility()!=(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION + View.SYSTEM_UI_FLAG_FULLSCREEN))
          //  {
          //      mTextViewTO.setText("qrwqerqwt");
          //      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION + View.SYSTEM_UI_FLAG_FULLSCREEN);
          //  }
        }
    };

   /* private Handler FullScreenHandler = new Handler();
    private Runnable FullScreenRunnable = new Runnable() {
        @Override
        public void run() {
            if( decorView.getSystemUiVisibility()!=0)
            {
                mTextViewTO.setText("qrwqerqwt");
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION + View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        colorBar = (SeekBar) findViewById(R.id.seekBar2);
        txtPerc = (TextView) findViewById(R.id.txtPercentage);
        ExampleButton = (Button) findViewById(R.id.button);

        //Get the content resolver
        cResolver =  getContentResolver();

        //Get the current window
        window = getWindow();

        //Set the seekbar range between 0 and 255
        //seek bar settings//
        //sets the range between 0 and 255
        seekBar.setMax(255);
        colorBar.setMax(255);

        //set the seek bar progress to 1
        seekBar.setKeyProgressIncrement(1);
        colorBar.setKeyProgressIncrement(1);

        try
        {
            //Get the current system brightness
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        }

        catch (Settings.SettingNotFoundException e)
        {
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }

        //Set the progress of the seek bar based on the system's brightness
        seekBar.setProgress(brightness);


        colorBar.setProgress(color);
        ExampleButton.setTextColor(Color.argb(255, color, color, color));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Set the system brightness using the brightness variable value
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);

                //Get the current window attributes
                WindowManager.LayoutParams layoutpars = window.getAttributes();

                //Set the brightness of this window
                layoutpars.screenBrightness = brightness / (float) 255;

                //Apply attribute changes to this window
                window.setAttributes(layoutpars);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //Nothing handled here
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = progress;
                //Calculate the brightness percentage
                float perc = (brightness / (float) 255) * 100;
                //Set the brightness percentage
                txtPerc.setText((int) perc + " %");
            }
        });

        colorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Intent intent = new Intent();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Nothing handled here
            }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                color = progress;
                ExampleButton.setTextColor(Color.argb(255, color, color, color));
                mTextViewPress.setTextColor(Color.rgb(color, color, color));
                mTextViewTO.setTextColor(Color.rgb(color, color, color));
                mTextViewTI.setTextColor(Color.rgb(color, color, color));
                mTextViewTime.setTextColor(Color.rgb(color, color, color));
                mTextViewHum.setTextColor(Color.rgb(color, color, color));
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ModeButton = (Button) findViewById(R.id.mode_but);
        SyncButton = (Button) findViewById(R.id.Sync_but);
        GetTempButton = (Button) findViewById(R.id.button_send);
        mTextViewTO = (TextView)findViewById(R.id.textViewTO);
        mTextViewTI = (TextView)findViewById(R.id.textViewTI);
        mTextViewHum = (TextView)findViewById(R.id.textViewHum);
        mTextViewPress = (TextView)findViewById(R.id.textViewPress);
        mTextViewTime = (TextView)findViewById(R.id.textViewTime);
        decorView = getWindow().getDecorView();
        actionBar = getSupportActionBar();
        layout = (RelativeLayout) findViewById(R.id.relative_layout);
      /*  params1 =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);*/
        params2 =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
      /*  params3 =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);*/
        params4 =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        registerForContextMenu(ModeButton);
        mHandler = new MyHandler(this);
        mHandler.sendEmptyMessageDelayed(0, 100);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else
        {
            if (mChatService == null)
            {
                setupChat();
            }
        }
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            DemoMode();
            GetTempButton.setVisibility(View.INVISIBLE);
            ModeButton.setVisibility(View.INVISIBLE);
            SyncButton.setVisibility(View.INVISIBLE);
            layout.setBackgroundColor(Color.BLACK);
          //  if(changeColorDemoText)
           // {
            mTextViewPress.setTextColor(Color.rgb(color, color, color));
            mTextViewTO.setTextColor(Color.rgb(color, color, color));
            mTextViewTI.setTextColor(Color.rgb(color, color, color));
            mTextViewTime.setTextColor(Color.rgb(color, color, color));
            mTextViewHum.setTextColor(Color.rgb(color, color, color));
          //  }
           // else
           // {
          //      mTextViewPress.setTextColor(Color.WHITE);
           //     mTextViewTO.setTextColor(Color.WHITE);
           //     mTextViewTI.setTextColor(Color.WHITE);
          //      mTextViewTime.setTextColor(Color.WHITE);
           //     mTextViewHum.setTextColor(Color.WHITE);
          //  }

            WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            params4.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            mTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, dm.heightPixels / 6);
            mTextViewTime.setLayoutParams(params4);
            mTextViewTI.setTextSize(TypedValue.COMPLEX_UNIT_PX, dm.heightPixels / 8);
            mTextViewTO.setTextSize(TypedValue.COMPLEX_UNIT_PX, dm.heightPixels / 8);
            mTextViewPress.setTextSize(TypedValue.COMPLEX_UNIT_PX, dm.heightPixels / 8);
            mTextViewHum.setTextSize(TypedValue.COMPLEX_UNIT_PX, dm.heightPixels / 8);
            //DemoHandler.postDelayed(DemoRunnable, 5000);
            StartDemo[0] = 0x0a; // start demo
            if(mChatService!=null) {
                sendBytes(StartDemo);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            GetTempButton.setVisibility(View.VISIBLE);
            ModeButton.setVisibility(View.VISIBLE);
            SyncButton.setVisibility(View.VISIBLE);
            layout.setBackgroundColor(Color.WHITE);
            mTextViewPress.setTextColor(Color.BLACK);
            mTextViewTO.setTextColor(Color.BLACK);
            mTextViewTI.setTextColor(Color.BLACK);
            mTextViewTime.setTextColor(Color.BLACK);
            mTextViewHum.setTextColor(Color.BLACK);
            params2.addRule(RelativeLayout.BELOW, SyncButton.getId());
            mTextViewTime.setLayoutParams(params2);
            mTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            mTextViewTI.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            mTextViewTO.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            mTextViewPress.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            mTextViewHum.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            //DemoHandler.removeCallbacks(DemoRunnable);
            StartDemo[0] = 0x0b; // stop demo
            if(mChatService!=null) {
                sendBytes(StartDemo);
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo contextMenuInfo)
    {
        menu.add(0, 1, 0, getResources().getString(R.string.mode1));
        menu.add(0,2,0,getResources().getString(R.string.mode2));
        menu.add(0,3,0,getResources().getString(R.string.mode3));
        menu.add(0, 4, 0, getResources().getString(R.string.mode4));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case 1:
                dateTime[6] = 0x00; // for set mode=0
                dateTime[7] = 0x02; // for change mode
                if(sendBytes(dateTime)) ModeButton.setText(getResources().getString(R.string.mode1));
                break;
            case 2:
                dateTime[6] = 0x01; // for set mode=0
                dateTime[7] = 0x02; // for change mode
                if(sendBytes(dateTime)) ModeButton.setText(getResources().getString(R.string.mode2));
                break;
            case 3:
                dateTime[6] = 0x02; // for set mode=0
                dateTime[7] = 0x02; // for change mode
                if(sendBytes(dateTime)) ModeButton.setText(getResources().getString(R.string.mode3));
                break;
            case 4:
                dateTime[6] = 0x03; // for set mode=0
                dateTime[7] = 0x02; // for change mode
                if(sendBytes(dateTime)) ModeButton.setText(getResources().getString(R.string.mode4));
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (D)
            Log.e(TAG, "+++ ON CREATE OM +++");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.BTNA),
                    Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (D)
            Log.d(TAG, "Select Connect");

        Intent serverIntent/* = null*/;
        Intent setIntent;
        switch (item.getItemId())
        {
            case R.id.action_connect:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, Main2Activity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.action_disconnect:
                if (mChatService != null)
                {
                    mChatService.stop();
                    Toast.makeText(this, R.string.disc, Toast.LENGTH_SHORT).show();
                    changeColorDemoText = true;
                }
                return true;
            case R.id.action_demo:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                // mTextViewDemo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
               // mTextViewDemo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                //
               /* Timer t = new Timer();
                t.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        hum++;
                        mTextViewDemo.setText(String.valueOf(hum));
                    }
                }, 5000, 1000);*/
                return true;
            case R.id.setting:
                mDrawerLayout.openDrawer(GravityCompat.START);

                return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    connectDevice(data);
                }
                break;
            case REQUEST_DEMOTEXT_COLOR:
                if (resultCode == Activity.RESULT_OK)
                {
                    color = data.getIntExtra("color", 255);

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else
                {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }



    @Override
    public void onStart()
    {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");

    }

    private void setupChat()
    {
        Log.e(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    @Override
    public void onStop()
    {
        super.onStop();
        if (D)
            Log.e(TAG, "++ ON STop ++");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (D)
            Log.e(TAG, "++ ON pause ++");
        //DemoHandler.removeCallbacks(DemoRunnable);
        TimeHandler.removeCallbacks(TimeRunnable);
        //FullScreenHandler.removeCallbacks(FullScreenRunnable);
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");
        TimeHandler.postDelayed(TimeRunnable, 1000);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null)
        {
            mChatService.stop();
            mChatService = null;
        }

        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }



    static class MyHandler extends Handler {
        WeakReference<MainActivity> wrActivity;
        public MyHandler(MainActivity activity) {
            wrActivity = new WeakReference<MainActivity>(activity);
            }


        // The Handler that gets information back from the BluetoothChatService

        @Override
        public void handleMessage(Message msg)
        {
            MainActivity activity = wrActivity.get();
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1)
                    {
                        case BluetoothChatService.STATE_CONNECTED:
                           /* setStatus(getString(R.string.title_connected_to,
                                    mConnectedDeviceName));
                            mConversationArrayAdapter.clear();*/
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                           /* setStatus(R.string.title_connecting);*/
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                           /* setStatus(R.string.title_not_connected);*/
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    if( writeBuf[0] == 0x04)
                    {
                        activity.counter = 0;
                    }
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //  mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    int i = 0;
                    /*try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                   //meteoDataReceived = true;
                    activity.readBuf = (byte[]) msg.obj;
                    while(activity.readBuf[i++] == -1);
                    Log.e(TAG, String.valueOf(i));
                    activity.temp_in = 0;
                    activity.temp_in |= ((short)activity.readBuf[i] & 0x00ff);
                    activity.temp_in <<= 8;
                    activity.temp_in |= ((short)activity.readBuf[i+1] & 0x00ff);
                    activity.temp_out = 0;
                    activity.temp_out |= ((short)activity.readBuf[i+2] & 0x00ff);
                    activity.temp_out <<= 8;
                    activity.temp_out |= ((short)activity.readBuf[i+3] & 0x00ff);
                    activity.hum = 0;
                    activity.hum |= ((short)activity.readBuf[i+4] & 0x00ff);
                    activity.hum <<= 8;
                    activity.hum |= ((short)activity.readBuf[i+5] & 0x00ff);
                    activity.press = 0;
                    activity.press |= ((short)activity.readBuf[i+6] & 0x00ff);
                    activity.press <<= 8;
                    activity.press |= ((short)activity.readBuf[i+7] & 0x00ff);
                    activity.CS = 0;
                    activity.CS |= ((short)activity.readBuf[i+8] & 0x00ff);
                    activity.CS <<= 8;
                    activity.CS |= ((short)activity.readBuf[i+9] & 0x00ff);
                    //activity.ModeButton.setText(String.valueOf(activity.CS));
                    if(activity.CS == (activity.press+activity.hum+activity.temp_in+activity.temp_out))
                    {
                        Log.e(TAG, "CS match");
                        if( activity.temp_in<0)
                        {
                            activity.sign='-';
                        }
                        activity.temp_in*=6.25;
                        activity.temp_in/=10;
                        activity.mTextViewTI.setText( activity.getResources().getString(R.string.temp_in)+ " " + activity.sign + Math.abs(activity.temp_in/10) + "." + Math.abs(activity.temp_in%10)+" "+ (char)176 +"C");

                        if( activity.temp_out<0)
                        {
                            activity.sign='-';
                        }
                        activity.temp_out*=6.25;
                        activity.temp_out/=10;
                        activity.mTextViewTO.setText(activity.getResources().getString(R.string.temp_out) + " " + activity.sign + Math.abs(activity.temp_out / 10) + "." + Math.abs(activity.temp_out % 10) + " " + (char)176 + "C");
                        activity.mTextViewHum.setText(activity.getResources().getString(R.string.hum_in)+ " " + activity.hum + " " + '%');
                        activity.mTextViewPress.setText(activity.getResources().getString(R.string.pressure)+ " " + activity.press++ +" " + activity.getResources().getString(R.string.mm));
                        activity.sign='+';
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    activity.mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(activity.getApplicationContext(),
                            activity.getResources().getString(R.string.connected_to) + activity.mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(activity.getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }

    }

    public boolean sendBytes(byte[] data)
    {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        // Check that there's actually something to send
        if ( data.length > 0)
        {
            // Get the message bytes and tell the BluetoothChatService to write
            mChatService.write(data);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
        return true;
    }

    private void connectDevice(Intent data)
    {
        // Get the device MAC address
        if (D)
            Log.d(TAG, "connecting");
        String address = data.getExtras().getString(Main2Activity.EXTRA_DEVICE_ADDRESS);

        if (D)
            Log.d(TAG, "connect to: " + address);
        // Get the BluetoothDevice object

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(mBluetoothAdapter!=null) { if (D)
            Log.d(TAG, "mBluetoothAdapter!=null");}// Attempt to connect to the device
        mChatService.connect(device);
    }

    public void BtnClk(View v)
    {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setFirstDayOfWeek(Calendar.SATURDAY);
        SimpleDateFormat df = new SimpleDateFormat("EE dd.MM.yyyy HH:mm:ss");
        String mCurrentTime = df.format(cal.getTime());

        for(int i = 0; i<8; i++)
        {
            dateTime[i]=0;
        }
        // set seconds
        dateTime[0] =  (byte)(mCurrentTime.charAt(20) & 0b00001111);
        dateTime[0] <<= 4;
        dateTime[0] |= (byte)(mCurrentTime.charAt(21)&0b00001111);
        dateTime[0] &= 0b01111111;

        //set minutes
        dateTime[1] =  (byte)(mCurrentTime.charAt(17) & 0b00001111);
        dateTime[1] <<= 4;
        dateTime[1] |= (byte)(mCurrentTime.charAt(18)&0b00001111);

        //set hours
        dateTime[2] = (byte)(mCurrentTime.charAt(14) & 0b00001111);
        dateTime[2] <<= 4;
        dateTime[2] |= (byte)(mCurrentTime.charAt(15)&0b00001111);

        //set day
        dateTime[3] = (byte) (cal.get(cal.DAY_OF_WEEK)-1);

        //set date
        dateTime[4] =  (byte)(mCurrentTime.charAt(3) & 0b00001111);
        dateTime[4] <<= 4;
        dateTime[4] |= (byte)(mCurrentTime.charAt(4)&0b00001111);

        ////set month
        dateTime[5] =  (byte)(mCurrentTime.charAt(6) & 0b00001111);
        dateTime[5] <<= 4;
        dateTime[5] |= (byte)(mCurrentTime.charAt(7)&0b00001111);

        ////set year
        dateTime[6] = (byte)(mCurrentTime.charAt(11) & 0b00001111);
        dateTime[6] <<= 4;
        dateTime[6] |= (byte)(mCurrentTime.charAt(12)&0b00001111);

        dateTime[7] = 0x03;

        // SimpleDateFormat dayF = new SimpleDateFormat("EEEE", Locale.UK);

        //SyncButton.setText(dateTime[0]+"."+dateTime[1]+"."+dateTime[2]+"."+dateTime[3]+"."+dateTime[4]+"."+dateTime[5]+"."+dateTime[6]+"."+dateTime[7]);
        if(sendBytes(dateTime))
        {
            Toast.makeText(this, R.string.sync_succesfull,Toast.LENGTH_SHORT).show();
        }
    }

    public void GetTempButtonClick(View v)
    {
        flagGetTemp=true;
        //GetTempButton = (Button) v;
       // mTextViewTI.setText( "None");
        // for get temp_in
        dateTime[7] = 0x04;
        sendBytes(dateTime);
        flagGetTemp=false;
    }

    public void ModeBtnClk(View v)
    {
        openContextMenu(ModeButton);
    }

    public void ScreenClick(View v)
    {
        if (actionBar != null)
        {
            actionBar.show();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        decorView.setSystemUiVisibility(0);
       // FullScreenHandler.postDelayed(this.FullScreenRunnable, 1000);
       // mTextViewTI.setText(decorView.getSystemUiVisibility() + "");
    }



    public void DemoMode()
    {
        //mTextViewDemo.setVisibility(View.INVISIBLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (actionBar != null) {
            actionBar.hide();
        }
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION + View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
       // mTextViewTI.setText(decorView.getSystemUiVisibility()+"");
    }


}



