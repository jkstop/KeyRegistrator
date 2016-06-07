package com.example.ivsmirnov.keyregistrator.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;

import java.sql.Connection;

/**
 * Created by Илья on 07.06.2016.
 */
public class RoomsPreference extends DialogPreference {

    private Context mContext;

    public RoomsPreference(Context context, AttributeSet attrs) {
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
        View dialogView = View.inflate(mContext, R.layout.view_email_extra_list, null);
        return dialogView;
    }
}
