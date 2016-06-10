package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;

import java.sql.Connection;
import java.util.ArrayList;

/**
 * Диалог ввода пароля
 */
public class DialogPassword extends DialogFragment implements SQL_Connection.Callback{

    public static final String PERSONS_ACCESS = "persons_access";
    public static final String ROOMS_ACCESS = "rooms_access";
    public static final String ERASE_ACCESS = "erase_access";

    private static final String BUNDLE_RADIO_LABEL = "bundle_radio_label";
    private static final String BUNDLE_ERASE_LIST = "bundle_erase_list";

    private Context mContext;
    private Callback mCallback;
    private String mDialogTag, mPersonTag;
    private ArrayList<String> mEraseItems;

    private SQL_Connection.Callback mSQLCallback;

    public static DialogPassword newInstance (String personRadioLabel, ArrayList<String> eraseItems){
        DialogPassword dialogPassword = new DialogPassword();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE_ERASE_LIST, eraseItems);
        bundle.putString(BUNDLE_RADIO_LABEL, personRadioLabel);
        dialogPassword.setArguments(bundle);
        return dialogPassword;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mDialogTag = getTag();
        mSQLCallback = this;
        Bundle extras = getArguments();
        if (extras!=null) {
            mPersonTag = extras.getString(BUNDLE_RADIO_LABEL);
            mEraseItems = extras.getStringArrayList(BUNDLE_ERASE_LIST);
        }
        if (mDialogTag.equals(PERSONS_ACCESS)) mCallback = (Callback)getParentFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = View.inflate(mContext, R.layout.dialog_password, null);
        final TextInputLayout textInputLayout = (TextInputLayout)dialogView.findViewById(R.id.enter_pass_input_layout);
        final AlertDialog dialogPass = new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setTitle(getString(R.string.enter_password_title))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mCallback!=null) mCallback.onDialogEnterPassDismiss();
                        dialog.dismiss();
                    }
                })
                .create();
        dialogPass.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                dialogPass.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textInputLayout.getEditText().getText().toString().equals("1212")){
                            switch (mDialogTag){
                                case ROOMS_ACCESS:
                                    if (mPersonTag!=null){
                                        new BaseWriter(BaseWriter.UPDATE_CURRENT, (BaseWriter.Callback)getActivity())
                                                .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new BaseWriterParams().setPersonTag(mPersonTag));
                                    }
                                    break;
                                case ERASE_ACCESS:
                                    if (mEraseItems.contains(getString(R.string.title_journal))){
                                        JournalDB.clear();
                                    }
                                    if (mEraseItems.contains(getString(R.string.title_users))){
                                        FavoriteDB.clear();
                                    }
                                    if (mEraseItems.contains(getString(R.string.title_rooms))){
                                        RoomDB.clear();
                                    }

                                    if (mEraseItems.contains(getString(R.string.erase_server_also))){
                                        SQL_Connection.getConnection(null, ServerWriter.DELETE_ALL, mSQLCallback);
                                    }
                                    Snackbar.make(getActivity().getWindow().getCurrentFocus(),"Удалено",Snackbar.LENGTH_SHORT).show();
                                    break;
                                default:
                                    break;
                            }

                            dialog.dismiss();
                        } else {
                            textInputLayout.setError(getString(R.string.enter_password_error));
                        }
                    }
                });
            }
        });
        setCancelable(false);
        return dialogPass;
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        new ServerWriter(ServerWriter.DELETE_ALL, mEraseItems).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
    }

    @Override
    public void onServerConnectException(Exception e) {
//нет подключения к серверу
    }

    public interface Callback{
        void onDialogEnterPassDismiss();
    }
}
