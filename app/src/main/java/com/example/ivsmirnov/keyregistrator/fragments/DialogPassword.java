package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;

/**
 * Диалог ввода пароля
 */
public class DialogPassword extends DialogFragment {

    public static final String PERSONS_ACCESS = "persons_access";
    public static final String ROOMS_ACCESS = "rooms_access";

    private static final String BUNDLE_RADIO_LABEL = "bundle_radio_label";

    private Context mContext;
    private Callback mCallback;
    private String mDialogTag, mPersonTag;

    public static DialogPassword newInstance (String personRadioLabel){
        DialogPassword dialogPassword = new DialogPassword();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_RADIO_LABEL, personRadioLabel);
        dialogPassword.setArguments(bundle);
        return dialogPassword;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mDialogTag = getTag();
        Bundle extras = getArguments();
        if (extras!=null) mPersonTag = extras.getString(BUNDLE_RADIO_LABEL);
        if (mDialogTag.equals(PERSONS_ACCESS)) mCallback = (Callback)getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = View.inflate(mContext, R.layout.view_enter_password, null);
        final TextInputLayout textInputLayout = (TextInputLayout)dialogView.findViewById(R.id.enter_pass_input_layout);
        final AlertDialog dialogPass = new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setTitle(getString(R.string.view_enter_password_title))
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
                            if (mDialogTag.equals(ROOMS_ACCESS) && mPersonTag!=null){
                                new CloseRooms(mContext, mPersonTag, (CloseRoomInterface)getActivity()).execute();
                            }
                            dialog.dismiss();
                        } else {
                            textInputLayout.setError(getString(R.string.view_enter_password_entered_incorrect));
                        }
                    }
                });
            }
        });
        setCancelable(false);
        return dialogPass;
    }

    public interface Callback{
        void onDialogEnterPassDismiss();
    }
}
