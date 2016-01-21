package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_list_characters;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_persons_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.custom_views.JournalItem;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Persons_Fragment extends Fragment implements UpdateInterface{

    private Context mContext;
    private static RecyclerView mRecyclerView;
    private ListView mListView;

    private static ArrayList<PersonItem> mAllItems;
    public adapter_persons_grid mAdapter;
    private adapter_list_characters mListAdapter;

    private ArrayList<String> mListCharacters;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferencesEditor;

    private int type;

    private static long lastClickTime = 0;

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
            setHasOptionsMenu(true);
        }

    }

    private void initializeRecyclerAdapter(){
        if (type==Values.PERSONS_FRAGMENT_EDITOR){
            mAdapter = new adapter_persons_grid(mContext, mAllItems, Values.SHOW_FAVORITE_PERSONS, new RecycleItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    String gender = "";
                    String lastname = mAllItems.get(position).Midname;
                    if (lastname.length() != 0) {
                        if (lastname.substring(lastname.length() - 1).equals("а")) {
                            gender = "Ж";
                        } else {
                            gender = "М";
                        }
                    }

                    Bundle b = new Bundle();
                    b.putInt(Values.DIALOG_TYPE, Values.DIALOG_EDIT);
                    b.putStringArray("valuesForEdit", new String[]{
                            mAllItems.get(position).Lastname,
                            mAllItems.get(position).Firstname,
                            mAllItems.get(position).Midname,
                            mAllItems.get(position).Division,
                            gender,
                            mAllItems.get(position).PhotoOriginal});
                    b.putInt("position", position);
                    Dialog_Fragment dialog = new Dialog_Fragment();
                    dialog.setArguments(b);
                    dialog.setTargetFragment(Persons_Fragment.this, 0);
                    dialog.show(getChildFragmentManager(), "edit");
                }

                @Override
                public void onItemLongClick(View v, int position, long timeIn) {
                }
            });
        }else if (type==Values.PERSONS_FRAGMENT_SELECTOR){
            mAdapter = new adapter_persons_grid(mContext, mAllItems, Values.SHOW_FAVORITE_PERSONS, new RecycleItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }

                    takeKey(Values.ACCESS_BY_CLICK, position);

                    lastClickTime = SystemClock.elapsedRealtime();
                }

                @Override
                public void onItemLongClick(View v, int position, long timeIn) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }

                    takeKey(Values.ACCESS_BY_CARD, position);

                    lastClickTime = SystemClock.elapsedRealtime();
                }
            });
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    private void takeKey(int accessType, int position){
        JournalItem journalItem  = createJournalItem(accessType,position);
        long positionInBase = Values.writeInJournal(mContext, journalItem);
        writeRoom(journalItem, positionInBase, position);
        showMainAuditroomsGrid();
    }

    private String getPersonInitials (String lastname, String firstname, String midname){
        String initials = "-";
        if (lastname.length() != 0 && firstname.length() != 1 && midname.length() != 1) {
            initials = lastname + " " + firstname.charAt(0) + "." + midname.charAt(0) + ".";
        } else {
            if (lastname.length() != 1 && firstname.length() != 1) {
                initials = lastname + " " + firstname;
            } else {
                if (lastname.length() != 1) {
                    initials = lastname;
                }
            }
        }
        return initials;
    }

    private JournalItem createJournalItem(int accessType, int position){
        return new JournalItem(getArguments().getString(Values.AUDITROOM),
                System.currentTimeMillis(),
                null,
                accessType,
                mAllItems.get(position).Lastname,
                mAllItems.get(position).Firstname,
                mAllItems.get(position).Midname,
                mAllItems.get(position).PhotoPreview);
    }

    private void writeRoom (JournalItem journalItem, long positionInBase, int position){
        DataBaseRooms dataBaseRooms = new DataBaseRooms(mContext);
        dataBaseRooms.updateRoom(new RoomItem(journalItem.Auditroom,
                Values.ROOM_IS_BUSY,
                journalItem.AccessType,
                positionInBase,
                getPersonInitials(journalItem.PersonLastname,journalItem.PersonFirstname,journalItem.PersonMidname),
                mAllItems.get(position).RadioLabel,
                mAllItems.get(position).PhotoOriginal));
        dataBaseRooms.closeDB();
    }

    private void showMainAuditroomsGrid(){
        getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main)).commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_persons_fragment, container, false);
        mContext = rootView.getContext();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
        mAllItems = dbFavorite.readTeachersFromDB();
        dbFavorite.closeDB();

        sortByABC();

        mListView = (ListView)rootView.findViewById(R.id.list_for_base_sql);
        mListCharacters = new ArrayList<>();

        for (int i=0;i<mAllItems.size();i++){
            String surname = mAllItems.get(i).Lastname;
            String startChar = surname.substring(0,1);
            if (!mListCharacters.contains(startChar)){
                mListCharacters.add(startChar);
            }
        }

        mListAdapter = new adapter_list_characters(mContext,mListCharacters);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String character = mListCharacters.get(i);
                move(character);
            }
        });

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_base_sql);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,3));

        initializeRecyclerAdapter();

        return rootView;
    }

    private void move (String symbol){

        for (int i=0;i<mAllItems.size();i++){
            String lastname = mAllItems.get(i).Lastname;
            String startChar = lastname.substring(0,1);
            if (symbol.equalsIgnoreCase(startChar)){
                mRecyclerView.scrollToPosition(i);
                break;
            }
        }

    }

   /* public static void writeIt(Context context,String aud, String name, Long time, String path, String tag, String cardOrHandle) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Calendar calendar = Calendar.getInstance();
        today = calendar.get(Calendar.DATE);
        lastDate = preferences.getLong(Values.DATE, 0);

        DataBaseJournal dbJournal = new DataBaseJournal(context);
        if (today == lastDate) {
            dbJournal.writeInDBJournal(aud, name, time, (long) 0, false);
            editor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, dbJournal.cursor.getCount());
        } else {
            dbJournal.writeInDBJournalHeaderDate();
            editor.putInt(Values.CURSOR_POSITION, dbJournal.cursor.getCount());
            editor.apply();
            dbJournal.writeInDBJournal(aud, name, time, (long) 0, false);
            editor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, dbJournal.cursor.getCount() + 1);
        }
        dbJournal.closeDB();

        DataBaseRooms dbRooms = new DataBaseRooms(context);
        dbRooms.updateStatusRooms(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), "false");
        dbRooms.updateLastVisitersRoom(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), name);
        dbRooms.updatePhotoPath(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), path);
        dbRooms.updateTagRoom(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), tag);
        dbRooms.updateCardOrHandle(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), cardOrHandle);
        dbRooms.closeDB();

        editor.putLong(Values.DATE, today);
        editor.apply();
    }*/

    private void sortByABC(){
        Collections.sort(mAllItems, new Comparator<PersonItem>() {
            @Override
            public int compare(PersonItem lhs, PersonItem rhs) {
                String first = String.valueOf(lhs.Lastname);
                String second = String.valueOf(rhs.Lastname);
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
                Save_to_file saveToFile = new Save_to_file(mContext,Values.WRITE_TEACHERS);
                saveToFile.execute();
                return true;
            case R.id.menu_teachers_download_favorite:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Values.LOAD_TEACHERS);
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
                Intent iLC = new Intent(Intent.ACTION_GET_CONTENT);
                iLC.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                iLC.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                iLC.putExtra(FilePickerActivity.EXTRA_START_PATH,
                        mPreferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS, Environment.getExternalStorageDirectory().getPath()));
                startActivityForResult(iLC,Values.SELECT_LOCATION_TEACHERS);
                return true;
            case R.id.menu_teachers_set_columns_number:
                Dialog_Fragment dialog_fragment = new Dialog_Fragment();
                Bundle bundlePersons = new Bundle();
                bundlePersons.putInt(Values.DIALOG_TYPE, Values.SELECT_COLUMNS_DIALOG);
                bundlePersons.putString("AudOrPer", "per");
                dialog_fragment.setArguments(bundlePersons);
                dialog_fragment.setTargetFragment(this, 0);
                dialog_fragment.show(getFragmentManager(), "columns");
                return true;
            case R.id.menu_teachers_selector_add:
                getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Search_Fragment.new_Instance(), getResources().getString(R.string.fragment_tag_search)).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (requestCode==Values.LOAD_TEACHERS){
                if (resultCode== Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    Loader_intent loader_intent = new Loader_intent(mContext,path,this,Values.LOAD_TEACHERS);
                    loader_intent.execute();
                }
            }else if (requestCode == Values.SELECT_LOCATION_TEACHERS){
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    mPreferencesEditor.putString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS,path);
                    mPreferencesEditor.apply();
                }
            }
           // onFinishEditing();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void updateInformation() {
        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
        mAllItems = dbFavorite.readTeachersFromDB();
        dbFavorite.closeDB();

        sortByABC();

        initializeRecyclerAdapter();

        mListCharacters = new ArrayList<>();
        for (int i=0;i<mAllItems.size();i++){
            String lastname = mAllItems.get(i).Lastname;
            String startChar = lastname.substring(0,1);
            if (!mListCharacters.contains(startChar)){
                mListCharacters.add(startChar);
            }
        }

        mListAdapter = new adapter_list_characters(mContext,mListCharacters);
        mListView.setAdapter(mListAdapter);
    }
}
