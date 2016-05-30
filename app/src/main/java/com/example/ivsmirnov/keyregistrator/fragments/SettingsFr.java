package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Preferences;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.custom_views.EmailAttachPreference;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.services.Alarm;

import java.util.ArrayList;


/**
 * Created by ivsmirnov on 23.03.2016.
 */
public class SettingsFr extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Preference sendMailPreference = findPreference(getResources().getString(R.string.shared_preferences_email_send));
        Preference timeSetPreference = findPreference(getResources().getString(R.string.shared_preferences_sheduler_time));
        Preference dialogGridPreference = findPreference(getResources().getString(R.string.shared_preferences_main_grid_size));
        Preference taskPreference = findPreference(getResources().getString(R.string.shared_preferences_sheduler));
        Preference recipientsPreference = findPreference(getString(R.string.shared_preferences_email_recipients));

        sendMailPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Send_Email(getActivity(), Send_Email.DIALOG_ENABLED).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                return true;
            }
        });

        dialogGridPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogPickers dialogPickers = new DialogPickers();
                dialogPickers.show(((Preferences)getActivity()).getSupportFragmentManager(),"dialog_pickers");
                return false;
            }
        });


        timeSetPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Alarm.setAlarm(Alarm.getClosingTime(newValue.toString()));
                return true;
            }
        });


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


        recipientsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (((ArrayList<String>)newValue).size() == 0){
                    if (isLoggegIn()){
                        setPreferenceEnabled(false, sendMailPreference,
                                getString(R.string.shared_preferences_email_send_disable), getString(R.string.shared_preferences_email_send_disable_summary_norecipients));
                    }
                } else {
                    if (isLoggegIn()){
                        setPreferenceEnabled(true, sendMailPreference, getString(R.string.shared_preferences_email_send), getString(R.string.shared_preferences_email_send_summary));
                    } else {
                        setPreferenceEnabled(false, sendMailPreference,
                                getString(R.string.shared_preferences_email_send_disable), getString(R.string.shared_preferences_email_send_disable_summary_logoff));
                    }
                }
                return true;
            }
        });

        if (Settings.getRecepients().size() == 0){
            setPreferenceEnabled(false, sendMailPreference,
                    getString(R.string.shared_preferences_email_send_disable), getString(R.string.shared_preferences_email_send_disable_summary_norecipients));
        }

        if (Settings.getActiveAccountID().equals(getString(R.string.local_account))){
            setPreferenceEnabled(false, sendMailPreference,
                    getString(R.string.shared_preferences_email_send_disable), getString(R.string.shared_preferences_email_send_disable_summary_logoff));
        }
    }

    private void setPreferenceEnabled(boolean enabled, Preference preference, String title, String summary){
        preference.setEnabled(enabled);
        preference.setTitle(title);
        preference.setSummary(summary);
    }

    private boolean isLoggegIn(){
        return !Settings.getActiveAccountID().equals(getString(R.string.local_account));
    }

}
