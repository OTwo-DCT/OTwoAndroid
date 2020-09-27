package org.gautammahapatra.otwo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Dashboard extends AppCompatActivity {

    RecyclerView recyclerView;
    List<DashboardDataBinder> dashboardDataBinderList;
    DashboardAdapter adapter;
    private final String SERVICE_UUID = "94595bde-fd10-46c9-bfb0-a4073e62aede";

    private BluetoothAdapter bluetoothAdapter;
    private static final long LE_SCAN_PERIOD_MILLI = 30000;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private final String CHARE_UUID = "3fec3f7f-240c-4ed6-a7a3-b6a7e939794c";
    private final String TAG = "BROADCAST###";
    Button scanButton, broadcastButton;
    private boolean mScanning;
    AdvertiseCallback callback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "BLE advertisement added successfully");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e(TAG, "Failed to add BLE advertisement, reason: " + errorCode);
        }
    };
    BluetoothGattServerCallback gatCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
        }
    };
    private Handler handler;
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    int signalStrength = result.getRssi();
                    String deviceName = getMajorMinorString(Objects.requireNonNull(result.getScanRecord()).getBytes());
                    DashboardDataBinder binder = new DashboardDataBinder(signalStrength, deviceName, "BLE");
                    AddCard(binder);
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private void RemoveCard(AtomicInteger index) {
        dashboardDataBinderList.remove(index.get());
        adapter.notifyItemRemoved(index.get());
    }

    private void AddCard(DashboardDataBinder dashboardDataBinder) {
        AtomicInteger index = new AtomicInteger();
        AtomicBoolean exists = new AtomicBoolean(false);
        if (dashboardDataBinder.getDeviceName() == null)
            dashboardDataBinder.setDeviceName("Unknown");
        dashboardDataBinderList.forEach((device) -> {
            if (device.getDeviceName().equals(dashboardDataBinder.getDeviceName())) {
                exists.set(true);
                index.set(dashboardDataBinderList.indexOf(device));
            }
        });
        if (!exists.get()) {
            index.set(dashboardDataBinderList.size());
        } else {
            RemoveCard(index);
        }
        dashboardDataBinderList.add(index.get(), dashboardDataBinder);
        adapter.notifyItemInserted(index.get());
        Log.d("Dashboard", dashboardDataBinder.toString());
    }

    private void scanLeDevice() {
        if (!mScanning) {
            handler.postDelayed(() -> {
                mScanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);
                Log.d("Dashboard", "Stopping");
                Toast.makeText(getApplicationContext(), "Stopping", Toast.LENGTH_LONG).show();
                scanButton.setEnabled(true);
            }, LE_SCAN_PERIOD_MILLI);

            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            Log.d("Dashboard", "Scanning");
            Toast.makeText(getApplicationContext(), "Scanning", Toast.LENGTH_LONG).show();
            scanButton.setEnabled(false);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        handler = new Handler();
        assert bluetoothManager != null;
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanButton = findViewById(R.id.scan_btn);
        broadcastButton = findViewById(R.id.broadcast_btn);
        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBroadcast();
            }
        });
        mScanning = false;
        scanButton.setOnClickListener(v -> {
            scanLeDevice();
            Log.d("Dashboard", "Start LE Discovery");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void startBroadcast() {
        broadcastButton.setEnabled(false);
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData advertiseData = setAdvertiseData();

        AdvertiseData scanResponseData = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)))
                .setIncludeTxPowerLevel(true)
                .build();

        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);

        BluetoothGattServer bluetoothGattServer = bluetoothManager.openGattServer(this, gatCallback);
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID.fromString(CHARE_UUID), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);

        service.addCharacteristic(characteristic);

        bluetoothGattServer.addService(service);

    }

    protected AdvertiseData setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = asBytes(UUID.fromString(SERVICE_UUID));
        mManufacturerData.put(0, (byte) 0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte) 0xAC); // Beacon Identifier
        for (int i = 2; i <= 17; i++) {
            mManufacturerData.put(i, uuid[i - 2]); // adding the UUID
        }
        SecureRandom random = new SecureRandom();
        byte[] majorminor = new byte[4];
        random.nextBytes(majorminor);
        mManufacturerData.put(18, majorminor[0]); // first byte of Major
        mManufacturerData.put(19, majorminor[1]); // second byte of Major
        mManufacturerData.put(20, majorminor[2]); // first minor
        mManufacturerData.put(21, majorminor[3]); // second minor
        Toast.makeText(getApplicationContext(), hex(majorminor), Toast.LENGTH_LONG).show();
        mManufacturerData.put(22, (byte) 0xB5); // txPower
        mBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID
        mBuilder.setIncludeDeviceName(false);
        mBuilder.setIncludeTxPowerLevel(false);
        return mBuilder.build();
    }

    private String getMajorMinorString(byte[] scanRecordBytes) {
        byte[] record = {scanRecordBytes[25], scanRecordBytes[26], scanRecordBytes[27], scanRecordBytes[28]};
        return hex(record);
    }
}
