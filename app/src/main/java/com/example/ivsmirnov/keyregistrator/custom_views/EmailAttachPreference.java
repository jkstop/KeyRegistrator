package com.example.ivsmirnov.keyregistrator.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Preferences;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPreferenceExtra;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.io.File;
import java.util.ArrayList;

/**
 * Файлы для рассылки
 */
public class EmailAttachPreference extends DialogPreference implements AdapterPreferenceExtra.Callback{

    private Context mContext;
    private AdapterPreferenceExtra mAdapter;
    private ArrayList<String> mAttachList;


    public EmailAttachPreference(Context context, AttributeSet attrs) {
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
        mAttachList = SharedPrefs.getAttachments();
        mAdapter = new AdapterPreferenceExtra(mContext, AdapterPreferenceExtra.ATTACHMENTS, mAttachList, this);
        RecyclerView attachListView = (RecyclerView)dialogView.findViewById(R.id.recycler_main);
        attachListView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        attachListView.setAdapter(mAdapter);
        attachListView.setLayoutManager(new LinearLayoutManager(mContext));
        return dialogView;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final AlertDialog dialog = (AlertDialog)getDialog();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileChooser((Preferences)mContext)
                        .setFileListener(new FileChooser.FileSelectedListener() {
                            @Override
                            public void fileSelected(File file) {
                                mAttachList.add(file.getPath());
                                mAdapter.notifyItemInserted(mAttachList.size());
                            }
                        }).showDialog();
            }
        });
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefs.setAttachments(mAttachList);
                dialog.dismiss();
            }
        });
    }


    @Override
    public void onDeleteItem(int position) {
        mAttachList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onAddItem(String item) {

    }
}
