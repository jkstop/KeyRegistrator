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
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.FileManager;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_persons_grid;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import at.markushi.ui.CircleButton;

public class Persons_Fragment extends Fragment implements View.OnClickListener,UpdateTeachers{

    private Context mContext;
    private GridView mGridView;
    private DataBases db;
    private CircleButton mAddButton;

    private ArrayList<SparseArray> mAllItems;
    public adapter_persons_grid mAdapter;

    private static long today, lastDate;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferencesEditor;

    private int type;

    public static Persons_Fragment newInstance(){
        return new Persons_Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        if (extras != null) {
            type = extras.getInt(Values.PERSONS_FRAGMENT_TYPE);
        }

        if (type == Values.PERSONS_FRAGMENT_EDITOR) {
            setHasOptionsMenu(true);
        } else {
            setHasOptionsMenu(false);

        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (type == Values.PERSONS_FRAGMENT_SELECTOR) {
            if (((Launcher) getActivity()).getSupportActionBar() != null) {
                ((Launcher) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_persons_select));
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_persons_fragment,container,false);
        mContext = rootView.getContext();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        openBase();
        mAllItems = db.readTeachersFromDB();
        closeBase();
        sortByABC();

        mAddButton = (CircleButton)rootView.findViewById(R.id.add_user_button);
        mAddButton.setOnClickListener(this);

        mGridView = (GridView)rootView.findViewById(R.id.grid_for_base_sql);
        mAdapter = new adapter_persons_grid(mContext, mAllItems);
        mGridView.setAdapter(mAdapter);

        if (type == Values.PERSONS_FRAGMENT_EDITOR) {
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
        } else {
            Calendar calendar = Calendar.getInstance();
            today = calendar.get(Calendar.DATE);
            lastDate = mPreferences.getLong(Values.DATE, 0);

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = position - parent.getFirstVisiblePosition();
                    View rootView = parent.getChildAt(pos);
                    TextView textSurname = (TextView) rootView.findViewById(R.id.text_familia);
                    TextView textName = (TextView) rootView.findViewById(R.id.text_imya);
                    TextView textLastName = (TextView) rootView.findViewById(R.id.otchestvo);
                    TextView textKaf = (TextView) rootView.findViewById(R.id.kafedra);

                    String aud = getArguments().getString(Values.AUDITROOM);
                    String name = textSurname.getText().toString() + " "
                            + textName.getText().toString().charAt(0) + "." +
                            textLastName.getText().toString().charAt(0) + ".";
                    final Long time = System.currentTimeMillis();

                    openBase();
                    String path = db.findPhotoPath(new String[]{textSurname.getText().toString(), textName.getText().toString(),
                            textLastName.getText().toString(), textKaf.getText().toString()});
                    closeBase();
                    writeIt(aud, name, time, path);

                    getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance()).commit();
                }
            });
        }

        return rootView;
    }

    private void writeIt(String aud, String name, Long time, String path) {
        openBase();
        if (today == lastDate) {
            db.writeInDBJournal(aud, name, time, (long) 0, false);
            mPreferencesEditor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, db.cursorJournal.getCount());
        } else {
            db.writeInDBJournalHeaderDate();
            mPreferencesEditor.putInt(Values.CURSOR_POSITION, db.cursorJournal.getCount());
            mPreferencesEditor.commit();
            db.writeInDBJournal(aud, name, time, (long) 0, false);
            mPreferencesEditor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, db.cursorJournal.getCount() + 1);
        }
        db.updateStatusRooms(mPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), 0);
        db.updateLastVisitersRoom(mPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), name);
        db.updatePhotoPath(mPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), path);
        closeBase();

        mPreferencesEditor.putLong(Values.DATE, today);
        mPreferencesEditor.commit();
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
                String path = preferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                String srFileTeachers = mPath + "/Teachers.txt";
                String dtFileTeachers = path + "/Teachers.txt";
                DataBases.copyfile(mContext, srFileTeachers, dtFileTeachers);
                return true;
            case R.id.menu_teachers_download_favorite:
                startActivityForResult(new Intent(mContext, FileManager.class).putExtra("what", 11), 334);
                return true;
            case R.id.menu_teachers_download_local:
                startActivity(new Intent(mContext, FileManager.class).putExtra("what", 66));
                return true;
            case R.id.menu_teachers_delete:
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLEAR_TEACHERS);
                dialog.setArguments(bundle);
                dialog.setTargetFragment(Persons_Fragment.this,0);
                dialog.show(getFragmentManager(),"clearTeachers");
                return true;
            case R.id.menu_teachers_select_location_for_copy:
                startActivity(new Intent(mContext,FileManager.class).putExtra("buttonChoise", true).putExtra("pathFor","teachers"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            onFinishEditing();
        }
    }

    @Override
    public void onFinishEditing() {
        openBase();
        mAllItems = db.readTeachersFromDB();
        closeBase();

        sortByABC();
        mAdapter = new adapter_persons_grid(mContext, mAllItems);
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
