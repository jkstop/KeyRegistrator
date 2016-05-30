package com.example.ivsmirnov.keyregistrator.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterEmailExtras;
import com.example.ivsmirnov.keyregistrator.interfaces.EmailInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 30.05.2016.
 */
public class EmailRecipientsPreference extends DialogPreference implements EmailInterface {

    public static final String ADD_NEW_RECIPIENT = "add_new_recipient";

    private Context mContext;
    private AdapterEmailExtras mAdapter;
    private ArrayList<String> mRecepientList;

    private int pressedButton = 0;

    public EmailRecipientsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton("Добавить", null);
    }

    @Override
    protected View onCreateDialogView() {
        View dialogView = View.inflate(mContext, R.layout.view_email_extras, null);
        mRecepientList = Settings.getRecepients();
        RecyclerView recipientView = (RecyclerView)dialogView.findViewById(R.id.preference_email_extra_list);
        mAdapter = new AdapterEmailExtras(mContext, this, AdapterEmailExtras.RECIPIENTS, mRecepientList);
        mAdapter.setHasStableIds(true);
        recipientView.setAdapter(mAdapter);
        recipientView.setLayoutManager(new LinearLayoutManager(mContext));
        return dialogView;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final AlertDialog dialog = (AlertDialog)getDialog();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //focus - http://stackoverflow.com/questions/9102074/android-edittext-in-dialog-doesnt-pull-up-soft-keyboard
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecepientList.add(ADD_NEW_RECIPIENT);
                mAdapter.notifyItemInserted(mRecepientList.size());
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        pressedButton = which;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (pressedButton == AlertDialog.BUTTON_POSITIVE){
            if (mRecepientList.contains(ADD_NEW_RECIPIENT)){
                mRecepientList.remove(ADD_NEW_RECIPIENT);
            }
            Settings.setRecepients(mRecepientList);
            callChangeListener(mRecepientList);
            pressedButton = 0;
        }

    }

    @Override
    public void onAddRecepient(View v, int position, int view_id) {
        if (view_id == R.id.card_email_extra_new_recipient_add){
            mRecepientList.add(v.getTag().toString());
            mRecepientList.remove(ADD_NEW_RECIPIENT);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteRecepient(int position, int view_id) {
        mRecepientList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onDeleteAttachment(int position, int view_id) {

    }

    @Override
    public void onAddAttachment() {

    }
}
