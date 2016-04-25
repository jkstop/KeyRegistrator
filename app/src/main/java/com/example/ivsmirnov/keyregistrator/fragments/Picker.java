package com.example.ivsmirnov.keyregistrator.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.NumberPicker;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Фрагмент с pickers для выбора колонок и строк
 */
public class Picker extends Fragment {

    public static final int PICKER_PORTRAIT = 100;
    public static final int PICKER_LANDSCAPE = 200;
    public static final String PICKER_TYPE = "picker_Type";

    private int pickersType = 0;

    private GridView gridView;
    private BaseAdapter adapter;

    public static Picker newInstance (int pickerType){
        Picker picker = new Picker();
        Bundle args = new Bundle();
        args.putInt(PICKER_TYPE, pickerType);
        picker.setArguments(args);
        return picker;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickersType = getArguments().getInt(PICKER_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_table, container, false);
        final MaterialNumberPicker pickerColumns = (MaterialNumberPicker)rootView.findViewById(R.id.view_table_picker_columns);
        final MaterialNumberPicker pickerRows = (MaterialNumberPicker)rootView.findViewById(R.id.view_table_picker_rows);

        pickerColumns.setOnScrollListener(scrollListener);
        pickerRows.setOnScrollListener(scrollListener);

        String [] values;

        gridView = (GridView)rootView.findViewById(R.id.view_table_grid);

        switch (pickersType){
            case PICKER_LANDSCAPE:
                pickerColumns.setValue(Settings.getColumnsLandscape());
                pickerRows.setValue(Settings.getRowsLandscape());

                adapter = new GridAdapter(Settings.getColumnsLandscape()*Settings.getRowsLandscape(),RoomDB.getRoomList().size());
                gridView.setNumColumns(Settings.getColumnsLandscape());
                gridView.setAdapter(adapter);
                break;
            case PICKER_PORTRAIT:
                pickerColumns.setValue(Settings.getColumnsPortrait());
                pickerRows.setValue(Settings.getRowsPortrait());
                break;
            default:
                break;
        }
        return rootView;
    }

    NumberPicker.OnScrollListener scrollListener = new NumberPicker.OnScrollListener() {
        @Override
        public void onScrollStateChange(NumberPicker view, int scrollState) {
            switch (pickersType){
                case PICKER_LANDSCAPE:
                    switch (view.getId()){
                        case R.id.view_table_picker_columns:
                            Settings.setColumnsLandscape(view.getValue());
                            break;
                        case R.id.view_table_picker_rows:
                            Settings.setRowsLandscape(view.getValue());
                            break;
                        default:
                            break;
                    }
                    break;
                case PICKER_PORTRAIT:
                    switch (view.getId()){
                        case R.id.view_table_picker_columns:
                            Settings.setColumnsPortrait(view.getValue());
                            break;
                        case R.id.view_table_picker_rows:
                            Settings.setRowsPortrait(view.getValue());
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private class GridAdapter extends BaseAdapter{

        private int totalItemsCount, currentItemsCount;
        private Integer[] totalItems, currentItems;

        public GridAdapter (int totalItemsCount, int currentItemsCount){
            System.out.println("new adapter " + totalItemsCount);
            this.totalItemsCount = totalItemsCount;
            this.currentItemsCount = currentItemsCount;
            this.totalItems = new Integer[totalItemsCount];
            this.currentItems = new Integer[currentItemsCount];
        }


        @Override
        public int getCount() {
            return totalItemsCount;
        }

        @Override
        public Object getItem(int position) {
            return totalItems[position];
        }

        @Override
        public long getItemId(int position) {
            return totalItems[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            System.out.println("getView");
            View rootView = new Button(getContext());
            rootView.setBackgroundColor(Color.LTGRAY);
            return rootView;
        }
    }
}
