package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ivsmirnov on 28.01.2016.
 */
public class Get_Account_Information extends AsyncTask<Void,AccountItem,AccountItem>{

    private final static String G_PLUS_SCOPE =
            "oauth2:https://www.googleapis.com/auth/plus.me";
    private final static String USERINFO_SCOPE =
            "https://www.googleapis.com/auth/userinfo.profile";
    private final static String EMAIL_SCOPE =
            "https://www.googleapis.com/auth/userinfo.email";
    private final static String SCOPES = G_PLUS_SCOPE + " " + USERINFO_SCOPE + " " + EMAIL_SCOPE;

    private Context mContext;
    private String mAccountName;
    private Get_Account_Information_Interface mListener;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public Get_Account_Information (Context context, String accountName, Get_Account_Information_Interface get_account_information_interface){
        this.mContext = context;
        this.mAccountName = accountName;
        this.mListener = get_account_information_interface;
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected AccountItem doInBackground(Void... params) {
        AccountItem accountItem = null;
        try {

            HttpURLConnection urlConnection = (HttpURLConnection) new URL("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="
                    + GoogleAuthUtil.getToken(mContext,mAccountName,SCOPES))
                    .openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            accountItem = new AccountItem().setLastname(jsonObject.getString("given_name"))
                    .setFirstname( jsonObject.getString("family_name"))
                    .setEmail(jsonObject.getString("email"))
                    .setPhoto(jsonObject.getString("picture"))
                    .setAccountID(GoogleAuthUtil.getAccountId(mContext,mAccountName));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } catch (UserRecoverableAuthException e) {
           mListener.onUserRecoverableAuthException(e);
        } catch (GoogleAuthException e) {
            e.printStackTrace();
        }
        return accountItem;
    }

    @Override
    protected void onPostExecute(AccountItem accountItem) {
        super.onPostExecute(accountItem);
        if (accountItem!=null){
            DataBaseAccount dbAccount = new DataBaseAccount(mContext);
            dbAccount.writeAccount(accountItem);
            dbAccount.closeDB();

            mSharedPreferencesEditor.putString(Values.ACTIVE_ACCOUNT_ID, accountItem.getAccountID());
            mSharedPreferencesEditor.apply();
            mListener.onChangeAccount();
        }
    }



}
