package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_main_auditrooms_grid_resize;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.custom_views.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Dialog_Fragment extends DialogFragment{

    private Context mContext;
    private int dialog_id;
   // private SharedPreferences sharedPreferences;
   // private SharedPreferences.Editor editor;
    private Settings mSettings;
    private LayoutInflater mInflater;
    private Resources mResources;
    private boolean pin_verify;

    public static FrameLayout mFrameGrid;

    private int mProgress = 0;

    DialogInterface.OnDismissListener onDismissListener;


    public Dialog_Fragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog_id = getArguments().getInt(Values.DIALOG_TYPE,0);
        mContext = getActivity();
        mSettings = new Settings(mContext);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = mContext.getResources();
    }


    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        switch (dialog_id){
            case Values.DIALOG_CLEAR_JOURNAL:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(mResources.getString(R.string.dialog_clear_journal_title))
                        .setMessage(mResources.getString(R.string.dialog_clear_journal_message))
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                                dbJournal.clearJournalDB();
                                dbJournal.closeDB();

                                UpdateInterface listen = (UpdateInterface)getTargetFragment();
                                listen.updateInformation();

                                Toast.makeText(mContext,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.DIALOG_CLEAR_TEACHERS:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(mResources.getString(R.string.dialog_clear_teachers_title))
                        .setMessage(mResources.getString(R.string.dialog_clear_teachers_message))
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
                                dbFavorite.clearTeachersDB();
                                dbFavorite.closeDB();

                                UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                updateInterface.updateInformation();

                                Toast.makeText(mContext,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.DIALOG_EDIT:

                final View dialogView = mInflater.inflate(R.layout.layout_person_information, null);
                final ImageView personImage = (ImageView) dialogView.findViewById(R.id.person_information_image);
                final String [] values = getArguments().getStringArray("valuesForEdit");
                final DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);

                final TextInputLayout inputLastname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_lastname_layout);
                final TextInputLayout inputFirstname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_firstname_layout);
                final TextInputLayout inputMidname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_midname_layout);
                final TextInputLayout inputDivision = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_division_layout);

                try {
                    assert values != null;
                    inputLastname.getEditText().setText(values[0]);
                    inputFirstname.getEditText().setText(values[1]);
                    inputMidname.getEditText().setText(values[2]);
                    inputDivision.getEditText().setText(values[3]);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

/*
                try {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    if (!imageLoader.isInited()){
                        imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
                    }
                    imageLoader.displayImage("file://" + values[5], personImage);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }*/

                byte[] decodedString = Base64.decode(values[5], Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                personImage.setImageBitmap(bitmap);

                AlertDialog.Builder builderEdit = new AlertDialog.Builder(getActivity());
                builderEdit.setView(dialogView);
                builderEdit.setNeutralButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (values!=null){
                            dbFavorite.deleteFromTeachersDB(new PersonItem(values[0],values[1],values[2],values[3],null,null,null,null));
                            dbFavorite.closeDB();
                        }
                        UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                        updateInterface.updateInformation();
                    }
                });
                builderEdit.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dbFavorite.closeDB();
                    }
                });
                builderEdit.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String [] source = getArguments().getStringArray("valuesForEdit");
                        String[] edited = new String[4];

                        edited[0] = inputLastname.getEditText().getText().toString();
                        edited[1] = inputFirstname.getEditText().getText().toString();
                        edited[2] = inputMidname.getEditText().getText().toString();
                        edited[3] = inputDivision.getEditText().getText().toString();

                        dbFavorite.updateTeachersDB(source, edited);
                        dbFavorite.closeDB();


                        UpdateTeachers activity;
                        if (getTargetFragment()==null){
                            activity = (UpdateTeachers)getActivity();
                        }else{
                            activity = (UpdateTeachers)getTargetFragment();
                        }

                        activity.onFinishEditing();
                    }
                });
                builderEdit.setCancelable(false);
                return builderEdit.create();
            case Values.DELETE_ROOM_DIALOG:
                final String aud = getArguments().getString("aud");
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.title_delete_room_dialog))
                        .setMessage(getResources().getString(R.string.dialog_delete_room_message) + " "+ aud + "?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                                dbRooms.deleteFromRoomsDB(aud);
                                dbRooms.closeDB();

                                UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                updateInterface.updateInformation();

                                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),R.string.done,Snackbar.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
            case Values.DIALOG_CLEAR_ROOMS:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_clear_rooms_title)
                        .setMessage(R.string.dialog_clear_rooms_message)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                                dbRooms.clearRoomsDB();
                                dbRooms.closeDB();

                                UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                updateInterface.updateInformation();
                                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),R.string.done,Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.ADD_ROOM_DIALOG:
                final TextInputLayout enterAuditroomLayout = (TextInputLayout) mInflater.inflate(R.layout.view_enter_auditroom,null);
                final AppCompatEditText enterAuditroomText = (AppCompatEditText)enterAuditroomLayout.findViewById(R.id.view_auditroom_enter_room);
                AppCompatButton enterAuditroomOkButton = (AppCompatButton)enterAuditroomLayout.findViewById(R.id.view_auditroom_ok_button);
                enterAuditroomOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText = enterAuditroomText.getText().toString();
                        if (!inputText.isEmpty()){
                            DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                            dbRooms.writeInRoomsDB(new RoomItem(
                                    inputText,
                                    Values.ROOM_IS_FREE,
                                    Values.ACCESS_BY_CLICK,
                                    0,
                                    null,
                                    null,
                                    null));
                            dbRooms.closeDB();

                            enterAuditroomText.getText().clear();
                            UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                            updateInterface.updateInformation();
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),R.string.done,Snackbar.LENGTH_SHORT).show();
                            dismiss();
                        }else{
                            enterAuditroomLayout.setError(getResources().getString(R.string.input_empty_error));
                        }
                    }
                });
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.view_enter_auditroom_title)
                        .setView(enterAuditroomLayout)
                        .create();
            case Values.SELECT_COLUMNS_DIALOG:
                final String ident = getArguments().getString("AudOrPer");
                final int mSelectedItemAud = mSettings.getAuditroomColumnsCount();
                //final int mSelectedItemPer = sharedPreferences.getInt(Values.COLUMNS_PER_COUNT, 0);
                LayoutInflater layoutInflater1 = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View pickerView  = layoutInflater1.inflate(R.layout.view_column_selector,null);
                final NumberPicker numberPicker = (NumberPicker)pickerView.findViewById(R.id.select_column_number_picker);
                setNumberPickerTextColor(numberPicker,Color.BLACK);
                numberPicker.setMaxValue(5);
                numberPicker.setMinValue(2);
                if (ident != null && ident.equalsIgnoreCase("aud")) {
                    numberPicker.setValue(mSelectedItemAud);
                } else if (ident != null && ident.equalsIgnoreCase("per")) {
                    //numberPicker.setValue(mSelectedItemPer);
                }
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.dialog_columns_title))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ident != null && ident.equalsIgnoreCase("aud")) {
                                    mSettings.setAuditroomColumnsCount(numberPicker.getValue());
                                    UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                    updateInterface.updateInformation();
                                } else if (ident != null && ident.equalsIgnoreCase("per")) {
                                    //editor.putInt(Values.COLUMNS_PER_COUNT, numberPicker.getValue());
                                    //editor.commit();
                                    //UpdateTeachers updateTeachers = (UpdateTeachers) getTargetFragment();
                                    //updateTeachers.onFinishEditing();
                                }
                                dialog.cancel();
                            }
                        })
                        .setView(pickerView)
                        .create();

            case Values.DIALOG_RESIZE_ITEMS:
                View rootView =  mInflater.inflate(R.layout.view_resize_main_fragment_items,null);
                final CardView cardView = (CardView)rootView.findViewById(R.id.layout_main_fragment_disclaimer_card);
                final SeekBar mResizeSeekBar = (SeekBar)rootView.findViewById(R.id.view_resize_vertical_seekbar);
                mFrameGrid = (FrameLayout)rootView.findViewById(R.id.frame_for_grid_aud);

                final LinearLayout.LayoutParams cardViewLayoutParams = (LinearLayout.LayoutParams)cardView.getLayoutParams();
                final LinearLayout.LayoutParams frameGridParams = (LinearLayout.LayoutParams)mFrameGrid.getLayoutParams();

                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                ArrayList <RoomItem> mRoomsItems = dbRooms.readRoomsDB();
                dbRooms.closeDB();

                RecyclerView mRoomsGrid = (RecyclerView)rootView.findViewById(R.id.main_fragment_auditroom_grid);
                mRoomsGrid.setClickable(false);
                mRoomsGrid.setLayoutManager(new GridLayoutManager(mContext, mSettings.getAuditroomColumnsCount()));
                final adapter_main_auditrooms_grid_resize mAdapter = new adapter_main_auditrooms_grid_resize(mContext, mRoomsItems);
                mRoomsGrid.setAdapter(mAdapter);

                int weightCard = mSettings.getDisclaimerWeight();
                mResizeSeekBar.setMax(100);
                mResizeSeekBar.setProgress(weightCard);
                cardViewLayoutParams.weight = weightCard;
                frameGridParams.weight = mResizeSeekBar.getMax() - weightCard;
                mResizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        cardViewLayoutParams.weight = progress;
                        frameGridParams.weight = mResizeSeekBar.getMax() - progress;
                        cardView.requestLayout();
                        mFrameGrid.requestLayout();
                        mAdapter.notifyDataSetChanged();

                        mProgress = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.view_resize_title)
                        .setView(rootView)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mProgress==0){
                                    mSettings.setDisclaimerWeight(mResizeSeekBar.getProgress());
                                }else{
                                    mSettings.setDisclaimerWeight(mProgress);
                                }

                                UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                updateInterface.updateInformation();

                                dismiss();
                            }
                        })
                        .show();
            case Values.DIALOG_CLOSE_ROOM:

                final TextInputLayout textInputLayout = (TextInputLayout) mInflater.inflate(R.layout.view_enter_password,null);
                final AppCompatEditText editPassword = (AppCompatEditText)textInputLayout.findViewById(R.id.view_enter_password_edit_text);
                AppCompatButton okButton = (AppCompatButton)textInputLayout.findViewById(R.id.view_enter_password_ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editPassword.getText().toString().equalsIgnoreCase("1212")){
                            if (getArguments().getInt(Values.DIALOG_CLOSE_ROOM_TYPE)==Values.DIALOG_CLOSE_ROOM_TYPE_ROOMS){
                                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                                String aud = getArguments().getString("aud");
                                long pos = getArguments().getLong(Values.POSITION_IN_BASE_FOR_ROOM,-1);
                                if (pos != -1) {
                                    dbJournal.updateDB(pos);
                                }
                                dbRooms.updateRoom(new RoomItem(aud,
                                        Values.ROOM_IS_FREE,
                                        Values.ACCESS_BY_CLICK,
                                        0,
                                        null,
                                        null,
                                        null));
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.fragment_tag_main)).commit();
                                dbJournal.closeDB();
                                dbRooms.closeDB();
                                dismiss();
                            }else{
                                Bundle bundle = new Bundle();
                                bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                                bundle.putString(Values.AUDITROOM, getArguments().getString(Values.AUDITROOM));
                                Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                                persons_fragment.setArguments(bundle);

                                getActivity().getSupportFragmentManager()
                                        .beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.fragment_tag_persons)).commit();
                            }
                        }else{
                            textInputLayout.setError(getResources().getString(R.string.view_enter_password_entered_incorrect));
                            editPassword.getText().clear();
                        }
                    }
                });

                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.view_enter_password_title)
                        .setView(textInputLayout)
                        .setCancelable(false)
                        .create();
            case Values.DIALOG_SQL_CONNECT:
                LayoutInflater inflater_sql = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View sqlDialogView = inflater_sql.inflate(R.layout.layout_dialog_sql_connect,null);
                final EditText inputServer = (EditText)sqlDialogView.findViewById(R.id.dialog_sql_connect_input_server);
                final EditText inputUser = (EditText)sqlDialogView.findViewById(R.id.dialog_sql_connect_input_user);
                final EditText inputPassowrd = (EditText)sqlDialogView.findViewById(R.id.dialog_sql_connect_input_password);
                final TextView connectionStatus = (TextView)sqlDialogView.findViewById(R.id.dialog_sql_connect_connection_status);
                Button mCheckButton = (Button)sqlDialogView.findViewById(R.id.dialog_sql_connect_check_button);

                ServerConnectionItem connectionParams = mSettings.getServerConnectionParams();
                inputServer.setText(connectionParams.getServerName());
                inputUser.setText(connectionParams.getUserName());
                inputPassowrd.setText(connectionParams.getUserPassword());
                if (mSettings.getServerStatus()){
                    connectionStatus.setText(R.string.connected);
                    connectionStatus.setTextColor(Color.GREEN);
                }else{
                    connectionStatus.setText(R.string.disconnected);
                    connectionStatus.setTextColor(Color.RED);
                }

                mCheckButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ServerConnectionItem serverConnectionItem = new ServerConnectionItem()
                                .setServerName(inputServer.getText().toString())
                                .setUserName(inputUser.getText().toString())
                                .setUserPassword(inputPassowrd.getText().toString());
                        if (SQL_Connector.check_sql_connection(mContext, serverConnectionItem)){
                            connectionStatus.setText(R.string.connected);
                            connectionStatus.setTextColor(Color.GREEN);
                            mSettings.setServerConnectionParams(serverConnectionItem);
                        }else{
                            connectionStatus.setText(R.string.disconnected);
                            connectionStatus.setTextColor(Color.RED);
                        }
                    }
                });
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_check_sql_server_connect)
                        .setView(sqlDialogView)
                        .create();
            default:
                return null;
        }
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("NumberPickerTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("NumberPickerTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("NumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

}
