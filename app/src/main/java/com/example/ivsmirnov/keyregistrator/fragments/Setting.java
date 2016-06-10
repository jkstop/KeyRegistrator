package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileLoader;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.example.ivsmirnov.keyregistrator.services.Alarm;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Фрагмент настроек
 */
public class Setting extends PreferenceFragment {

    private static final int REQUEST_CODE_SELECT_BACKUP_LOCATION = 203;
    private static final int REQUEST_CODE_RESTORE = 204;


    private Preference backupLocationPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Preference sendMailPreference = findPreference(getResources().getString(R.string.shared_preferences_email_send));
        Preference timeSetPreference = findPreference(getResources().getString(R.string.shared_preferences_sheduler_time));
        Preference taskPreference = findPreference(getResources().getString(R.string.shared_preferences_sheduler));
        Preference recipientsPreference = findPreference(getString(R.string.shared_preferences_email_recipients));
        Preference backupItemsPreference = findPreference(getString(R.string.shared_preferences_backup_items));
        final Preference backupNowPreference = findPreference(getString(R.string.shared_preferences_backup_now));
        Preference backupRestorePreference = findPreference(getString(R.string.shared_preferences_backup_restore));
        Preference erasePreference = findPreference(getString(R.string.shared_preferences_backup_erase_base));

        backupLocationPreference = findPreference(getString(R.string.shared_preferences_backup_location));
        backupLocationPreference.setSummary(SharedPrefs.getBackupLocation());


        sendMailPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                new Send_Email(getActivity(), Send_Email.DIALOG_ENABLED).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                return true;
            }
        });


        timeSetPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                Alarm.setAlarm(Alarm.getClosingTime(newValue.toString()));
                return true;
            }
        });

        backupLocationPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Intent iLC = new Intent(Intent.ACTION_GET_CONTENT);
                iLC.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                iLC.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                iLC.putExtra(FilePickerActivity.EXTRA_START_PATH, SharedPrefs.getBackupLocation());
                startActivityForResult(iLC,REQUEST_CODE_SELECT_BACKUP_LOCATION);
                return true;
            }
        });

        backupRestorePreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, SharedPrefs.getBackupLocation());
                startActivityForResult(i, REQUEST_CODE_RESTORE);
                return true;
            }
        });

        taskPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (Boolean.parseBoolean(newValue.toString())){
                    Alarm.setAlarm(Alarm.getClosingTime(null));
                } else {
                    Alarm.cancelAlarm();
                }
                return true;
            }
        });

        backupItemsPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (((HashSet)newValue).size() == 0){
                    setPreferenceEnabled(false, backupNowPreference, getString(R.string.shared_preferences_backup_now_disable), getString(R.string.shared_preferences_backup_now_disable_summary));
                } else {
                    setPreferenceEnabled(true, backupNowPreference, getString(R.string.shared_preferences_backup_now), getString(R.string.shared_preferences_backup_now_summary));
                }
                return true;
            }
        });

        erasePreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                new DialogErase().show(((com.example.ivsmirnov.keyregistrator.activities.Preferences)getActivity()).getSupportFragmentManager(),"erase");
                return false;
            }
        });

        backupNowPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                new FileWriter(getActivity(), true).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, SharedPrefs.getBackupItems());
                return true;
            }
        });


        recipientsPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (((ArrayList)newValue).size() == 0){
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

        if (SharedPrefs.getRecepients().size() == 0){
            setPreferenceEnabled(false, sendMailPreference,
                    getString(R.string.shared_preferences_email_send_disable), getString(R.string.shared_preferences_email_send_disable_summary_norecipients));
        }

        if (SharedPrefs.getActiveAccountID().equals(getString(R.string.local_account))){
            setPreferenceEnabled(false, sendMailPreference,
                    getString(R.string.shared_preferences_email_send_disable), getString(R.string.shared_preferences_email_send_disable_summary_logoff));
        }

        if (SharedPrefs.getBackupItems().size() == 0){
            setPreferenceEnabled(false, backupNowPreference, getString(R.string.shared_preferences_backup_now_disable), getString(R.string.shared_preferences_backup_now_disable_summary));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && data!=null){
            String path = data.getData().getPath();
            switch (requestCode){
                case REQUEST_CODE_SELECT_BACKUP_LOCATION:
                    SharedPrefs.setBackupLocation(path);
                    backupLocationPreference.setSummary(path);
                    break;
                case REQUEST_CODE_RESTORE:
                    new FileLoader(getActivity(),path).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    break;
                default:
                    break;
            }
        }
    }

    private void setPreferenceEnabled(boolean enabled, android.preference.Preference preference, String title, String summary){
        preference.setEnabled(enabled);
        preference.setTitle(title);
        preference.setSummary(summary);
    }

    private boolean isLoggegIn(){
        return !SharedPrefs.getActiveAccountID().equals(getString(R.string.local_account));
    }

}
