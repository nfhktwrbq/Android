package ru.andrew.meteostation;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class Main2Activity extends AppCompatActivity {

    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private TextView PDev;
    private Button button2;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mAdapter = null;
    private BluetoothDevice mDevice = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        PDev = (TextView) findViewById(R.id.textView1);
        //set invisible textview finded dev
        PDev.setVisibility(View.INVISIBLE);

        //init BT adapter
        mAdapter = BluetoothAdapter.getDefaultAdapter();
       /* if(!mAdapter.isEnabled()) mAdapter.enable();
        while(!mAdapter.isEnabled()){}*/

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item);
        ListView pairedListView = (ListView) findViewById(R.id.listView);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);

        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);
                    // EXTRA_DEVICE_ADDRESS = info.substring(info.length() - 17);
                    //Create the result Intent and include the MAC address
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                    // Set result and finish this Activity
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    //button2 = (Button) findViewById(R.id.button2);
                    //button2.setText(address);
                }
        });


        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.textView1).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());
            }
        } else {
            //  String noDevices = getResources().getText(R.string.hello_world2).toString();
            PDev.setText(R.string.not_paired);
            mPairedDevicesArrayAdapter.add(PDev.getText().toString());
        }
    }

}

