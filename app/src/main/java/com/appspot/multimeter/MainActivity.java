package com.appspot.multimeter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private ListView scanList;
    private ImageView voltmeterImage;
    private TextView choose;

    private ProgressDialog mProgressDlg;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;

    private static List<ScanFilter> filters;
    private static ScanSettings settings;
    static {
        ScanFilter filer = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(MultimeterGattAtrributes.MULTIMETER_SERVICE)).build();
        filters = new ArrayList<ScanFilter>();
        filters.add(filer);
        settings = new ScanSettings.Builder().build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        voltmeterImage = (ImageView) findViewById(R.id.voltmeterImg);
        choose = (TextView) findViewById(R.id.txtChoose);

        // load list
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        scanList = (ListView) findViewById(R.id.scanList);
        scanList.setOnItemClickListener(listOnClickListener);
        // hide till results
        scanList.setVisibility(View.GONE);
        choose.setVisibility(View.GONE);

        // progress dialog
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
    }

    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        mLeDeviceListAdapter.clear();
        scanList.setAdapter(mLeDeviceListAdapter);
        scanList.setVisibility(View.GONE);
        choose.setVisibility(View.GONE);
        voltmeterImage.setVisibility(View.VISIBLE);

        // display only if bluetooth enabled
        if (mBluetoothAdapter.isEnabled()) {
            // start scan automatically
            scanLeDevice(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Please Enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                    // discard progress dialog
                    mProgressDlg.dismiss();
                    if (mLeDeviceListAdapter.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
                        scanList.setVisibility(View.GONE);
                        choose.setVisibility(View.GONE);
                    }
                    else {
                        // show list and hide image
                        scanList.setVisibility(View.VISIBLE);
                        choose.setVisibility(View.VISIBLE);
                        voltmeterImage.setVisibility(View.GONE);
                    }
                }
            }, SCAN_PERIOD);
            mProgressDlg.show();
            //mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
            mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mLeScanCallback);
        } else {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
            mProgressDlg.dismiss();
        }
    }

    private AdapterView.OnItemClickListener listOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            if (device == null) return;
            // Move to Multimeter activity
            final Intent intent = new Intent(MainActivity.this, Multimeter.class);
            intent.putExtra(Multimeter.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(Multimeter.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            startActivity(intent);
        }
    };



    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // not using
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    public void scanClick(View v) {
        // make sure bluetooth enabled
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // display only if bluetooth enabled
        if (mBluetoothAdapter.isEnabled()) {
            // Initializes list view adapter.
            mLeDeviceListAdapter.clear();
            scanList.setAdapter(mLeDeviceListAdapter);
            scanList.setVisibility(View.GONE);
            choose.setVisibility(View.GONE);
            voltmeterImage.setVisibility(View.VISIBLE);
            // start scan automatically
            scanLeDevice(true);
        }
    }
}
