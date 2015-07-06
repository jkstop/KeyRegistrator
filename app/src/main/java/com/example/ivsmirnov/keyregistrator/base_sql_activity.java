package com.example.ivsmirnov.keyregistrator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity extends Activity {

    public static final int DIALOG_NEW = 0;
    public static final int DIALOG_EDIT = 1;
    static final int POPUP_MENU_TEACHER = 1;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private Context context;
    public static GridView gridView;
    private DataBases db;

    ArrayList <String> nameList;
    ArrayList <String> surnameList;
    ArrayList <String> lastnameList;
    ArrayList <String> kafList;
    ArrayList <String> genderList;
    base_sql_activity_adapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_sql_activity);

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        db = new DataBases(context);
        nameList = db.readTeachersFromDB(DataBasesRegist.COLUMN_NAME_FAVORITE);
        surnameList = db.readTeachersFromDB(DataBasesRegist.COLUMN_SURNAME_FAVORITE);
        lastnameList = db.readTeachersFromDB(DataBasesRegist.COLUMN_LASTNAME_FAVORITE);
        kafList = db.readTeachersFromDB(DataBasesRegist.COLUMN_KAF_FAVORITE);
        genderList = db.readTeachersFromDB(DataBasesRegist.COLUMN_GENDER_FAVORITE);
        db.closeDBconnection();

        gridView = (GridView)findViewById(R.id.grid_for_base_sql);
        adapter = new base_sql_activity_adapter(context,kafList,nameList,surnameList,lastnameList,genderList);
        gridView.setAdapter(adapter);
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
                                //сделать диалог для редактирования
                                break;
                            case R.id.popup_menu_clear:
                                db = new DataBases(context);
                                db.deleteFromTeachersDB(surname, name, lastname, kaf);
                                db.closeDBconnection();

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
}
