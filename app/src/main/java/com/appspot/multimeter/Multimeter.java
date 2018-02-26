package com.appspot.multimeter;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;

import it.beppi.knoblibrary.Knob;

public class Multimeter extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // knob states
    public static final int EXIT_STATE = 0;
    public static final int MA500_STATE = 1;
    public static final int OFF_STATE = 2;
    public static final int V3_STATE = 3;
    public static final int V5_STATE = 4;


    private Knob knob;
    private TextView value;

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;


    private static final String TAG = Multimeter.class.getSimpleName();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private BluetoothGattCharacteristic mModeCharacteristic;
    private BluetoothGattCharacteristic mMeasurementCharacteristic;

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                // open intro activity and close current activity
                final Intent newIntent = new Intent(Multimeter.this, MainActivity.class);
                startActivity(newIntent);
                finish();
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //get current multimeter mode
                BluetoothGattService multimeterService =
                        mBluetoothLeService.getService(UUID.fromString(MultimeterGattAtrributes.MULTIMETER_SERVICE));
                mModeCharacteristic =
                        multimeterService.getCharacteristic(UUID.fromString(MultimeterGattAtrributes.MODE));
                mBluetoothLeService.readCharacteristic(mModeCharacteristic);
                mMeasurementCharacteristic =
                        multimeterService.getCharacteristic(UUID.fromString(MultimeterGattAtrributes.MEASUREMENT));
                //subscribe notifications
                mBluetoothLeService.setCharacteristicNotification(mMeasurementCharacteristic, true);
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //set mode
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter);

        // init knob
        knob = (Knob) findViewById(R.id.knob);
        knob.setOnStateChanged(knobChanged);
        value = (TextView) findViewById(R.id.txtValue);

        // set value font
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/digital.ttf");
        value.setTypeface(myTypeface);
        value.setText("123456789");

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    Knob.OnStateChanged knobChanged = new Knob.OnStateChanged() {
        @Override
        public void onState(int state) {
            switch (state) {
                case EXIT_STATE:
                    // Disconnect and return to intro page
                    AlertDialog.Builder builder = new AlertDialog.Builder(Multimeter.this);
                    builder.setMessage("Exit Multimeter?").setTitle("Please confirm exit");
                    builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mBluetoothLeService.disconnect();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
                case MA500_STATE:
                    mBluetoothLeService.writeCharacteristic(mModeCharacteristic, (byte) 3);
                    break;
                case OFF_STATE:
                    mBluetoothLeService.writeCharacteristic(mModeCharacteristic, (byte) 0);
                    // deal
                    break;
                case V3_STATE:
                    mBluetoothLeService.writeCharacteristic(mModeCharacteristic, (byte) 1);
                    break;
                case V5_STATE:
                    mBluetoothLeService.writeCharacteristic(mModeCharacteristic, (byte) 2);
                    break;
                default:
                    break;
            }
        }
    };
}
