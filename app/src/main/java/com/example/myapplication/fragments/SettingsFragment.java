package com.example.myapplication.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.myapplication.R;
import com.example.myapplication.utils.ColorListPreference;
import com.example.myapplication.utils.ColorListPreferenceDialogFragmentCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SettingsFragment extends PreferenceFragmentCompat {

    private ActivityResultLauncher<Intent> directoryPickerLauncher;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directoryPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION &
                                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            requireContext().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                            prefs.edit().putString("pref_save_location", treeUri.toString()).apply();

                            Preference saveLocationPref = findPreference("pref_save_location");
                            if (saveLocationPref != null) {
                                saveLocationPref.setSummary(treeUri.toString());
                            }
                        }
                    }
                });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey);

        ListPreference themePreference = findPreference("pref_theme");
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String themeValue = (String) newValue;
                switch (themeValue) {
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    default:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
                if (getActivity() != null) {
                    getActivity().recreate();
                }
                return true;
            });
        }

        Preference saveLocationPref = findPreference("pref_save_location");
        if (saveLocationPref != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String savedUri = prefs.getString("pref_save_location", null);
            if (savedUri != null) {
                saveLocationPref.setSummary(savedUri);
            }

            saveLocationPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                directoryPickerLauncher.launch(intent);
                return true;
            });
        }

        Preference wifiPref = findPreference("pref_wifi_connection");
        if (wifiPref != null) {
            wifiPref.setOnPreferenceClickListener(preference -> {
                checkPermissionsAndScanWiFi();
                return true;
            });
        }
    }

    private void checkPermissionsAndScanWiFi() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            scanWiFiNetworks();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanWiFiNetworks();
            } else {
                Toast.makeText(requireContext(), "Permission denied! Cannot scan WiFi networks.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof ColorListPreference) {
            ColorListPreferenceDialogFragmentCompat dialogFragment =
                    ColorListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void scanWiFiNetworks() {
        WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(requireContext(), "WiFi is disabled, enabling...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        requireContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Set<ScanResult> results = new HashSet<>(wifiManager.getScanResults());
                results.removeIf(r -> Objects.equals(r.SSID, "") || Objects.equals(r.SSID, " "));
                showWiFiSelectionDialog(results);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
    }

    private void showWiFiSelectionDialog(Set<ScanResult> scanResults) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select a WiFi Network");

        List<String> wifiNames = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            wifiNames.add(scanResult.SSID);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, wifiNames);
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedSSID = wifiNames.get(which);
            showPasswordInputDialog(selectedSSID);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showPasswordInputDialog(String ssid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password for " + ssid);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Connect", (dialog, which) -> {
            String password = input.getText().toString().trim();
            if (!password.isEmpty()) {
                sendWiFiCredentialsToRaspberry(ssid, password);
            } else {
                Toast.makeText(requireContext(), "Password cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendWiFiCredentialsToRaspberry(String ssid, String password) {
        String wifiData = "SSID:" + ssid + ";PASSWORD:" + password;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Bluetooth is not enabled!", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice raspberryPiDevice = getPairedDevice("raspberrypi");

        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            BluetoothSocket socket = raspberryPiDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            socket.connect();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(wifiData.getBytes());
            outputStream.flush();
            socket.close();
            Toast.makeText(requireContext(), "WiFi credentials sent!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to send data", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothDevice getPairedDevice(String deviceName) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Bluetooth permissions are not granted!", Toast.LENGTH_SHORT).show();
            return null;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.isEmpty()) {
            Toast.makeText(requireContext(), "No paired Bluetooth devices found!", Toast.LENGTH_SHORT).show();
            return null;
        }

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName() != null && device.getName().equalsIgnoreCase(deviceName)) {
                return device;
            }
        }

        Toast.makeText(requireContext(), "Raspberry Pi is not paired!", Toast.LENGTH_SHORT).show();
        return null;
    }
}
