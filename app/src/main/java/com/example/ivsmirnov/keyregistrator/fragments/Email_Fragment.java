package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by IVSmirnov on 03.09.2015.
 */
public class Email_Fragment extends Fragment {

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;


    public static Email_Fragment newInstance(){
        return new Email_Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_dialog_email_settings,container,false);
       /* mContext = rootView.getContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        String emailText = mSharedPreferences.getString(Values.EMAIL, "");
        String passwordText = mSharedPreferences.getString(Values.PASSWORD, "");
        String recipientsText = mSharedPreferences.getString(Values.RECIPIENTS, "");
        String bodyText = mSharedPreferences.getString(Values.BODY, "");
        String themeText = mSharedPreferences.getString(Values.THEME, "");

        final EditText email = (EditText) rootView.findViewById(R.id.email_settings_edit_email);
        final EditText pass = (EditText) rootView.findViewById(R.id.email_settings_edit_password);
        final EditText recipient = (EditText) rootView.findViewById(R.id.email_settings_edit_recipients);
        final EditText body = (EditText) rootView.findViewById(R.id.email_settings_edit_body);
        final EditText theme = (EditText) rootView.findViewById(R.id.email_settings_edit_email_theme);
        final CheckBox checkJournal = (CheckBox)rootView.findViewById(R.id.email_settings_mail_content_checkJournal);
        final CheckBox checkTeachers = (CheckBox)rootView.findViewById(R.id.email_settings_mail_content_checkTeachers);
        final Button sendNowButton = (Button)rootView.findViewById(R.id.email_settings_button_send);
        final Button saveButton = (Button)rootView.findViewById(R.id.email_settings_button_save);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.email_settings_button_save:
                        mSharedPreferencesEditor.putString(Values.EMAIL, email.getText().toString());
                        mSharedPreferencesEditor.putString(Values.PASSWORD, pass.getText().toString());
                        mSharedPreferencesEditor.putString(Values.RECIPIENTS, recipient.getText().toString());
                        mSharedPreferencesEditor.putString(Values.BODY, body.getText().toString());
                        mSharedPreferencesEditor.putString(Values.THEME, theme.getText().toString());
                        mSharedPreferencesEditor.putBoolean(Values.CHECK_JOURNAL, checkJournal.isChecked());
                        mSharedPreferencesEditor.putBoolean(Values.CHECK_TEACHERS, checkTeachers.isChecked());
                        mSharedPreferencesEditor.commit();
                        Toast.makeText(mContext,"Сохранено",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.email_settings_button_send:
                        mSharedPreferencesEditor.putString(Values.EMAIL, email.getText().toString());
                        mSharedPreferencesEditor.putString(Values.PASSWORD, pass.getText().toString());
                        mSharedPreferencesEditor.putString(Values.RECIPIENTS, recipient.getText().toString());
                        mSharedPreferencesEditor.putString(Values.BODY, body.getText().toString());
                        mSharedPreferencesEditor.putString(Values.THEME, theme.getText().toString());
                        mSharedPreferencesEditor.putBoolean(Values.CHECK_JOURNAL, checkJournal.isChecked());
                        mSharedPreferencesEditor.putBoolean(Values.CHECK_TEACHERS, checkTeachers.isChecked());
                        mSharedPreferencesEditor.commit();
                        Send_Email send_email = new Send_Email(mContext,new String[]{email.getText().toString() + "@gmail.com",
                                pass.getText().toString(), recipient.getText().toString(), body.getText().toString(), theme.getText().toString()});
                        send_email.execute();
                        Toast.makeText(mContext,"Отправка...",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };

        sendNowButton.setOnClickListener(onClickListener);
        saveButton.setOnClickListener(onClickListener);

        email.setText(emailText);
        pass.setText(passwordText);
        recipient.setText(recipientsText);
        body.setText(bodyText);
        theme.setText(themeText);
        checkJournal.setChecked(mSharedPreferences.getBoolean(Values.CHECK_JOURNAL, false));
        checkTeachers.setChecked(mSharedPreferences.getBoolean(Values.CHECK_TEACHERS,false));*/
        return rootView;
    }
}
