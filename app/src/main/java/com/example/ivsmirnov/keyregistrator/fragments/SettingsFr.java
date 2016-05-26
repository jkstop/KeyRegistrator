package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Preferences;
import com.example.ivsmirnov.keyregistrator.services.Alarm;


/**
 * Created by ivsmirnov on 23.03.2016.
 */
public class SettingsFr extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference dialogGridPreference = findPreference(getResources().getString(R.string.shared_preferences_main_grid_size));
        dialogGridPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogPickers dialogPickers = new DialogPickers();
                dialogPickers.show(((Preferences)getActivity()).getSupportFragmentManager(),"dialog_pickers");
                return false;
            }
        });

        Preference timeSetPreference = findPreference(getResources().getString(R.string.shared_preferences_sheduler_time));
        timeSetPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Alarm.setAlarm(Alarm.getClosingTime(newValue.toString()));
                return true;
            }
        });

        Preference taskPreference = findPreference(getResources().getString(R.string.shared_preferences_sheduler));
        taskPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Boolean.parseBoolean(newValue.toString())){
                    Alarm.setAlarm(Alarm.getClosingTime(null));
                } else {
                    Alarm.cancelAlarm();
                }
                return true;
            }
        });

    }



}
