package com.example.ivsmirnov.keyregistrator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;


public class Teachers_Activity extends ActionBarActivity {

    private DataBases db;
    private static long today,lastDate;
    public static GridView gridView;
    BaseAdapter adapter;
    ArrayList <String> items;
    public static final int DIALOG_NEW = 0;
    public static final int DIALOG_EDIT = 1;
    static final int POPUP_MENU_TEACHER = 1;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    EditText editName;
    private int posForEdit;
    private String nameForEdit;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        context = this;
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        openBase();
        items = new ArrayList<>(db.readTeachersFromDB());
        closeBase();

        gridView = (GridView)findViewById(R.id.gridview_for_teachers_2);
        adapter = new TeachersBaseAdapter(this,items);
        gridView.setAdapter(adapter);

        sortByABC();

        Calendar calendar = Calendar.getInstance();
        today = calendar.get(Calendar.DATE);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.getTag().equals("add")){
                    showDialog(DIALOG_NEW);
                }else{
                    int pos = position - parent.getFirstVisiblePosition();
                    View rootView = parent.getChildAt(pos);
                    TextView text = (TextView)rootView.findViewById(R.id.textPerson);
                    String aud = getIntent().getStringExtra(Values.AUDITROOM);
                    String name = text.getText().toString();
                    final Long time = System.currentTimeMillis();

                    writeIt(aud,name,time);

                    finish();
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!view.getTag().equals("add")) {
                    showPopupMenu(POPUP_MENU_TEACHER, view, position, items.get(position));
                }
                return false;
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


    private void showPopupMenu(int menu, View v,final int position, final String name){
        PopupMenu popupMenu = new PopupMenu(this,v);
        switch (menu){
            case POPUP_MENU_TEACHER:
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.popup_menu_edit:
                                Bundle b = new Bundle();
                                b.putString("name", name);
                                b.putInt("pos",position);
                                editor.putString("nameForEdit",name);
                                editor.commit();
                                showDialog(DIALOG_EDIT, b);
                                break;
                            case R.id.popup_menu_clear:
                                openBase();
                                db.deleteFromTeachersDB(name);
                                closeBase();
                                items.remove(position);
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

    //сортировка по алфавиту
    private void sortByABC(){
        Collections.sort(items, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        for (int i =0;i<items.size();i++){
            if (items.get(i).equals("яяя")){
                Collections.swap(items,i,items.size()-1);
            }
        }
    }
    //дата для заголовка
    public static String showDate(){
        Date currentDate =  new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM",new Locale("ru"));
        return String.valueOf(dateFormat.format(currentDate));
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {

        switch (id){
            case DIALOG_EDIT:
                posForEdit = args.getInt("pos");
                nameForEdit = args.getString("name");
                editName.setText(nameForEdit);
            break;
            default:
                super.onPrepareDialog(id, dialog, args);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case DIALOG_NEW:
                LayoutInflater inflater = getLayoutInflater();
                View rowView = inflater.inflate(R.layout.dialog_input_name, null);
                final EditText editText = (EditText)rowView.findViewById(R.id.editName);
                final CheckBox checkBoxAddInBase = (CheckBox)rowView.findViewById(R.id.checkboxAdd);
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setView(rowView)
                        .setTitle("Новый преподаватель")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String aud = getIntent().getStringExtra(Values.AUDITROOM);
                                String name = editText.getText().toString();
                                final Long time = System.currentTimeMillis();

                                writeIt(aud,name,time);

                                if (checkBoxAddInBase.isChecked()){
                                    db = new DataBases(context);
                                    db.writeInDBTeachers(name);
                                    db.closeDBconnection();
                                }
                                finish();
                            }

                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                return builder.create();
            case DIALOG_EDIT:
                editName = new EditText(this);
                nameForEdit = sharedPreferences.getString("nameForEdit","null");
                AlertDialog.Builder editNameBuilder = new AlertDialog.Builder(this);
                editNameBuilder.setTitle("Редактирование")
                        .setView(editName)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String edited = editName.getText().toString();
                                items.remove(posForEdit);
                                items.add(posForEdit, edited);
                                sortByABC();
                                adapter.notifyDataSetChanged();
                                openBase();
                                db.updateTeachersDB(nameForEdit, edited);
                                closeBase();
                                removeDialog(DIALOG_EDIT);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(DIALOG_EDIT);
                            }
                        });
                return editNameBuilder.create();
        }
        return null;
    }

    private void openBase(){
        db = new DataBases(context);
    }
    private void closeBase(){
        db.closeDBconnection();
    }


    @Override
    protected void onResume() {
        super.onResume();

        lastDate = sharedPreferences.getLong(Values.DATE,0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        items.clear();
        finish();
    }

}
