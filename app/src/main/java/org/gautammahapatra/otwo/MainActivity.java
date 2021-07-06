    package org.gautammahapatra.otwo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Set;

    public class MainActivity extends AppCompatActivity {

    private static final int COARSE_LOCATION_PERMISSION_CODE = 1;
    private BluetoothAdapter bluetoothAdapter;
    private Intent btEnablingIntent;
    private Integer requestCodeEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeEnable = 1;

        TextView labName = findViewById(R.id.lab_name);
        TextView appName = findViewById(R.id.app_name);
        ImageView logo = findViewById(R.id.logo);

        appName.setLetterSpacing(0.25f);
        labName.setLetterSpacing(0.5f);

        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logo.setAnimation(topAnim);
        appName.setAnimation(bottomAnim);
        labName.setAnimation(bottomAnim);

        int bluetoothFlag = allowBluetoothIntentMethod();
        if (1 == bluetoothFlag) {
            requestLocationAccess();
        } else if (0 == bluetoothFlag) {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
            closeApp();
        }
    }

    private void requestLocationAccess() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadDashBoardScreen();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).setTitle("Location Permission").setMessage("Permission needed for scanning nearby bluetooth devices").setPositiveButton("Ok", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE)).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == COARSE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDashBoardScreen();
            } else {
                closeApp();
            }
        }
    }

    private void loadDashBoardScreen() {
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            Set<String> virtual_ids = sharedPref.getStringSet(getString(R.string.pref_virtual_id_key), null);
            Intent intent;
            if (virtual_ids != null && !virtual_ids.isEmpty())
                intent= new Intent(MainActivity.this, Dashboard.class);
            else {
                intent= new Intent(MainActivity.this, Registration.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            finish();
        }, 3000);
    }

    private int allowBluetoothIntentMethod() {
        if (bluetoothAdapter == null) {
            return 0;
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                startActivityForResult(btEnablingIntent, requestCodeEnable);
                return -1;
            } else {
                return 1;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCodeEnable) {
            if (resultCode == RESULT_OK) {
                requestLocationAccess();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth enable cancelled", Toast.LENGTH_LONG).show();
                closeApp();
            }
        }
    }

    private void closeApp() {
        finishAndRemoveTask();
    }
}
