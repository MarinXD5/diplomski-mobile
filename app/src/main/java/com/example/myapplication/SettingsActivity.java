package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.myapplication.utils.ColorListPreference;
import com.example.myapplication.utils.ColorListPreferenceDialogFragmentCompat;

public class SettingsActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private ActivityResultLauncher<Intent> directoryPickerLauncher;

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
    }
}
