package org.gautammahapatra.digitalcontacttracing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dashboard extends AppCompatActivity {

    RecyclerView recyclerView;
    List<DashboardDataBinder> dashboardDataBinderList;
    DashboardAdapter adapter;
    Button scanButton;

    private BluetoothAdapter bluetoothAdapter;
    private static final long SCAN_PERIOD = 30000;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean mScanning;
    private Handler handler = new Handler();
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    int signalStrength = result.getRssi();
                    String deviceName = device.getName();
                    DashboardDataBinder binder = new DashboardDataBinder(signalStrength, deviceName);
                    AddCard(binder);
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        assert bluetoothManager != null;
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanButton = findViewById(R.id.scan_btn);

        mScanning = false;
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice();
                Log.d("Dashboard", "Start Discovery");
            }
        });

        try {
            dashboardDataBinderList = new ArrayList<>();
            adapter = new DashboardAdapter(this, dashboardDataBinderList);
            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Dashboard", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void RemoveCard(int index) {
        dashboardDataBinderList.remove(index);
        adapter.notifyItemRemoved(index);
    }

    private void AddCard(DashboardDataBinder dashboardDataBinder) {
        int index = dashboardDataBinderList.size();
        dashboardDataBinderList.add(index, dashboardDataBinder);
        adapter.notifyItemInserted(index);
        Log.d("Dashboard", dashboardDataBinder.toString());
    }

    private void scanLeDevice() {
        if (!mScanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.d("Dashboard", "Stopping");
                    Toast.makeText(getApplicationContext(), "Bluetooth Stopping", Toast.LENGTH_LONG).show();
                    scanButton.setEnabled(true);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            Log.d("Dashboard", "Scanning");
            Toast.makeText(getApplicationContext(), "Bluetooth Scanning", Toast.LENGTH_LONG).show();
            scanButton.setEnabled(false);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }
}
