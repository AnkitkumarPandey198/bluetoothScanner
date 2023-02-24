package com.example.bluetoothscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;

    public static final int REQUEST_BLUETOOTH_SCAN = 111;

    public static final int REQUEST_BLUETOOTH_CONNECT = 1111;
    private Button scanningBtn;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ListView deviceLists = findViewById(R.id.listView);
        scanningBtn = findViewById(R.id.buttonAvailable);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceLists.setAdapter(listAdapter);

        checkBluetoothState();


        scanningBtn.setOnClickListener(v -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                if (checkCoarseLocationPermission()) {
                    listAdapter.clear();
                    bluetoothAdapter.startDiscovery();

                }
            } else {
                checkBluetoothState();
            }
        });

        checkCoarseLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(devicesFoundReceiver);
    }

    public boolean checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        }
        return true;
    }

    public void checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on your Device", Toast.LENGTH_LONG).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_SCAN);
                    }
                    return;
                }
                if (bluetoothAdapter.isDiscovering()) {
                    Toast.makeText(this, "Device Discovering Process....", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Bluetooth is enabled!", Toast.LENGTH_LONG).show();
                    scanningBtn.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "You need to enable Bluetooth", Toast.LENGTH_LONG).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            checkBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access Coarse Location allowed.You can scan Bluetooth Devices ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Access Coarse Location forbidden.You can't scan Bluetooth Devices", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_BLUETOOTH_SCAN:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You can scan Bluetooth Devices ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "You can't scan Bluetooth Devices", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_BLUETOOTH_CONNECT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You can Connect Bluetooth Devices ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "You can't Connect Bluetooth Devices", Toast.LENGTH_LONG).show();
                }
        }

    }

    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT);
                    }
                    return;
                }
                listAdapter.add(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanningBtn.setText("Scanning Bluetooth Devices");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scanningBtn.setText("Scanning in Progress....");
            }
        }

    };
}
