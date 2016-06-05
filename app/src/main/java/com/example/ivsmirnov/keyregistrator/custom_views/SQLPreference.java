package com.example.ivsmirnov.keyregistrator.custom_views;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;

/**
 * Created by ivsmirnov on 24.05.2016.
 */
public class SQLPreference extends DialogPreference implements SQL_Connection.Callback {

    private static final int HANDLER_CONNECTED = 100;
    private static final int HANDLER_DISCONNECTED = 200;

    private Context mContext;
    private SQL_Connection.Callback mCallback;
    private TextInputLayout mInputServerName;
    private EditText mServerName;
    private ImageView mServerStatus,mCheckServerConnect;
    private Handler mHandler;
    private String connectError;
    public SQLPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mCallback = this;

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HANDLER_CONNECTED:
                        mCheckServerConnect.clearAnimation();
                        mInputServerName.setErrorEnabled(false);
                        mServerStatus.setImageResource(R.drawable.ic_cloud_done_black_48dp);
                        break;
                    case HANDLER_DISCONNECTED:
                        mCheckServerConnect.clearAnimation();
                        mServerStatus.setImageResource(R.drawable.ic_cloud_off_black_48dp);
                        mInputServerName.setErrorEnabled(true);
                        mInputServerName.setError(connectError);
                        break;
                    default:
                        break;
                }
            }
        };

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        View dialogLayout = View.inflate(mContext, R.layout.view_preference_sql, null);

        //анимация поворота стрелки
        final Animation rotationAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
        rotationAnim.setRepeatCount(Animation.INFINITE);

        mInputServerName = (TextInputLayout)dialogLayout.findViewById(R.id.preference_item_sql_input_server);
        mServerName = mInputServerName.getEditText();
        mCheckServerConnect = (ImageView)dialogLayout.findViewById(R.id.preference_item_sql_check_connect);
        mServerStatus = (ImageView)dialogLayout.findViewById(R.id.preference_item_sql_status);

        mServerName.setText(Settings.getServerName());

        mCheckServerConnect.startAnimation(rotationAnim);

        connect(mServerName.getText().toString(), mCallback);

        mCheckServerConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputServerName.getEditText().getText().length() == 0){
                    mInputServerName.setErrorEnabled(true);
                    mInputServerName.setError(App.getAppContext().getString(R.string.input_sql_server_name_error));
                } else {
                    mCheckServerConnect.startAnimation(rotationAnim);
                    connect(mServerName.getText().toString(), mCallback);
                }
            }
        });

        //если android по 4.4 включительно, то включаем программное ускорение
        //иначе анимация не работает
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            mCheckServerConnect.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        return dialogLayout;
    }

    private void connect (String serverName, SQL_Connection.Callback callback){
        SQL_Connection.getConnection(serverName, 0, callback);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                Settings.setServerName(mServerName.getText().toString());
                connect(mServerName.getText().toString(), null);
                //SQL_Connection.getConnection(mServerName.getText().toString(), null);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            default:
                super.onClick(dialog,which);
        }
    }


    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        mHandler.sendEmptyMessage(HANDLER_CONNECTED);
    }

    @Override
    public void onServerConnectException(Exception e) {
        connectError = e.getLocalizedMessage();
        mHandler.sendEmptyMessage(HANDLER_DISCONNECTED);

    }
}
