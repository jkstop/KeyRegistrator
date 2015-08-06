package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Add_user;
import com.example.ivsmirnov.keyregistrator.adapters.base_sql_activity_adapter;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import at.markushi.ui.CircleButton;

/**
 * Created by IVSmirnov on 03.08.2015.
 */
public class Persons_Fragment extends Fragment implements View.OnClickListener,UpdateTeachers{

    private Context mContext;
    private GridView mGridView;
    private DataBases db;
    private CircleButton mAddButton;

    private ArrayList<SparseArray> mAllItems;
    public base_sql_activity_adapter mAdapter;

    public static Persons_Fragment newInstance(){
        Persons_Fragment persons_fragment = new Persons_Fragment();
        return persons_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_persons_fragment,container,false);
        mContext = rootView.getContext();


        openBase();
        mAllItems = db.readTeachersFromDB();
        closeBase();
        sortByABC();

        mAddButton = (CircleButton)rootView.findViewById(R.id.add_user_button);
        mAddButton.setOnClickListener(this);

        mGridView = (GridView)rootView.findViewById(R.id.grid_for_base_sql);
        mAdapter = new base_sql_activity_adapter(mContext, mAllItems);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String gender = "";
                String lastname = (String) mAllItems.get(position).get(2);
                if (lastname.length() != 0) {
                    if (lastname.substring(lastname.length() - 1).equals("а")) {
                        gender = "Ж";
                    } else {
                        gender = "М";
                    }
                }
                String[] values = new String[]{(String) mAllItems.get(position).get(0),
                        (String) mAllItems.get(position).get(1),
                        (String) mAllItems.get(position).get(2),
                        (String) mAllItems.get(position).get(3),
                        gender};
                Bundle b = new Bundle();
                b.putInt(Values.DIALOG_TYPE, Values.DIALOG_EDIT);
                b.putStringArray("valuesForEdit", values);
                b.putInt("position", position);
                Dialog_Fragment dialog = new Dialog_Fragment();
                dialog.setArguments(b);
                dialog.setTargetFragment(Persons_Fragment.this, 0);
                dialog.show(getChildFragmentManager(), "edit");
            }
        });
        return rootView;
    }


    private void openBase(){
        db = new DataBases(mContext);
    }
    private void closeBase(){
        db.closeDBconnection();
    }
    private void sortByABC(){
        Collections.sort(mAllItems, new Comparator<SparseArray>() {
            @Override
            public int compare(SparseArray lhs, SparseArray rhs) {
                String first = String.valueOf(lhs.get(0));
                String second = String.valueOf(rhs.get(0));
                return first.compareToIgnoreCase(second);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_teachers, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_teachers_save_to_file:
                DataBases db = new DataBases(mContext);
                db.writeFile(Values.WRITE_TEACHERS);
                db.closeDBconnection();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String mPath = Environment.getExternalStorageDirectory().getPath();
                String path = preferences.getString(Values.PATH_FOR_COPY_ON_PC, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                String srFileTeachers = mPath + "/Teachers.txt";
                String dtFileTeachers = path + "/Teachers.txt";
                DataBases.copyfile(mContext, srFileTeachers, dtFileTeachers);
                return true;
            case R.id.menu_teachers_download_favorite:
                return true;
            case R.id.menu_teachers_download_local:
                return true;
            case R.id.menu_teachers_delete:
                return true;
            case R.id.menu_teachers_select_location_for_copy:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFinishEditing() {
        openBase();
        mAllItems = db.readTeachersFromDB();
        closeBase();

        sortByABC();
        mAdapter = new base_sql_activity_adapter(mContext, mAllItems);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        Dialog_Fragment dialogType = new Dialog_Fragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt(Values.DIALOG_TYPE,Values.INPUT_DIALOG);
        dialogType.setArguments(bundle1);
        dialogType.setTargetFragment(Persons_Fragment.this,0);
        dialogType.show(getChildFragmentManager(), "type");
    }
}
