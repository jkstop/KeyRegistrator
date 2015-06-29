package com.example.ivsmirnov.keyregistrator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.widget.ProgressBar;
import android.widget.SeekBar;

/**
 * Created by ivsmirnov on 25.06.2015.
 */
public class Dialog_Fragment extends DialogFragment {

    private Context context;
    private int dialog_id;

    public Dialog_Fragment(Context c,int id){
        context = c;
        dialog_id = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        switch (dialog_id){
            case Values.DIALOG_SEEKBAR:
                final SeekBar seekBar = new SeekBar(getActivity());
                seekBar.setMax(40);
                seekBar.setProgress((int) (preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15)*100));
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float prog = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        prog = (float) (progress/100.0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        editor.putFloat(Values.DISCLAIMER_SIZE,prog);
                        editor.commit();
                    }
                });

                return new AlertDialog.Builder(getActivity())
                        .setTitle("Размер уведомления")
                        .setView(seekBar)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
            case Values.DIALOG_LOADING:
                ProgressBar progressBar = new ProgressBar(context);
                return new AlertDialog.Builder(getActivity())
                        //.setView(progressBar)
                        .setMessage("LOAD")
                        .create();

        }

        return null;
    }
}
