package com.example.myapplication.utils;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ColorListPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static ColorListPreferenceDialogFragmentCompat newInstance(String key) {
        final ColorListPreferenceDialogFragmentCompat fragment = new ColorListPreferenceDialogFragmentCompat();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        ColorListPreference preference = (ColorListPreference) getPreference();

        CharSequence[] csEntries = preference.getEntries();
        String[] entries = new String[csEntries.length];
        for (int i = 0; i < csEntries.length; i++) {
            entries[i] = csEntries[i].toString();
        }

        CharSequence[] csEntryValues = preference.getEntryValues();
        String[] entryValues = new String[csEntryValues.length];
        for (int i = 0; i < csEntryValues.length; i++) {
            entryValues[i] = csEntryValues[i].toString();
        }

        int selectedIndex = preference.findIndexOfValue(preference.getValue());
        ColorAdapter adapter = new ColorAdapter(requireContext(), entries, entryValues, selectedIndex);

        builder.setSingleChoiceItems(adapter, selectedIndex, (dialog, which) -> {
            String value = entryValues[which];
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
            dialog.dismiss();
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Obavezno overrideat, trenutno nema svrhe
    }
}
