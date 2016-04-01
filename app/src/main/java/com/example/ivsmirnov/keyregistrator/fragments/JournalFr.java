package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.Load_from_server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.GetJournalParams;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class JournalFr extends Fragment implements UpdateInterface,ActionBar.OnNavigationListener {

    public static final int REQUEST_CODE_SELECT_BACKUP_JOURNAL_LOCATION = 203;

    private Context mContext;
    private RecyclerView mJournalRecycler;
    private ActionBar mActionBar;
    private ArrayList<String> mDates;
    private AdapterJournalList mAdapterjournallist;
    private ArrayList <Long> mJournalTags;

    private ProgressBar mLoadingBar;

    public static JournalFr newInstance() {
        return new JournalFr();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.toolbar_title_journal));
        }
        showDateSpinner();
    }

    public static ArrayList<String> getDates(Context context){

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

        mJournalRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_journal);
        mLoadingBar = (ProgressBar)rootView.findViewById(R.id.layout_journal_fragment_loading_progress_bar);
        mDates = getDates(mContext);
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
            case R.id.menu_journal_download_to_server:
                new ServerWriter(mContext, true).execute(ServerWriter.JOURNAL_ALL);
                return true;
            case R.id.menu_journal_download_from_server:
                Load_from_server loadFromServer = new Load_from_server(mContext,this);
                loadFromServer.execute();
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
        mDates = getDates(mContext);
        Date date = null;
        if (mDates.size()!=0){
            try {
                date = new SimpleDateFormat("dd MMM yyyy", new Locale("RU","ru")).parse(mDates.get(0));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            date = new Date(System.currentTimeMillis());
        }

        try {
            new getJournalForDate(date).execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        showDateSpinner();
    }

    private void initializeJournal(){
        mAdapterjournallist = new AdapterJournalList(mContext, new RecycleItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int viewID) {

            }

            @Override
            public void onItemLongClick(View v, final int position, final long timeIn) {
                showDeleteItemDialog(position, timeIn);
            }
        }, mJournalTags);

        mJournalRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mJournalRecycler.setAdapter(mAdapterjournallist);
        mJournalRecycler.scrollToPosition(mJournalTags.size()-1);
    }

    private void showDeleteItemDialog(final int clickedPosition, final long clickedTag){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = View.inflate(mContext, R.layout.view_dialog_delete_journal_item, null);
        CardView selectedCard = (CardView)dialogView.findViewById(R.id.view_dialog_delete_journal_item_card);
        final CheckBox deleteFromJournalCheck = (CheckBox)dialogView.findViewById(R.id.view_dialog_delete_journal_item_delete_from_journal_check);
        final CheckBox deleteFromServerCheck = (CheckBox)dialogView.findViewById(R.id.view_dialog_delete_journal_item_delete_from_server_check);

        new GetJournal(new GetJournalParams()
                .setCard(selectedCard)
                .setTextAuditroom((TextView)selectedCard.findViewById(R.id.card_journal_item_text_auditroom))
                .setImagePerson((ImageView)selectedCard.findViewById(R.id.card_journal_item_person_image))
                .setTextInitials((TextView)selectedCard.findViewById(R.id.card_journal_item_person_initials))
                .setTextTimeIn((TextView)selectedCard.findViewById(R.id.card_journal_item_time_in))
                .setTextTimeOut((TextView)selectedCard.findViewById(R.id.card_journal_item_time_out))
                .setTimeIn(clickedTag),
                AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in))
                .execute();
        builder.setTitle(R.string.title_dialog_delete_journal_item)
                .setView(dialogView)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (deleteFromJournalCheck.isChecked()){ //удаление из журнала
                            mJournalTags.remove(clickedPosition);
                            mAdapterjournallist.notifyItemRemoved(clickedPosition);
                            JournalDB.deleteFromDB(clickedTag);
                        }

                        if (deleteFromServerCheck.isChecked()){ //удаление с сервера
                            new ServerWriter(clickedTag).execute(ServerWriter.JOURNAL_DELETE_ONE);
                        }


                    }
                })
                .setCancelable(true);
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Date date;
        try {
            date = new SimpleDateFormat("dd MMM yyyy", new Locale("RU","ru")).parse(mDates.get(itemPosition));
            new getJournalForDate(date).execute();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private class getJournalForDate extends AsyncTask<Void,Void,Void>{

        private Date mDate;
        private getJournalForDate(Date date){
            this.mDate = date;
        }

        @Override
        protected void onPreExecute() {
            mLoadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            mJournalTags = JournalDB.getJournalItemTags(mDate);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mLoadingBar.setVisibility(View.INVISIBLE);

            initializeJournal();
        }
    }
}
