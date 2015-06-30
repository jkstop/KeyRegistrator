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
import android.widget.Toast;

/**
 * Created by ivsmirnov on 25.06.2015.
 */
public class Dialog_Fragment extends android.support.v4.app.DialogFragment {

    private Context context;
    private int dialog_id;

    public Dialog_Fragment(){
    }
/*
    public Dialog_Fragment(Context c,int id){
        context = c;
        dialog_id = id;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog_id = getArguments().getInt(Values.DIALOG_TYPE,0);
        context = getActivity().getApplicationContext();
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
            case Values.DIALOG_CLEAR_JOURNAL:
                return new AlertDialog.Builder(getActivity())
                        .setTitle("ВНИМАНИЕ!")
                        .setMessage("Из журнала будут удалены все записи. Продолжить?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBases db = new DataBases(context);
                                db.clearJournalDB();
                                db.closeDBconnection();
                                Toast.makeText(context,"Готово!",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.DIALOG_CLEAR_TEACHERS:
                return new AlertDialog.Builder(getActivity())
                        .setTitle("ВНИМАНИЕ!")
                        .setMessage("Из базы данных преподавателей будут удалены все записи. Продолжить?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBases db = new DataBases(context);
                                db.clearTeachersDB();
                                db.closeDBconnection();
                                Toast.makeText(context,"Готово!",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
        }

        return null;
    }
}
