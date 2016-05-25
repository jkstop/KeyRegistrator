package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ivsmirnov on 23.03.2016.
 */
public class SettingsFr extends PreferenceFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
