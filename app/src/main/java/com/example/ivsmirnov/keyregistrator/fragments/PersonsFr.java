package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsCharacters;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileLoader;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.Updatable;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

public class PersonsFr extends Fragment implements UpdateInterface, Updatable, RecycleItemClickListener,
        DialogPersonInfo.Callback,
DialogSearch.Callback{

    public static final int REQUEST_CODE_SELECT_BACKUP_FAVORITE_STAFF_LOCATION = 204;
    public static final String PERSONS_FRAGMENT_TYPE = "persons_fragment_type";
    public static final String PERSONS_ACCESS_TYPE = "persons_access_type";
    public static final String PERSONS_SELECTED_ROOM = "persons_selected_room";
    public static final int PERSONS_FRAGMENT_EDITOR = 115;
    public static final int PERSONS_FRAGMENT_SELECTOR = 116;

    private static final int HANDLER_SHOW_PROGRESS = 100;
    private static final int HANDLER_HIDE_PROGRESS = 101;
    private static final int HANDLER_DATA_CHANGED = 102;

    public static boolean contentNeedsForUpdate = false;

    private Context mContext;
    public static RecyclerView mRecyclerView;
    private ListView mListView;

    private Handler mHandler;

    private ArrayList <PersonItem> mPersonsList;
    public AdapterPersonsGrid mAdapter;
    private AdapterPersonsCharacters mListCharAdapter;

    private FloatingActionButton mAddFAB;

    private BaseWriterInterface mBaseWriterInterface;

    private ArrayList<CharacterItem> mListCharacters;

    private ProgressBar mLoadingBar;

    private int bundleType, bundleAccess;
    private String bundleRoom;

    private static long lastClickTime = 0;

    public static PersonsFr newInstance(int fragmentType, int accessType, String selectedRoom){
        PersonsFr personsFr = new PersonsFr();
        Bundle bundle = new Bundle();
        bundle.putInt(PERSONS_FRAGMENT_TYPE, fragmentType);
        bundle.putInt(PERSONS_ACCESS_TYPE, accessType);
        bundle.putString(PERSONS_SELECTED_ROOM, selectedRoom);
        personsFr.setArguments(bundle);
        return personsFr;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);

        Bundle extras = getArguments();
        if (extras != null) {
            bundleType = extras.getInt(PERSONS_FRAGMENT_TYPE);
            bundleAccess = extras.getInt(PERSONS_ACCESS_TYPE);
            bundleRoom = extras.getString(PERSONS_SELECTED_ROOM);
        }

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case HANDLER_SHOW_PROGRESS:
                        if (mLoadingBar!=null) mLoadingBar.setVisibility(View.VISIBLE);
                        break;
                    case HANDLER_HIDE_PROGRESS:
                        if (mLoadingBar!=null) mLoadingBar.setVisibility(View.INVISIBLE);
                        break;
                    case HANDLER_DATA_CHANGED:
                        mListCharAdapter.notifyDataSetChanged();
                        mAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.layout_persons_fr, container, false);
        mContext = rootView.getContext();

        mListCharacters = new ArrayList<>();
        mPersonsList = new ArrayList<>();

        mBaseWriterInterface = (BaseWriterInterface)getActivity();

        mListView = (ListView)rootView.findViewById(R.id.persons_fragment_list_characters);
        mLoadingBar = (ProgressBar)rootView.findViewById(R.id.layout_persons_fragment_loading_progress_bar);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (CharacterItem characterItem : mListCharacters){
                    characterItem.setSelection(false);
                }
                mListCharacters.get(i).setSelection(true);

                initPersons(mListCharacters.get(i).getCharacter(), false).start();
            }
        });

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_main);
        mRecyclerView.setItemAnimator(null);
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,getResources().getInteger(R.integer.persons_grid_columns)));

        initListCharactersAdapter();
        initRecyclerAdapter();

        initPersons("#",true).start();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (bundleRoom == null){
            ((Launcher)getActivity()).getSupportActionBar().setTitle(getString(R.string.toolbar_title_persons));
            setHasOptionsMenu(true);
        } else {
            setHasOptionsMenu(false);
        }

        //if (actionBar!=null){
        //    actionBar.setTitle(getResources().getString(R.string.toolbar_title_persons));
            //actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
        //}
        //ActionBar actionBar = getActivity().get
        //if (actionBar != null) {
        //    actionBar.setTitle(getResources().getString(R.string.toolbar_title_persons));
        //}

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_users, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_person_add:
                DialogSearch.newInstance(null, 0, this).show(getChildFragmentManager(),"search");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initRecyclerAdapter(){
        mAdapter = new AdapterPersonsGrid(mContext, mPersonsList, AdapterPersonsGrid.SHOW_FAVORITE_PERSONS, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initListCharactersAdapter(){
        mListCharAdapter = new AdapterPersonsCharacters(mContext, mListCharacters);
        mListView.setAdapter(mListCharAdapter);
    }

    private void getPersonsItems(String character){
        if (mPersonsList.size()!=0) mPersonsList.clear();
        mPersonsList.addAll(FavoriteDB.getPersonItems(character, bundleAccess));
    }

    private void getPersonsCharacters(){
        if (mListCharacters.size()!=0) mListCharacters.clear();
        mListCharacters.addAll(FavoriteDB.getPersonsCharacters(bundleAccess));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK){
            if (data!=null){
                if (requestCode== FileLoader.REQUEST_CODE_LOAD_FAVORITE_STAFF){
                    new FileLoader(mContext,
                            data.getData().getPath()).execute();
                }else if (requestCode == REQUEST_CODE_SELECT_BACKUP_FAVORITE_STAFF_LOCATION){
                    Settings.setPersonsBackupLocation(data.getData().getPath());
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("resume");
        if (contentNeedsForUpdate){
            initPersons("#", true).start();
            contentNeedsForUpdate = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("pause");
    }


    @Override
    public void updateInformation() {
        initPersons("#", true).start();
    }

    private Thread initPersons (final String character, final boolean isInitAllCharacters){
        return new Thread(new Runnable() {
            @Override
            public void run() {

                mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);

                if (isInitAllCharacters) getPersonsCharacters();
                getPersonsItems(character);

                mHandler.sendEmptyMessage(HANDLER_HIDE_PROGRESS);
                mHandler.sendEmptyMessage(HANDLER_DATA_CHANGED);
            }
        });
    }

    @Override
    public void onItemDeleted(int position) {
        mPersonsList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemChanged(String tag, int position) {
        PersonItem updatedPerson = FavoriteDB.getPersonItem(tag, FavoriteDB.LOCAL_USER, false);

        mPersonsList.get(position)
                .setLastname(updatedPerson.getLastname())
                .setFirstname(updatedPerson.getFirstname())
                .setMidname(updatedPerson.getMidname())
                .setDivision(updatedPerson.getDivision())
                .setAccessType(updatedPerson.getAccessType());
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        switch (bundleType){
            case PERSONS_FRAGMENT_SELECTOR:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
                if (bundleRoom!=null){
                    new BaseWriter(mContext, mBaseWriterInterface).execute(new BaseWriterParams()
                            .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                            .setAuditroom(bundleRoom)
                            .setPersonTag(mPersonsList.get(position).getRadioLabel()));
                }
                lastClickTime = SystemClock.elapsedRealtime();

                break;
            case PERSONS_FRAGMENT_EDITOR:
                //Bundle b = new Bundle();
                //b.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_EDIT);
                //b.putString(Dialogs.BUNDLE_TAG, mPersonsList.get(position).getRadioLabel());
                //b.putInt(Dialogs.DIALOG_PERSON_INFORMATION_KEY_POSITION, position);
                //Dialogs dialog = new Dialogs();
                //dialog.setArguments(b);
                //dialog.setTargetFragment(PersonsFr.this, 0);
                //dialog.show(getChildFragmentManager(), "edit");

                //ArrayList<String> item = new ArrayList<>();
                //item.add(DialogPersonInfo.PERSON_LASTNAME, mPersonsList.get(position).getLastname());
                //item.add(DialogPersonInfo.PERSON_FIRSTNAME, mPersonsList.get(position).getFirstname());
                //item.add(DialogPersonInfo.PERSON_MIDNAME, mPersonsList.get(position).getMidname());
                //item.add(DialogPersonInfo.PERSON_DIVISION, mPersonsList.get(position).getDivision());
                //item.add(DialogPersonInfo.PERSON_ACCESS, String.valueOf(mPersonsList.get(position).getAccessType()));
                //item.add(DialogPersonInfo.PERSON_TAG, mPersonsList.get(position).getRadioLabel());

                String [] itemM = new String[6];
                itemM[DialogPersonInfo.PERSON_LASTNAME] = mPersonsList.get(position).getLastname();
                itemM[DialogPersonInfo.PERSON_FIRSTNAME] = mPersonsList.get(position).getFirstname();
                itemM[DialogPersonInfo.PERSON_MIDNAME] = mPersonsList.get(position).getMidname();
                itemM[DialogPersonInfo.PERSON_DIVISION] = mPersonsList.get(position).getDivision();
                itemM[DialogPersonInfo.PERSON_ACCESS] = String.valueOf(mPersonsList.get(position).getAccessType());
                itemM[DialogPersonInfo.PERSON_TAG] = mPersonsList.get(position).getRadioLabel();

                DialogPersonInfo.newInstanse(itemM, position).show(getChildFragmentManager(), "person_info");

                break;
            default:
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        switch (bundleType){
            case PERSONS_FRAGMENT_SELECTOR:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
                if (bundleRoom!=null){
                    new BaseWriter(mContext, mBaseWriterInterface).execute(new BaseWriterParams()
                            .setAccessType(FavoriteDB.CARD_USER_ACCESS)
                            .setAuditroom(bundleRoom)
                            .setPersonTag(mPersonsList.get(position).getRadioLabel()));
                }
                lastClickTime = SystemClock.elapsedRealtime();
                break;
            default:
                break;
        }
    }

    @Override
    public void onUserDeleted(int position) {
        mPersonsList.remove(position);
        mAdapter.notifyItemRemoved(position);

        Snackbar.make(getView(),"Удалено",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUserChanged(int position, PersonItem newPerson) {
        mPersonsList.get(position)
                .setLastname(newPerson.getLastname())
                .setFirstname(newPerson.getFirstname())
                .setMidname(newPerson.getMidname())
                .setDivision(newPerson.getDivision())
                .setAccessType(newPerson.getAccessType());
        mAdapter.notifyItemChanged(position);

        Snackbar.make(getView(),"Изменено",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUserAdded(PersonItem personItem) {
        mPersonsList.add(0, personItem);
        mAdapter.notifyDataSetChanged();
    }
}
