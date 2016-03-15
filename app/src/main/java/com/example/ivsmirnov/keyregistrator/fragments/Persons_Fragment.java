package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_list_characters;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_persons_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.async_tasks.TakeKey;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.KeyInterface;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.TakeKeyParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

public class Persons_Fragment extends Fragment implements UpdateInterface, KeyInterface{

    private Context mContext;
    private static RecyclerView mRecyclerView;
    private ListView mListView;
    private FloatingActionButton mAddFAB;

    private static ArrayList<String> mPersonTagList;
    public adapter_persons_grid mAdapter;
    private adapter_list_characters mListAdapter;

    private KeyInterface mKeyInterface;

    private ArrayList<CharacterItem> mListCharacters;

    private ProgressBar mLoadingBar;

    private int type;

    private static long lastClickTime = 0;

    public static Persons_Fragment newInstance(){
        return new Persons_Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        if (extras != null) {
            type = extras.getInt(Values.PERSONS_FRAGMENT_TYPE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.toolbar_title_persons));
        }
        if (type == Values.PERSONS_FRAGMENT_SELECTOR){
            setHasOptionsMenu(false);
        }else{
            setHasOptionsMenu(true);
        }

    }

    private void initializeRecyclerAdapter(){
        if (type==Values.PERSONS_FRAGMENT_EDITOR){
            mAdapter = new adapter_persons_grid(mContext, mPersonTagList, Values.SHOW_FAVORITE_PERSONS, new RecycleItemClickListener() {
                @Override
                public void onItemClick(View v, int position, int viewID) {

                    Bundle b = new Bundle();
                    b.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_EDIT);
                    b.putString(Values.DIALOG_PERSON_INFORMATION_KEY_TAG, mPersonTagList.get(position));

                    Dialogs dialog = new Dialogs();
                    dialog.setArguments(b);
                    dialog.setTargetFragment(Persons_Fragment.this, 0);
                    dialog.show(getChildFragmentManager(), "edit");
                }

                @Override
                public void onItemLongClick(View v, int position, long timeIn) {
                }
            });
        }else if (type==Values.PERSONS_FRAGMENT_SELECTOR){
            mAdapter = new adapter_persons_grid(mContext, mPersonTagList, Values.SHOW_FAVORITE_PERSONS, new RecycleItemClickListener() {
                @Override
                public void onItemClick(View v, int position, int viewID) {

                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }

                    new TakeKey(mContext).execute(new TakeKeyParams()
                            .setAccessType(DataBaseJournal.ACCESS_BY_CLICK)
                            .setAuditroom(Settings.getLastClickedAuditroom())
                            .setPersonTag(mPersonTagList.get(position))
                            .setPublicInterface(mKeyInterface));

                    lastClickTime = SystemClock.elapsedRealtime();
                }

                @Override
                public void onItemLongClick(View v, int position, long timeIn) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }

                    new TakeKey(mContext).execute(new TakeKeyParams()
                            .setAccessType(DataBaseJournal.ACCESS_BY_CARD)
                            .setAuditroom(Settings.getLastClickedAuditroom())
                            .setPersonTag(mPersonTagList.get(position))
                            .setPublicInterface(mKeyInterface));

                    lastClickTime = SystemClock.elapsedRealtime();
                }
            });
        }

        mRecyclerView.setAdapter(mAdapter);
    }


    public static String getPersonInitials (String lastname, String firstname, String midname){
        String initials = "-";
        if (lastname.length() != 0 && firstname.length() != 1 && firstname.length()!=0 && midname.length() != 1 && midname.length() != 0) {
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

    private void showMainAuditroomsGrid(){
        getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main)).commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_persons_fragment, container, false);
        mContext = rootView.getContext();

        mPersonTagList = new ArrayList<>();

        mKeyInterface = this;

        mAddFAB = (FloatingActionButton)rootView.findViewById(R.id.persons_fragment_fab);
        mAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //запуск activity для поиска пользователя
                getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Search_Fragment.new_Instance(), getResources().getString(R.string.fragment_tag_search)).commit();

            }
        });

        mListView = (ListView)rootView.findViewById(R.id.list_for_base_sql);
        mLoadingBar = (ProgressBar)rootView.findViewById(R.id.layout_persons_fragment_loading_progress_bar);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (CharacterItem characterItem : mListCharacters){
                    characterItem.setSelection(false);
                }
                mListCharacters.get(i).setSelection(true);

                setmListCharactersAdapter();
                getTagsForSelectedCharacher(i);
                initializeRecyclerAdapter();
            }
        });

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_base_sql);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,3));

        new getPersonTags().execute();
        return rootView;
    }

    private void setmListCharactersAdapter(){
        mListAdapter = new adapter_list_characters(mContext,mListCharacters);
        mListView.setAdapter(mListAdapter);
    }


    private void initListCharacters(){
        mListCharacters = DataBaseFavorite.getPersonsCharacters();
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
                Save_to_file saveToFile = new Save_to_file(mContext,Values.WRITE_TEACHERS, true);
                saveToFile.execute();
                return true;
            case R.id.menu_teachers_download_favorite:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Values.REQUEST_CODE_LOAD_FAVORITE_STAFF);
                return true;
            case R.id.menu_teachers_delete:
                Dialogs dialog = new Dialogs();
                Bundle bundle = new Bundle();
                bundle.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_CLEAR_TEACHERS);
                dialog.setArguments(bundle);
                dialog.setTargetFragment(Persons_Fragment.this,0);
                dialog.show(getFragmentManager(),"clearTeachers");
                return true;
            case R.id.menu_teachers_select_location_for_copy:
                Intent iLC = new Intent(Intent.ACTION_GET_CONTENT);
                iLC.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                iLC.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                iLC.putExtra(FilePickerActivity.EXTRA_START_PATH, Settings.getPersonsBackupLocation());
                startActivityForResult(iLC,Values.REQUEST_CODE_SELECT_BACKUP_FAVORITE_STAFF_LOCATION);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (data!=null){
                if (requestCode==Values.REQUEST_CODE_LOAD_FAVORITE_STAFF){
                    new Loader_intent(mContext,
                            data.getData().getPath(),
                            this,
                            Values.REQUEST_CODE_LOAD_FAVORITE_STAFF).execute();
                }else if (requestCode == Values.REQUEST_CODE_SELECT_BACKUP_FAVORITE_STAFF_LOCATION){
                    Settings.setPersonsBackupLocation(data.getData().getPath());
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void updateInformation() {

        new getPersonTags().execute();
    }

    @Override
    public void onTakeKey() {

        showMainAuditroomsGrid();
    }

    private void getTagsForSelectedCharacher(int position){

        if (!mPersonTagList.isEmpty()) mPersonTagList.clear();

        mPersonTagList.addAll(DataBaseFavorite.getTagsForCurrentCharacter(mListCharacters.get(position).getCharacter()));
    }

    private class getPersonTags extends AsyncTask<Void,PersonItem,Void>{

        @Override
        protected void onPreExecute() {
            mLoadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            initListCharacters();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            setmListCharactersAdapter();
            getTagsForSelectedCharacher(0);
            initializeRecyclerAdapter();
            mLoadingBar.setVisibility(View.INVISIBLE);
        }
    }
}
