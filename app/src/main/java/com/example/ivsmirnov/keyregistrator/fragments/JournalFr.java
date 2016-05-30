package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterJournalList;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetJournal;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.Updatable;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.GetJournalParams;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class JournalFr extends Fragment implements UpdateInterface,ActionBar.OnNavigationListener, RecycleItemClickListener, Updatable {

    public static final int REQUEST_CODE_SELECT_BACKUP_JOURNAL_LOCATION = 203;

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

    public static JournalFr newInstance() {
        return new JournalFr();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mHandler = new Handler(){
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

        ((Launcher)getActivity()).setToolbarTitle(R.string.toolbar_title_journal);
        //ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        //if (actionBar != null) {
         //   actionBar.setTitle(getResources().getString(R.string.toolbar_title_journal));
        //}
        showDateSpinner();
    }

    public static ArrayList<String> getDates(){
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_journal_fr,container,false);
        mContext = rootView.getContext();
        mJournalItems = new ArrayList<>();
        mDates = new ArrayList<>();

        mJournalRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_journal);
        mLoadingBar = (ProgressBar)rootView.findViewById(R.id.layout_journal_fragment_loading_progress_bar);
        mDates.addAll(getDates());

        mAdapterjournallist = new AdapterJournalList(mContext, this, mJournalItems);
        mAdapterjournallist.setHasStableIds(true);
        mJournalRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mJournalRecycler.setAdapter(mAdapterjournallist);
        mJournalRecycler.scrollToPosition(mJournalItems.size()-1);
        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_journal, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_journal_save_to_file:
                FileWriter saveToFile = new FileWriter(mContext, FileWriter.WRITE_JOURNAL, true);
                saveToFile.execute();
                return true;
            case R.id.menu_journal_upload_to_server:
                new ServerWriter(mContext, true).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.JOURNAL_UPDATE);
                return true;
            case R.id.menu_journal_download_from_server:
                new ServerReader(mContext, this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerReader.LOAD_JOURNAL);
                return true;
            case R.id.menu_journal_download_from_file:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Loader_intent.REQUEST_CODE_LOAD_JOURNAL);
                return true;
            case R.id.menu_journal_delete:
                Dialogs dialog = new Dialogs();
                Bundle bundle = new Bundle();
                bundle.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_CLEAR_JOURNAL);
                dialog.setTargetFragment(this, 0);
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"clearJournal");
                return true;
            case R.id.menu_journal_select_location_for_copy:
                Intent iLC = new Intent(Intent.ACTION_GET_CONTENT);
                iLC.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                iLC.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                iLC.putExtra(FilePickerActivity.EXTRA_START_PATH, Settings.getJournalBackupLocation());
                startActivityForResult(iLC,REQUEST_CODE_SELECT_BACKUP_JOURNAL_LOCATION);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (data!=null){
                if (requestCode == Loader_intent.REQUEST_CODE_LOAD_JOURNAL){
                    new Loader_intent(mContext,
                            data.getData().getPath(),
                            this,
                            Loader_intent.REQUEST_CODE_LOAD_JOURNAL).execute();
                }else if (requestCode == REQUEST_CODE_SELECT_BACKUP_JOURNAL_LOCATION){
                    Settings.setJournalBackupLocation(data.getData().getPath());
                }
            }
        }
    }

    @Override
    public void updateInformation() {
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

    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        Dialogs dialogs = new Dialogs();
        Bundle bundle = new Bundle();
        bundle.putInt(Dialogs.DIALOG_TYPE, Dialogs.DELETE_JOURNAL_ITEM);
        bundle.putLong(Dialogs.BUNDLE_TAG, timeIn);
        bundle.putInt(Dialogs.BUNDLE_POSITION, position);
        dialogs.setArguments(bundle);
        dialogs.setTargetFragment(this,0);
        dialogs.show(getFragmentManager(), getResources().getString(R.string.title_dialog_delete_journal_item));
    }

    private Thread getJournal (final Date date){
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
    public void onItemDeleted(int position) {
        mJournalItems.remove(position);
        mAdapterjournallist.notifyDataSetChanged();
    }

    @Override
    public void onItemChanged(String tag, int position) {
    }
}