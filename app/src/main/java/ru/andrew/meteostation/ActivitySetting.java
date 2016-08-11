package ru.andrew.meteostation;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class ActivitySetting extends AppCompatActivity {

    private static final String TAG = "Settings";
    private static final boolean D = true;

    private SeekBar seekBar;
    private SeekBar colorBar;
    private Button ExampleButton;
    //Variable to store brightness value

    private int brightness;
    //Content resolver used as a handle to the system's settings

    private int color;


    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window

    private Window window;
    TextView txtPerc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_setting);

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

        Intent intentEx = getIntent();
        color = intentEx.getIntExtra("color", 255);
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
                //Set the minimal brightness level
                //if seek bar is 20 or any value below
                // if (progress <= 20)
                //  {
                //Set the brightness to 20
                //    brightness = 20;
                //  }
                // else //brightness is greater than 20
                //  {
                //Set brightness variable based on the progress bar
                brightness = progress;
                // }

                //Calculate the brightness percentage
                float perc = (brightness / (float) 255) * 100;
                //Set the brightness percentage
                txtPerc.setText((int) perc + " %");
            }
        });

        colorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar)
            {
               // Intent intent = new Intent();
                Intent intent = new Intent();
                intent.putExtra("color", color);
                setResult(RESULT_OK, intent);
            }

            public void onStartTrackingTouch(SeekBar seekBar)
            {
                //Nothing handled here
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {

                color = progress;
                ExampleButton.setTextColor(Color.argb(255, color, color, color));

            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Stop the Bluetooth chat services
        finish();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }
}
