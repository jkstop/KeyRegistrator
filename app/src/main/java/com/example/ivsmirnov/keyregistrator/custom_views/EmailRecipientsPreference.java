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
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPreferenceExtra;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 30.05.2016.
 */
public class EmailRecipientsPreference extends DialogPreference implements AdapterPreferenceExtra.Callback {

    private Context mContext;
    private AdapterPreferenceExtra mAdapter;
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
        View dialogView = View.inflate(mContext, R.layout.main_recycler, null);
        mRecepientList = SharedPrefs.getRecepients();
        RecyclerView recipientView = (RecyclerView)dialogView.findViewById(R.id.recycler_main);
        recipientView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mAdapter = new AdapterPreferenceExtra(mContext, AdapterPreferenceExtra.RECIPIENTS, mRecepientList, this);
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
                if (!mRecepientList.contains(AdapterPreferenceExtra.ADD_NEW_ITEM)){
                    mRecepientList.add(AdapterPreferenceExtra.ADD_NEW_ITEM);
                    mAdapter.notifyItemInserted(mRecepientList.size());
                }
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
            if (mRecepientList.contains(AdapterPreferenceExtra.ADD_NEW_ITEM)){
                mRecepientList.remove(AdapterPreferenceExtra.ADD_NEW_ITEM);
            }
            SharedPrefs.setRecepients(mRecepientList);
            callChangeListener(mRecepientList);
            pressedButton = 0;
        }

    }

    @Override
    public void onDeleteItem(int position) {
        mRecepientList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onAddItem(String item) {
        mRecepientList.add(item);
        mRecepientList.remove(AdapterPreferenceExtra.ADD_NEW_ITEM);
        mAdapter.notifyDataSetChanged();

    }
}
