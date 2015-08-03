package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.custom_views.FloatingActionButton;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_Image;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.adapters.base_sql_activity_adapter;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity extends ActionBarActivity implements Dialog_Fragment.EditDialogListener {

    static final int POPUP_MENU_TEACHER = 1;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private static long today,lastDate;

    private Context context;
    public static GridView gridView;
    private DataBases db;

    ArrayList<SparseArray> allItems;
    public static base_sql_activity_adapter adapter;

    Loader_Image.OnImageLoaded loader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_sql_activity);

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

/*        FloatingActionButton fab = new FloatingActionButton.Builder(this)
                .withDrawable(getResources().getDrawable(R.drawable.fab_ic_add))
                .withButtonColor(Color.BLUE)
                .withGravity(Gravity.BOTTOM|Gravity.LEFT)
                .withMargins(0,0,16,16)
                .withButtonSize(100)
                .create();*/

        gridView = (GridView)findViewById(R.id.grid_for_base_sql);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle b = new Bundle();
                b.putString("surname", (String) allItems.get(position).get(0));
                b.putString("name", (String) allItems.get(position).get(1));
                b.putString("lastname", (String) allItems.get(position).get(2));
                b.putString("kaf", (String) allItems.get(position).get(3));
                showPopupMenu(POPUP_MENU_TEACHER, view, position, b);
                return false;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - parent.getFirstVisiblePosition();
                View rootView = parent.getChildAt(pos);
                TextView textSurname = (TextView) rootView.findViewById(R.id.text_familia);
                TextView textName = (TextView) rootView.findViewById(R.id.text_imya);
                TextView textLastName = (TextView) rootView.findViewById(R.id.otchestvo);

                String aud = getIntent().getStringExtra(Values.AUDITROOM);
                String name = textSurname.getText().toString() + " "
                        + textName.getText().toString().charAt(0) + "." +
                        textLastName.getText().toString().charAt(0) + ".";
                final Long time = System.currentTimeMillis();

                writeIt(aud, name, time);

                finish();
            }
        });


    }

    private void writeIt (String aud,String name,Long time){
        openBase();
        if (today==lastDate){
            db.writeInDBJournal(aud,name,time,(long)0,false);
            editor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, db.cursorJournal.getCount());
        }else{
            db.writeInDBJournalHeaderDate();
            editor.putInt(Values.CURSOR_POSITION, db.cursorJournal.getCount());
            editor.commit();
            db.writeInDBJournal(aud, name, time, (long) 0,false);
            editor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, db.cursorJournal.getCount()+1);
        }
        db.updateStatusRooms(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), 0);
        db.updateLastVisitersRoom(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), name);
        closeBase();

        editor.putLong(Values.DATE, today);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("onResume","basesql");

        Calendar calendar = Calendar.getInstance();
        today = calendar.get(Calendar.DATE);
        lastDate = sharedPreferences.getLong(Values.DATE, 0);

        openBase();
        allItems = db.readTeachersFromDB();
        closeBase();

        sortByABC();

        adapter = new base_sql_activity_adapter(context,allItems);
        gridView.setAdapter(adapter);

        Log.d("childs", String.valueOf(gridView.getChildCount()));
        //LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        for (int i=0;i<gridView.getChildCount();i++){
            View gridCell = gridView.getChildAt(i);
            ImageView imageView = (ImageView)gridCell.findViewById(R.id.image_sql);
            Log.d("sqlImage", String.valueOf(imageView.getTag()));
        }


    }


    private void sortByABC(){
        Collections.sort(allItems, new Comparator<SparseArray>() {
            @Override
            public int compare(SparseArray lhs, SparseArray rhs) {
                String first = String.valueOf(lhs.get(0));
                String second = String.valueOf(rhs.get(0));
                return first.compareToIgnoreCase(second);
            }
        });
    }



    private void showPopupMenu(int menu, View v,final int position,Bundle args){
        final PopupMenu popupMenu = new PopupMenu(this,v);
        final String surname = args.getString("surname");
        final String name = args.getString("name");
        final String lastname = args.getString("lastname");
        final String kaf = args.getString("kaf");
        switch (menu){
            case POPUP_MENU_TEACHER:
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.popup_menu_edit:
                                Dialog_Fragment dialog = new Dialog_Fragment();
                                String gender = "";
                                if (lastname.length() != 0) {
                                    if (lastname.substring(lastname.length()-1).equals("а")){
                                        gender = "Ж";
                                    }else {
                                        gender = "М";
                                    }
                                }
                                String [] values = new String[]{surname,name,lastname,kaf,gender};
                                Bundle b = new Bundle();
                                b.putInt(Values.DIALOG_TYPE,Values.DIALOG_EDIT);
                                b.putStringArray("valuesForEdit", values);
                                b.putInt("position", position);
                                dialog.setArguments(b);
                                dialog.setTargetFragment(dialog,99);
                                dialog.show(getSupportFragmentManager(),"edit");
                                break;
                            case R.id.popup_menu_clear:
                                openBase();
                                db.deleteFromTeachersDB(surname, name, lastname, kaf);
                                closeBase();

                                allItems.remove(position);
                                adapter.notifyDataSetChanged();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                break;
            default:
                break;
        }
    }

    public void showAddUserDialog(View view) {
       /* Dialog_Fragment dialogType = new Dialog_Fragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt(Values.DIALOG_TYPE,Values.INPUT_DIALOG);
        dialogType.setArguments(bundle1);
        dialogType.show(getSupportFragmentManager(), "type");*/
        startActivity(new Intent(this,Add_user.class));
    }

    private void openBase(){
        db = new DataBases(context);
    }
    private void closeBase(){
        db.closeDBconnection();
    }



    @Override
    public void onFinishEditDialog(String[] values, int position, int type) {

        openBase();
        allItems = db.readTeachersFromDB();
        closeBase();

        sortByABC();
        adapter = new base_sql_activity_adapter(context,allItems);
        gridView.setAdapter(adapter);
    }


}
