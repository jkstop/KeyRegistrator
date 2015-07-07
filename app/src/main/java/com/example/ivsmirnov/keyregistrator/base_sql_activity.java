package com.example.ivsmirnov.keyregistrator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity extends ActionBarActivity implements Dialog_Fragment.EditDialogListener{

    public static final int DIALOG_NEW = 0;
    public static final int DIALOG_EDIT = 1;
    static final int POPUP_MENU_TEACHER = 1;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private Context context;
    public static GridView gridView;
    private DataBases db;

    public static ArrayList <String> nameList;
    public static ArrayList <String> surnameList;
    public static ArrayList <String> lastnameList;
    public static ArrayList <String> kafList;
    public static ArrayList <String> genderList;
    public static base_sql_activity_adapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_sql_activity);

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        gridView = (GridView)findViewById(R.id.grid_for_base_sql);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle b = new Bundle();
                b.putString("surname", surnameList.get(position));
                b.putString("name", nameList.get(position));
                b.putString("lastname", lastnameList.get(position));
                b.putString("kaf", kafList.get(position));
                showPopupMenu(POPUP_MENU_TEACHER, view, position, b);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
Log.d("onResume","ok");
        openBase();
        nameList = db.readTeachersFromDB(DataBasesRegist.COLUMN_NAME_FAVORITE);
        surnameList = db.readTeachersFromDB(DataBasesRegist.COLUMN_SURNAME_FAVORITE);
        lastnameList = db.readTeachersFromDB(DataBasesRegist.COLUMN_LASTNAME_FAVORITE);
        kafList = db.readTeachersFromDB(DataBasesRegist.COLUMN_KAF_FAVORITE);
        genderList = db.readTeachersFromDB(DataBasesRegist.COLUMN_GENDER_FAVORITE);
        closeBase();
        adapter = new base_sql_activity_adapter(context,kafList,nameList,surnameList,lastnameList,genderList);
        gridView.setAdapter(adapter);
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
                                b.putInt(Values.DIALOG_TYPE,44);
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

                                nameList.remove(position);
                                surnameList.remove(position);
                                lastnameList.remove(position);
                                kafList.remove(position);
                                genderList.remove(position);
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
        Dialog_Fragment dialogType = new Dialog_Fragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt(Values.DIALOG_TYPE,33);
        dialogType.setArguments(bundle1);
        dialogType.show(getSupportFragmentManager(), "type");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 99:
                Toast.makeText(context,"TOAST",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void openBase(){
        db = new DataBases(context);
    }
    private void closeBase(){
        db.closeDBconnection();
    }


    @Override
    public void onFinishEditDialog(String[] values, int position) {
        surnameList.remove(position);
        nameList.remove(position);
        lastnameList.remove(position);
        kafList.remove(position);
        genderList.remove(position);

        surnameList.add(values[0]);
        nameList.add(values[1]);
        lastnameList.add(values[2]);
        kafList.add(values[3]);
        genderList.add(values[4]);

        adapter.notifyDataSetChanged();

    }
}
