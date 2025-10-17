package com.example.gesturaapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static BluetoothSocket bluetoothSocket;
    public static BluetoothAdapter bluetoothAdapter;
    public static InputStream inputStream;
    public static boolean isConnected = false;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
    public static final String DEVICE_NAME = "GloveA"; // <-- Replace with your Bluetooth device name

    BottomNavigationView bottomNavigation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Load the default fragment when app opens
        loadFragment(new LearnFragment());

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_learn) {
                selectedFragment = new LearnFragment();
            } else if (item.getItemId() == R.id.nav_translate) {
                selectedFragment = new TranslateFragment();
            } else if (item.getItemId() == R.id.nav_assessment) {
                selectedFragment = new AssessmentSelectionFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public static void connectBluetoothDevice(Context context, Runnable onSuccess, Runnable onFailure) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            onFailure.run();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;
        for (BluetoothDevice d : pairedDevices) {
            if (d.getName().equals(DEVICE_NAME)) {
                device = d;
                break;
            }
        }

        if (device == null) {
            Toast.makeText(context, "Device " + DEVICE_NAME + " not found", Toast.LENGTH_SHORT).show();
            onFailure.run();
            return;
        }

        BluetoothDevice finalDevice = device;
        new Thread(() -> {
            try {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    bluetoothSocket.close();
                }
                bluetoothSocket = finalDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();

                isConnected = true;
                ((AppCompatActivity) context).runOnUiThread(onSuccess);
            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;
                ((AppCompatActivity) context).runOnUiThread(onFailure);
            }
        }).start();
    }

    public static void disconnectBluetoothDevice(Context context, Runnable onDisconnected) {
        new Thread(() -> {
            try {
                if (inputStream != null) inputStream.close();
                if (bluetoothSocket != null) bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnected = false;
            ((AppCompatActivity) context).runOnUiThread(onDisconnected);
        }).start();
    }
}
