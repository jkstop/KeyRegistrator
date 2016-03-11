package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Build;
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
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_main_auditrooms_grid_resize;
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetPersons;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.items.CloseRoomsParams;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;

import jxl.biff.drawing.CheckBox;

public class Dialog_Fragment extends DialogFragment{

    private Context mContext;
    private int dialog_id;
    private Settings mSettings;
    private LayoutInflater mInflater;
    private Resources mResources;
    private DataBaseFavorite mDataBaseFavorite;
    private DataBaseJournal mDataBaseJournal;
    private DataBaseRooms mDataBaseRooms;

    public ArrayList<String> mAttachmentList;


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

        if (dialog_id == Values.DIALOG_CLEAR_JOURNAL){

            if (Launcher.mDataBaseJournal!=null){
                mDataBaseJournal = Launcher.mDataBaseJournal;
            }else{
                mDataBaseJournal = new DataBaseJournal(mContext);
            }
        } else if (dialog_id == Values.ADD_ROOM_DIALOG |
                dialog_id == Values.DIALOG_CLEAR_ROOMS |
                dialog_id == Values.DELETE_ROOM_DIALOG |
                dialog_id == Values.DIALOG_RESIZE_ITEMS){

            if (Launcher.mDataBaseRooms!=null){
                mDataBaseRooms = Launcher.mDataBaseRooms;
            } else {
                mDataBaseRooms = new DataBaseRooms(mContext);
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

                                mDataBaseJournal.clearJournalDB();

                                updateInformation();

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

                                DataBaseFavorite.clearTeachersDB();

                                updateInformation();

                                Toast.makeText(mContext,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.DIALOG_EDIT:

                final View dialogView = mInflater.inflate(R.layout.layout_person_information, null);
                final ImageView personImage = (ImageView) dialogView.findViewById(R.id.person_information_image);

                final TextInputLayout inputLastname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_lastname_layout);
                final TextInputLayout inputFirstname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_firstname_layout);
                final TextInputLayout inputMidname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_midname_layout);
                final TextInputLayout inputDivision = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_division_layout);
                AppCompatCheckBox accessType = (AppCompatCheckBox) dialogView.findViewById(R.id.person_information_access_type);
                final String tag = getArguments().getString(Values.DIALOG_PERSON_INFORMATION_KEY_TAG);

                //получаем пользователя и заполняем поля
                new GetPersons(mContext, null, null).execute(new GetPersonParams()
                        .setPersonLocation(DataBaseFavorite.LOCAL_USER)
                        .setPersonPhotoDimension(DataBaseFavorite.FULLSIZE_PHOTO)
                        .setPersonTag(tag)
                        .setPersonImageView(personImage)
                        .setPersonLastname(inputLastname.getEditText())
                        .setPersonFirstname(inputFirstname.getEditText())
                        .setPersonMidname(inputMidname.getEditText())
                        .setAccessTypeContainer(accessType)
                        .setFreeUser(mSettings.getFreeUsers().contains(tag))
                        .setPersonDivision(inputDivision.getEditText()));

                accessType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) mSettings.addFreeUser(tag);
                                else mSettings.deleteFreeUser(tag);
                    }
                });


                AlertDialog.Builder builderEdit = new AlertDialog.Builder(getActivity());
                builderEdit.setView(dialogView);

                if (DataBaseFavorite.isUserInBase(tag)){
                    builderEdit.setNeutralButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (tag!=null){


                                //new DataBaseFavorite.deleteUser(mContext).execute(tag);
                                DataBaseFavorite.deleteUser(tag);

                                //удаление метки в free_users
                                mSettings.deleteFreeUser(tag);

                                updateInformation();
                            }
                        }
                    });
                    builderEdit.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builderEdit.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            DataBaseFavorite.updatePersonItem(tag, new PersonItem()
                                    .setLastname(inputLastname.getEditText().getText().toString())
                                    .setFirstname(inputFirstname.getEditText().getText().toString())
                                    .setMidname(inputMidname.getEditText().getText().toString())
                                    .setDivision(inputDivision.getEditText().getText().toString()));

                            updateInformation();
                        }
                    });
                }else{
                    builderEdit.setNeutralButton(getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //DataBaseFavorite.writeInDBTeachers(mContext, new PersonItem().setLastname(personItem.getLastname())
                            //        .setFirstname(personItem.getFirstname())
                            //        .setMidname(personItem.getMidname())
                            //        .setDivision(personItem.getDivision())
                                    //.setPhotoOriginal(valuesForEdits.get(Values.DIALOG_PERSON_INFORMATION_KEY_PHOTO_ORIGINAL))
                                    //.setPhotoPreview(DataBaseFavorite.getPhotoPreview(valuesForEdits.get(Values.DIALOG_PERSON_INFORMATION_KEY_PHOTO_ORIGINAL)))
                            //        .setSex(personItem.getSex())
                            //        .setRadioLabel(personItem.getRadioLabel()));
                            updateInformation();
                        }
                    });
                }


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

                                mDataBaseRooms.deleteFromRoomsDB(aud);

                                updateInformation();

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

                                mDataBaseRooms.clearRoomsDB();

                                updateInformation();
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

                            mDataBaseRooms.writeInRoomsDB(new RoomItem().setAuditroom(inputText)
                                    .setStatus(Values.ROOM_IS_FREE)
                                    .setAccessType(DataBaseJournal.ACCESS_BY_CLICK));

                            enterAuditroomText.getText().clear();
                            updateInformation();
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
                                    updateInformation();
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

                ArrayList <RoomItem> mRoomsItems = mDataBaseRooms.readRoomsDB();

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

                                updateInformation();
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
                                new CloseRooms(mContext).execute(new CloseRoomsParams()
                                        .setTag(getArguments().getString("tag"))
                                        .setRoomInterface(Main_Fragment.roomInterface));
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
                View dialogLayout = mInflater.inflate(R.layout.layout_dialog_sql_connect_new,null);
                final AppCompatEditText inputServer = (AppCompatEditText) ((TextInputLayout)dialogLayout.findViewById(R.id.layout_dialog_sql_new_input_server)).getEditText();
                final AppCompatEditText inputLogin = (AppCompatEditText) ((TextInputLayout)dialogLayout.findViewById(R.id.layout_dialog_sql_new_input_login)).getEditText();
                final AppCompatEditText inputPassword = (AppCompatEditText) ((TextInputLayout)dialogLayout.findViewById(R.id.layout_dialog_sql_new_input_password)).getEditText();
                final ImageView serverStatusImage = (ImageView)dialogLayout.findViewById(R.id.layout_dialog_sql_new_image_status);
                final TextView serverStatusText = (TextView)dialogLayout.findViewById(R.id.layout_dialog_sql_new_text_status);
                final ImageView serverCheckConnection = (ImageView)dialogLayout.findViewById(R.id.layout_dialog_sql_new_image_reconnect);

                final ServerConnectionItem serverConnectionItem = mSettings.getServerConnectionParams();

                inputServer.setText(serverConnectionItem.getServerName());
                inputLogin.setText(serverConnectionItem.getUserName());
                inputPassword.setText(serverConnectionItem.getUserPassword());

                if (mSettings.getServerStatus()){
                    serverStatusImage.setImageResource(R.drawable.ic_cloud_done_black_48dp);
                    serverStatusText.setText(R.string.dialog_sql_server_connected);
                    serverStatusText.setTextColor(getResources().getColor(R.color.primary));
                } else {
                    serverStatusImage.setImageResource(R.drawable.ic_cloud_off_black_48dp);
                    serverStatusText.setText(R.string.dialog_sql_server_disconnected);
                    serverStatusText.setTextColor(getResources().getColor(R.color.colorAccent));
                }

                serverCheckConnection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //анимация поворота
                        Animation rotationAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
                        rotationAnim.setRepeatCount(Animation.INFINITE);

                        serverCheckConnection.startAnimation(rotationAnim);

                        //если android по 4.4 включительно, то включаем программное ускорение
                        //иначе анимация не работает
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
                            serverCheckConnection.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        }

                        ServerConnectionItem newServerConnectionItem = new ServerConnectionItem()
                                .setServerName(inputServer.getText().toString())
                                .setUserName(inputLogin.getText().toString())
                                .setUserPassword(inputPassword.getText().toString());
                        try {
                            SQL_Connection.SQLconnect = null;
                            new SQL_Connection(mContext, newServerConnectionItem, new SQL_Connection.SQL_Connection_interface() {
                                @Override
                                public void onServerConnected() {
                                    serverCheckConnection.clearAnimation();
                                    serverStatusImage.setImageResource(R.drawable.ic_cloud_done_black_48dp);
                                    serverStatusText.setText(R.string.dialog_sql_server_connected);
                                    serverStatusText.setTextColor(getResources().getColor(R.color.primary));
                                }

                                @Override
                                public void onServerDisconnected() {
                                    serverCheckConnection.clearAnimation();
                                    serverStatusImage.setImageResource(R.drawable.ic_cloud_off_black_48dp);
                                    serverStatusText.setText(R.string.dialog_sql_server_disconnected);
                                    serverStatusText.setTextColor(getResources().getColor(R.color.colorAccent));
                                }

                                @Override
                                public void onServerConnectException(Exception e) {
                                    if (e.getLocalizedMessage().contains("Unable to resolve host")) {
                                        inputServer.setError(e.getLocalizedMessage());
                                    } else if (e.getLocalizedMessage().contains("Ошибка входа пользователя")) {
                                        inputLogin.setError(e.getLocalizedMessage());
                                        inputPassword.setError(e.getLocalizedMessage());
                                    }
                                }
                            }).execute();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                return new AlertDialog.Builder(getActivity())
                        .setView(dialogLayout)
                        .setTitle(R.string.title_check_sql_server_connect)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ServerConnectionItem newServerConnectionItem = new ServerConnectionItem()
                                        .setServerName(inputServer.getText().toString())
                                        .setUserName(inputLogin.getText().toString())
                                        .setUserPassword(inputPassword.getText().toString());
                                mSettings.setServerConnectionParams(newServerConnectionItem);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
            case Values.DIALOG_LOG_OUT:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.title_dialog_log_out))
                        .setMessage(getString(R.string.dialog_log_out_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Get_Account_Information_Interface get_account_information_interface =
                                        (Get_Account_Information_Interface)getActivity();
                                get_account_information_interface.onChangeAccount();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
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

    private void updateInformation(){
        UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
        updateInterface.updateInformation();
    }


}
