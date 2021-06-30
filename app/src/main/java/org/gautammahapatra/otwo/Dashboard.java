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
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
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
    BluetoothGattServer bluetoothGattServer;
    BluetoothGattService service;
    private final String TAG = "BROADCAST###";
    Button scanButton, broadcastButton, vidButton;
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
    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
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
        Log.d(TAG, dashboardDataBinder.toString());
    }

    private void scanLeDevice() {
        if (!mScanning) {
            handler.postDelayed(() -> {
                mScanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);
                Log.d(TAG, "Stopping");
                Toast.makeText(getApplicationContext(), "Stopping", Toast.LENGTH_LONG).show();
                scanButton.setEnabled(true);
            }, LE_SCAN_PERIOD_MILLI);

            mScanning = true;
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
            scanFilterBuilder.setServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)));
            filters.add(scanFilterBuilder.build());
            ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
            scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
            ScanSettings settings = scanSettingsBuilder.build();
            bluetoothLeScanner.startScan(filters, settings, leScanCallback);
            Log.d(TAG, "Scanning");
            Toast.makeText(getApplicationContext(), "Scanning", Toast.LENGTH_LONG).show();
            scanButton.setEnabled(false);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s.length() % 2 == 0) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        handler = new Handler();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) throw new AssertionError("bluetoothManager cannot be null");
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanButton = findViewById(R.id.scan_btn);
        broadcastButton = findViewById(R.id.broadcast_btn);
        vidButton = findViewById(R.id.vid_btn);
        broadcastButton.setOnClickListener(view -> startBroadcast());
        mScanning = false;
        scanButton.setOnClickListener(v -> {
            scanLeDevice();
            Log.d(TAG, "Start LE Discovery");
        });
        try {
            dashboardDataBinderList = new ArrayList<>();
            adapter = new DashboardAdapter(this, dashboardDataBinderList);
            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
        vidButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            String phoneNumber = sharedPref.getString(getString(R.string.phone_pref_key), null);
            JSONObject data = new JSONObject();
            try {
                data.put("phone_number", phoneNumber);
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = getString(R.string.api_url);
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, data, response -> {
                    if (response.has("data")) {
                        try {
                            if (response.getJSONObject("data").has("vids")) {
                                JSONArray vids = response.getJSONObject("data").getJSONArray("vids");
                                Set<String> vids_pref = new HashSet<>();
                                for (int i = 0; i < vids.length(); i++) {
                                    vids_pref.add(vids.getString(i));
                                }
                                editor.putStringSet(getString(R.string.pref_vids_key), vids_pref);
                                editor.apply();
                                stopBroadcast();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error getting virtual ID", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error getting virtual ID", Toast.LENGTH_LONG).show();
                        }
                    }
                }, error -> Toast.makeText(getApplicationContext(), "Error getting virtual ID", Toast.LENGTH_LONG).show());
                queue.add(req);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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

        bluetoothGattServer = bluetoothManager.openGattServer(this, gatCallback);
        service = new BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        String CHAR_UUID = "3fec3f7f-240c-4ed6-a7a3-b6a7e939794c";
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID.fromString(CHAR_UUID), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);

        service.addCharacteristic(characteristic);

        bluetoothGattServer.addService(service);

    }

    private void stopBroadcast() {
        broadcastButton.setEnabled(true);
        bluetoothGattServer = bluetoothManager.openGattServer(this, gatCallback);
        if (service != null)
            bluetoothGattServer.removeService(service);
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
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Set<String> virtual_ids = sharedPref.getStringSet(getString(R.string.pref_vids_key), null);
        if (virtual_ids != null) {
            Log.d(TAG, "VID: " + virtual_ids.toArray(new String[0])[new Random().nextInt(virtual_ids.size())]);
            byte[] vid = hexStringToByteArray(virtual_ids.toArray(new String[0])[new Random().nextInt(virtual_ids.size())]);
            if (vid != null) {
                mManufacturerData.put(18, vid[0]); // first byte of Major
                mManufacturerData.put(19, vid[1]); // second byte of Major
                mManufacturerData.put(20, vid[2]); // first minor
                mManufacturerData.put(21, vid[3]); // second minor
                Toast.makeText(getApplicationContext(), byteArrayToHexString(vid), Toast.LENGTH_LONG).show();
            }
            mManufacturerData.put(22, (byte) 0xB5); // txPower
            mBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID
            mBuilder.setIncludeDeviceName(false);
            mBuilder.setIncludeTxPowerLevel(false);
        }
        return mBuilder.build();
    }

    private String getMajorMinorString(byte[] scanRecordBytes) {
        byte[] record = {scanRecordBytes[25], scanRecordBytes[26], scanRecordBytes[27], scanRecordBytes[28]};
        return byteArrayToHexString(record);
    }
}
