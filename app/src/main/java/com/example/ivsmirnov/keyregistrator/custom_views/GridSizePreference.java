package com.example.ivsmirnov.keyregistrator.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Диалог размер сетки
 */
public class GridSizePreference extends DialogPreference {

    private Context mContext;
    private MaterialNumberPicker mPickerRows, mPickerColumns;

    public GridSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected View onCreateDialogView() {
        View dialogView = View.inflate(mContext, R.layout.view_grid_size_pickers, null);
        mPickerRows = (MaterialNumberPicker)dialogView.findViewById(R.id.grid_size_pickers_rows);
        mPickerColumns = (MaterialNumberPicker)dialogView.findViewById(R.id.grid_size_pickers_columns);
        mPickerRows.setValue(SharedPrefs.getGridRows());
        mPickerColumns.setValue(SharedPrefs.getGridColumns());
        return dialogView;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                SharedPrefs.setGridColumns(mPickerColumns.getValue());
                SharedPrefs.setGridRows(mPickerRows.getValue());
                break;
            default:
                super.onClick(dialog, which);
        }

    }

}
