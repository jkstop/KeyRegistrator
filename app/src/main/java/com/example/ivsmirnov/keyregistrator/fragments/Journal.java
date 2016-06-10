package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterJournalList;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class Journal extends Fragment implements
        ActionBar.OnNavigationListener,
        RecycleItemClickListener,
        ServerWriter.Callback,
        SQL_Connection.Callback {

    private static final int HANDLER_SHOW_PROGRESS = 100;
    private static final int HANDLER_HIDE_PROGRESS = 101;
    private static final int HANDLER_DATA_CHANGED = 102;

    private Context mContext;
    private RecyclerView mJournalRecycler;
    private ActionBar mActionBar;
    private ArrayList<String> mDates;
    private AdapterJournalList mAdapterjournallist;
    private ArrayList <JournalItem> mJournalItems;
    private static Handler mHandler;

    private ProgressBar mLoadingBar;

    public static boolean contentNeedsForUpdate = false;
    private int selectedPosition = 0;

    public static Journal newInstance() {
        return new Journal();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case HANDLER_SHOW_PROGRESS:
                        mLoadingBar.setVisibility(View.VISIBLE);
                        break;
                    case HANDLER_HIDE_PROGRESS:
                        mLoadingBar.setVisibility(View.INVISIBLE);
                        break;
                    case HANDLER_DATA_CHANGED:
                        mAdapterjournallist.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Launcher)getActivity()).setToolbarTitle(R.string.title_journal);
        showDateSpinner();
    }

    private static ArrayList<String> getDates(){
        return JournalDB.readJournalDatesFromDB();
    }

    private void showDateSpinner(){
        mActionBar = ((Launcher)getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(mContext,android.R.layout.simple_spinner_dropdown_item,mDates);
            mActionBar.setListNavigationCallbacks(spinnerAdapter,this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showDateSpinner();

        if (contentNeedsForUpdate){
            updateList();
            contentNeedsForUpdate = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_recycler,container,false);
        mContext = rootView.getContext();
        mJournalItems = new ArrayList<>();
        mDates = new ArrayList<>();

        mJournalRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_main);
        mLoadingBar = (ProgressBar)rootView.findViewById(R.id.progress_bar_main);
        mDates.addAll(getDates());

        mAdapterjournallist = new AdapterJournalList(mContext, this, mJournalItems);
        mJournalRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mJournalRecycler.setAdapter(mAdapterjournallist);
        mJournalRecycler.scrollToPosition(mJournalItems.size()-1);
        return rootView;

    }

    private void updateList(){
        if (!mDates.isEmpty()) mDates.clear();
        mDates.addAll(getDates());

        if (mDates.size() == 0){ //если дат нет (т.е. журнал пуст), то очищаем список
            mJournalItems.clear();
            mAdapterjournallist.notifyDataSetChanged();
        }

        showDateSpinner();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        System.out.println("selected navigation item " + mDates.get(itemPosition));
        Date date;
        try {
            date = new SimpleDateFormat("dd MMM yyyy", new Locale("RU","ru")).parse(mDates.get(itemPosition));
            getJournal(date).start();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        selectedPosition = position;

        //удаление из базы
        JournalDB.deleteFromDB(mJournalItems.get(position).getTimeIn());

        if (SharedPrefs.getWriteServerStatus()){
            SQL_Connection.getConnection(null, 0, this);
        }

        mJournalItems.remove(position);
        mAdapterjournallist.notifyItemRemoved(position);
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
    }

    public Thread getJournal (final Date date){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);
                if (!mJournalItems.isEmpty()) mJournalItems.clear();
                mJournalItems.addAll(JournalDB.getJournalItemsForCurrentDate(date));
                mHandler.sendEmptyMessage(HANDLER_HIDE_PROGRESS);
                mHandler.sendEmptyMessage(HANDLER_DATA_CHANGED);
            }
        });
    }

    @Override
    public void onSuccessServerWrite() {
    }

    @Override
    public void onErrorServerWrite() {
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        new ServerWriter(ServerWriter.DELETE_ONE, mJournalItems.get(selectedPosition), this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
    }

    @Override
    public void onServerConnectException(Exception e) {
    }
}
