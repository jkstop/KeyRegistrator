package com.example.ivsmirnov.keyregistrator.others;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by ivsmirnov on 16.02.2016.
 */
public class GoogleAuthenticator extends Launcher{

    public static final int SAVE_ACCOUNT = 0;
    public static final int GET_TOKEN = 1;

    private int mType;
    private Context mContext;
    private Settings mSettings;

    private GoogleApiClient mGoogleApiClient;

    public GoogleAuthenticator(Context context, FragmentActivity fragmentActivity, int type, GoogleApiClient googleApiClient){
        this.mContext = context;
        this.mType = type;
        mSettings = new Settings(mContext);
        mGoogleApiClient = googleApiClient;
        //mGoogleApiClient = initGoogleAPI(mContext, fragmentActivity);
        //Log.d("apiClient",String.valueOf(mGoogleApiClient));
    }

    public GoogleApiClient initGoogleAPI(Context context, FragmentActivity fragmentActivity){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        return new GoogleApiClient.Builder(context)
                .enableAutoManage(fragmentActivity,
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                            }
                        })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void LogOn(){
        Log.d("api",String.valueOf(mGoogleApiClient.isConnected()));
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.d("sign",String.valueOf(signInIntent.getData()!=null));
        startActivityForResult(signInIntent, Values.REQUEST_CODE_LOG_ON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == Values.REQUEST_CODE_LOG_ON){
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                if (result.isSuccess()) {
                    final GoogleSignInAccount acct = result.getSignInAccount();
                    if (mType == SAVE_ACCOUNT){
                        AccountItem accountItem = new AccountItem().setLastname(acct.getDisplayName())
                                .setEmail(acct.getEmail())
                                .setPhoto(acct.getPhotoUrl().toString())
                                .setAccountID(acct.getId());
                        DataBaseAccount dbAccount = new DataBaseAccount(mContext);
                        dbAccount.writeAccount(accountItem);
                        dbAccount.closeDB();
                    } else if (mType == GET_TOKEN){
                        try {
                            mSettings.setToken(GoogleAuthUtil.getToken(mContext,acct.getEmail(),"oauth2:https://mail.google.com/"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                        }
                    }


                   /* Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                String token = GoogleAuthUtil.getToken(mContext,acct.getEmail(),"oauth2:https://mail.google.com/");
                                Log.d("token",String.valueOf(token)); //TOKEN!!!
                                Properties props = new Properties();
                                props.put("mail.smtp.ssl.enable", "true"); // required for Gmail
                                props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
                                Session session = Session.getInstance(props);
                                session.setDebug(true);
                                //Store store = session.getStore("imap");
                                //store.connect("imap.gmail.com",acct.getEmail(),token);
                                //Log.d("connect",String.valueOf(store.isConnected()));///////wooooooooork!!!

                                MimeMessage mimeMessage = new MimeMessage(session);
                                mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ivsmirnov@fa.ru"));
                                mimeMessage.setText("dfkjdkjfjkdfbg");

                                Transport transport = session.getTransport("smtp");
                                transport.connect("imap.gmail.com",acct.getEmail(),token);
                                transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
                                transport.close();

                            } catch (UserRecoverableAuthException e) {
                                startActivityForResult(e.getIntent(),222);
                            } catch (GoogleAuthException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NoSuchProviderException e) {
                                e.printStackTrace();
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
*/

                   // mSettings.setActiveAccountID(acct.getId());
                   // mSettings.setAuthToken(acct.getIdToken());
                    //initNavigationDrawer(getMainNavigationItems());
                } else {
                    //Toast.makeText(mContext,"Не удалось подключиться", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
