package com.example.ivsmirnov.keyregistrator.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.ivsmirnov.keyregistrator.R;

/**
 * Created by ivsmirnov on 23.03.2016.
 */
public class SettingsFr extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
