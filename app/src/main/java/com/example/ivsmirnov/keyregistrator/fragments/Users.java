package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;

import java.util.ArrayList;

public class Users extends Fragment implements
        RecycleItemClickListener,
        DialogPersonInfo.Callback,
        DialogSearch.Callback{

    public static final int REQUEST_CODE_SELECT_BACKUP_FAVORITE_STAFF_LOCATION = 204;
    private static final String PERSONS_FRAGMENT_TYPE = "persons_fragment_type";
    private static final String PERSONS_ACCESS_TYPE = "persons_access_type";
    public static final String PERSONS_SELECTED_ROOM = "persons_selected_room";
    public static final int PERSONS_FRAGMENT_EDITOR = 115;
    public static final int PERSONS_FRAGMENT_SELECTOR = 116;

    private static final int HANDLER_SHOW_PROGRESS = 100;
    private static final int HANDLER_HIDE_PROGRESS = 101;
    private static final int HANDLER_DATA_CHANGED = 102;

    public static boolean contentNeedsForUpdate = false;

    private Context mContext;
    private static RecyclerView mRecyclerView;
    private ListView mListView;

    private Handler mHandler;

    private ArrayList <PersonItem> mPersonsList;
    private AdapterPersonsGrid mAdapter;
    private AdapterPersonsCharacters mListCharAdapter;

    private ArrayList<CharacterItem> mListCharacters;

    private ProgressBar mLoadingBar;

    private int bundleType, bundleAccess;
    private String bundleRoom;

    private static long lastClickTime = 0;

    public static Users newInstance(int fragmentType, int accessType, String selectedRoom){
        Users users = new Users();
        Bundle bundle = new Bundle();
        bundle.putInt(PERSONS_FRAGMENT_TYPE, fragmentType);
        bundle.putInt(PERSONS_ACCESS_TYPE, accessType);
        bundle.putString(PERSONS_SELECTED_ROOM, selectedRoom);
        users.setArguments(bundle);
        return users;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        if (extras != null) {
            bundleType = extras.getInt(PERSONS_FRAGMENT_TYPE);
            bundleAccess = extras.getInt(PERSONS_ACCESS_TYPE);
            bundleRoom = extras.getString(PERSONS_SELECTED_ROOM);
        }

        mHandler = new Handler(Looper.getMainLooper()){
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
        final View rootView = inflater.inflate(R.layout.fragment_users, container, false);
        mContext = rootView.getContext();

        mListCharacters = new ArrayList<>();
        mPersonsList = new ArrayList<>();

        mListView = (ListView)rootView.findViewById(R.id.users_index_list);
        mLoadingBar = (ProgressBar)rootView.findViewById(R.id.progress_bar_main);

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
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        initListCharactersAdapter();
        initRecyclerAdapter();

        initPersons("#",true).start();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (bundleRoom == null){
            ((Launcher)getActivity()).getSupportActionBar().setTitle(getString(R.string.title_users));
            setHasOptionsMenu(true);
        } else {
            setHasOptionsMenu(false);
        }
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
                DialogSearch.newInstance(null, 0).show(getChildFragmentManager(),"search");
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
    public void onResume() {
        super.onResume();
        if (contentNeedsForUpdate){
            initPersons("#", true).start();
            contentNeedsForUpdate = false;
        }
    }

    public Thread initPersons (final String character, final boolean isInitAllCharacters){
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
    public void onItemClick(View v, int position, int viewID) {
        switch (bundleType){
            case PERSONS_FRAGMENT_SELECTOR:
                writeInBase(position, FavoriteDB.CLICK_USER_ACCESS);
                break;
            case PERSONS_FRAGMENT_EDITOR:
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

    private void writeInBase(int clickedPosition, int accessType){
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
        if (bundleRoom!=null){
            new BaseWriter(BaseWriter.WRITE_NEW, mContext, (BaseWriter.Callback)getParentFragment())
                    .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new BaseWriterParams()
                            .setAccessType(accessType)
                            .setAuditroom(bundleRoom)
                            .setPersonTag(mPersonsList.get(clickedPosition).getRadioLabel()));
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }


    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        switch (bundleType){
            case PERSONS_FRAGMENT_SELECTOR:
                writeInBase(position, FavoriteDB.CARD_USER_ACCESS);
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

    public class AdapterPersonsCharacters extends ArrayAdapter<CharacterItem> {

        private Context mContext;
        private ArrayList<CharacterItem> mCharacters;

        public AdapterPersonsCharacters(Context context, ArrayList<CharacterItem> characters) {
            super(context, R.layout.view_users_index_item,characters);
            this.mContext = context;
            this.mCharacters = characters;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rootView = inflater.inflate(R.layout.view_users_index_item, parent, false);
            TextView text = (TextView)rootView.findViewById(R.id.users_index_text);
            text.setText(mCharacters.get(position).getCharacter());
            if (mCharacters.get(position).getSelection()){
                rootView.setBackgroundResource(R.drawable.character_background);
            }

            return rootView;
        }
    }
}
