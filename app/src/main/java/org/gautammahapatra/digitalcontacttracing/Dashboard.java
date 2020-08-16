package org.gautammahapatra.digitalcontacttracing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dashboard extends AppCompatActivity
{

    RecyclerView recyclerView;
    List<DashboardDataBinder> dashboardDataBinderList;
    DashboardAdapter adapter;
    Button scanButton;
    private boolean mScanning;
    private Handler handler = new Handler();

    private BluetoothAdapter bluetoothAdapter;

    private static final long SCAN_PERIOD = 10000;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        scanButton = findViewById(R.id.scan_btn);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Dashboard.this));

        scanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                scanBle();
            }
        });

        InflateRecyclerView();
        statusCheck();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
    }

    //Location Service Checking
    public boolean statusCheck()
    {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        assert manager != null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            displayLocationSettingsRequest(this);
            return false;
        }

        else
        {
            return true;
        }
    }

    //Dialog to Open Location Settings
    private void displayLocationSettingsRequest(Context context)
    {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        @SuppressWarnings("deprecation")
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(LocationSettingsResult result)
            {
                final Status status = result.getStatus();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("Location", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d("Location", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try
                        {
                            status.startResolutionForResult(Dashboard.this, REQUEST_CHECK_SETTINGS);
                        }

                        catch (IntentSender.SendIntentException e)
                        {
                            Log.d("Location", "PendingIntent unable to execute request.");
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("Location", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private void scanBle()
    {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null)
        {
            Log.d("REC_VE", "Null BLE");
            return;
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder().setDeviceName("SenFlexT").build();
        filters.add(filter);
        ScanSettings setting = new ScanSettings.Builder().build();

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mScanning = false;
                bluetoothAdapter.getBluetoothLeScanner().stopScan(BleScanCallback);
                Log.d("REC_VE", "Scan Stopped");
            }
        }, SCAN_PERIOD);

        mScanning = true;
        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, setting, BleScanCallback);
        Log.d("REC_VE", "Scan Start");
    }

    private ScanCallback BleScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, final ScanResult result)
        {
            super.onScanResult(callbackType, result);

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.d("REC_VE_BLE_DEV", "name: " + result.getDevice().getName().toString());
                    Log.d("REC_VE_BLE_DEV", "rssi: " + result.getRssi());
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            Log.d("REC_VE", "Scan Batch");
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.d("REC_VE", "Scan Failed: " + String.valueOf(errorCode));
            super.onScanFailed(errorCode);
        }
    };

    private void InflateRecyclerView()
    {
        try
        {
            dashboardDataBinderList = new ArrayList<>();
            dashboardDataBinderList.add(new DashboardDataBinder("1.0", "A", "Sen"));
            dashboardDataBinderList.add(new DashboardDataBinder("1.0", "A", "Sen"));
            DashboardAdapter adapter = new DashboardAdapter(Dashboard.this, dashboardDataBinderList);
            recyclerView.setAdapter(adapter);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("REC_VE", e.toString());
        }

    }

    private void AddCard(int index, DashboardDataBinder dashboardDataBinder)
    {
        dashboardDataBinderList.add(index, dashboardDataBinder);
        adapter.notifyItemInserted(index);
    }

    private void RemoveCard(int index)
    {
        dashboardDataBinderList.remove(index);
        adapter.notifyItemRemoved(index);
    }
}
