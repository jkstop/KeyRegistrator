package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
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
import com.example.ivsmirnov.keyregistrator.adapters.AdapterMainRoomGridResizer;
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetPersons;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.Updatable;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Dialogs extends DialogFragment{

    public static final String DIALOG_TYPE = "dialog_type";
    public static final String DIALOG_ENTER_PASSWORD_TYPE = "dialog_enter_password_type";
    public static final String DIALOG_PERSON_INFORMATION_KEY_TAG = "DIALOG_PERSON_INFORMATION_KEY_TAG";
    public static final String DIALOG_PERSON_INFORMATION_KEY_POSITION = "DIALOG_PERSON_INFORMATION_KEY_POSITION";

    public static final int DIALOG_EDIT = 100;
    public static final int DIALOG_CLEAR_JOURNAL = 101;
    public static final int DIALOG_CLEAR_TEACHERS = 102;
    public static final int DIALOG_CLEAR_ROOMS = 103;
    public static final int DIALOG_SQL_CONNECT = 104;
    public static final int DELETE_ROOM_DIALOG = 105;
    public static final int ADD_ROOM_DIALOG = 106;
    public static final int SELECT_COLUMNS_DIALOG = 107;
    public static final int DIALOG_RESIZE_ITEMS = 108;
    public static final int DIALOG_ENTER_PASSWORD = 109;
    public static final int DIALOG_LOG_OUT = 110;

    public static final int DIALOG_ENTER_PASSWORD_TYPE_ACCESS_FOR_PERSONS = 111;
    public static final int DIALOG_ENTER_PASSWORD_TYPE_CLOSE_ROOM = 112;

    private Context mContext;
    private int mDialogId;
    private Resources mResources;

    public static FrameLayout mFrameGrid;

    private int mProgress = 0;

    public Dialogs(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialogId = getArguments().getInt(DIALOG_TYPE,0);
        mContext = getActivity();
        mResources = mContext.getResources();
    }


    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        switch (mDialogId){
            case DIALOG_CLEAR_JOURNAL:
                View dialogClearJournalView = View.inflate(mContext, R.layout.view_dialog_clear,null);
                final CheckBox checkClearJournalLocal = (CheckBox)dialogClearJournalView.findViewById(R.id.view_dialog_clear_delete_local);
                final CheckBox checkClearJournalServer = (CheckBox)dialogClearJournalView.findViewById(R.id.view_dialog_clear_delete_server);

                return new AlertDialog.Builder(getActivity())
                        .setTitle(mResources.getString(R.string.dialog_clear_journal_title))
                        .setView(dialogClearJournalView)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (checkClearJournalLocal.isChecked()){
                                    JournalDB.clearJournalDB();
                                    updateInformation();
                                }

                                if (checkClearJournalServer.isChecked()){
                                    new ServerWriter().execute(ServerWriter.JOURNAL_DELETE_ALL);
                                }

                                Toast.makeText(mContext,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case DIALOG_CLEAR_TEACHERS:
                View dialogClearTeachersView = View.inflate(mContext, R.layout.view_dialog_clear, null);
                final CheckBox checkClearTeachersLocal = (CheckBox)dialogClearTeachersView.findViewById(R.id.view_dialog_clear_delete_local);
                final CheckBox checkClearTeachersServer = (CheckBox)dialogClearTeachersView.findViewById(R.id.view_dialog_clear_delete_server);
                return new AlertDialog.Builder(getActivity())
                        .setTitle(mResources.getString(R.string.dialog_clear_teachers_title))
                        .setView(dialogClearTeachersView)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (checkClearTeachersLocal.isChecked()){
                                    FavoriteDB.clearTeachersDB();
                                    updateInformation();
                                }

                                if (checkClearTeachersServer.isChecked()){
                                    new ServerWriter().execute(ServerWriter.PERSON_DELETE_ALL);
                                }

                                Toast.makeText(mContext,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case DIALOG_EDIT:

                final View dialogView = View.inflate(mContext, R.layout.layout_person_information, null);
                final ImageView personImage = (ImageView) dialogView.findViewById(R.id.person_information_image);

                final TextInputLayout inputLastname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_lastname_layout);
                final TextInputLayout inputFirstname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_firstname_layout);
                final TextInputLayout inputMidname = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_midname_layout);
                final TextInputLayout inputDivision = (TextInputLayout)dialogView.findViewById(R.id.person_information_text_division_layout);
                final AppCompatCheckBox accessType = (AppCompatCheckBox) dialogView.findViewById(R.id.person_information_access_type);
                final String tag = getArguments().getString(DIALOG_PERSON_INFORMATION_KEY_TAG);
                final int position = getArguments().getInt(DIALOG_PERSON_INFORMATION_KEY_POSITION);

                //интерфейс
                final Updatable updateInterface = (Updatable)getTargetFragment();

                //получаем пользователя и заполняем поля
                new GetPersons(mContext, null, null).execute(new GetPersonParams()
                        .setPersonLocation(FavoriteDB.LOCAL_USER)
                        .setPersonPhotoDimension(FavoriteDB.FULLSIZE_PHOTO)
                        .setPersonTag(tag)
                        .setPersonImageView(personImage)
                        .setPersonLastname(inputLastname.getEditText())
                        .setPersonFirstname(inputFirstname.getEditText())
                        .setPersonMidname(inputMidname.getEditText())
                        .setAccessTypeContainer(accessType)
                        .setFreeUser(FavoriteDB.getPersonAccessType(tag) == FavoriteDB.CLICK_USER_ACCESS)
                        .setPersonDivision(inputDivision.getEditText()));

                AlertDialog.Builder builderEdit = new AlertDialog.Builder(getActivity());
                builderEdit.setView(dialogView);

                if (FavoriteDB.isUserInBase(tag)){
                    builderEdit.setNeutralButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (tag!=null){

                                updateInterface.onUserDeleted(position);

                                FavoriteDB.deleteUser(tag);

                                //удаление с сервера
                                if (Settings.getWriteServerStatus() && Settings.getWriteTeachersStatus()){
                                    new ServerWriter(tag).execute(ServerWriter.PERSON_DELETE_ONE);
                                }

                                //updateInformation();
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

                            int access;
                            if (accessType.isChecked()){
                                access = FavoriteDB.CLICK_USER_ACCESS;
                            }else{
                                access = FavoriteDB.CARD_USER_ACCESS;
                            }

                            FavoriteDB.updatePersonItem(tag, new PersonItem()
                                    .setLastname(inputLastname.getEditText().getText().toString())
                                    .setFirstname(inputFirstname.getEditText().getText().toString())
                                    .setMidname(inputMidname.getEditText().getText().toString())
                                    .setDivision(inputDivision.getEditText().getText().toString())
                                    .setAccessType(access));

                           updateInterface.onUserChanged(tag, position);
                            //updateInformation();
                        }
                    });
                }else{
                    builderEdit.setNeutralButton(getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateInformation();
                        }
                    });
                }


                builderEdit.setCancelable(false);
                return builderEdit.create();
            case DELETE_ROOM_DIALOG:
                final String aud = getArguments().getString("aud");
                View dialogDeleteRoomView = View.inflate(mContext, R.layout.view_dialog_delete_room_item, null);
                final CheckBox deleteRoomLocalCheck = (CheckBox)dialogDeleteRoomView.findViewById(R.id.view_dialog_delete_room_item_local);
                final CheckBox deleteRoomServerCheck = (CheckBox)dialogDeleteRoomView.findViewById(R.id.view_dialog_delete_room_item_server);
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.title_dailog_delete_room_item) + " " + aud)
                        .setView(dialogDeleteRoomView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (deleteRoomLocalCheck.isChecked()){
                                    RoomDB.deleteFromRoomsDB(aud);
                                    updateInformation();
                                }

                                if (deleteRoomServerCheck.isChecked()){
                                    new ServerWriter(aud).execute(ServerWriter.ROOMS_DELETE_ONE);
                                }

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
            case DIALOG_CLEAR_ROOMS:
                View dialogEraseRoomsView = View.inflate(mContext, R.layout.view_dialog_clear, null);
                final CheckBox eraseLocalRoomsCheck = (CheckBox)dialogEraseRoomsView.findViewById(R.id.view_dialog_clear_delete_local);
                final CheckBox eraseServerRoomsCheck = (CheckBox)dialogEraseRoomsView.findViewById(R.id.view_dialog_clear_delete_server);
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_clear_rooms_title)
                        .setView(dialogEraseRoomsView)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (eraseLocalRoomsCheck.isChecked()){
                                    RoomDB.clearRoomsDB();
                                    updateInformation();
                                }

                                if (eraseServerRoomsCheck.isChecked()){
                                    new ServerWriter().execute(ServerWriter.ROOMS_DELETE_ALL);
                                }

                                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),R.string.done,Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case ADD_ROOM_DIALOG:
                //добавление нового помещения

                final TextInputLayout enterAuditroomLayout = (TextInputLayout) View.inflate(mContext, R.layout.view_enter_auditroom, null);
                final AppCompatEditText enterAuditroomText = (AppCompatEditText)enterAuditroomLayout.findViewById(R.id.view_auditroom_enter_room);
                AppCompatButton enterAuditroomOkButton = (AppCompatButton)enterAuditroomLayout.findViewById(R.id.view_auditroom_ok_button);
                enterAuditroomOkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText = enterAuditroomText.getText().toString();
                        ArrayList<String> auditroomList = RoomDB.getRoomList();
                        if (inputText.isEmpty()){
                            enterAuditroomLayout.setError(getResources().getString(R.string.input_empty_error));
                        } else if (auditroomList.contains(inputText)){
                            enterAuditroomLayout.setError(getResources().getString(R.string.input_already_exist_error));
                        } else {
                            RoomItem newRoomItem = new RoomItem().setAuditroom(inputText)
                                    .setStatus(RoomDB.ROOM_IS_FREE)
                                    .setAccessType(FavoriteDB.CLICK_USER_ACCESS);
                            RoomDB.writeInRoomsDB(newRoomItem);

                            //пишем на сервер
                            if (Settings.getWriteServerStatus() && Settings.getWriteRoomsServerStatus()){
                                new ServerWriter(newRoomItem).execute(ServerWriter.ROOMS_UPDATE);
                            }

                            enterAuditroomText.getText().clear();
                            updateInformation();
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),R.string.done,Snackbar.LENGTH_SHORT).show();
                            dismiss();
                        }

                    }
                });
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.view_enter_auditroom_title)
                        .setView(enterAuditroomLayout)
                        .create();
            case SELECT_COLUMNS_DIALOG:

                final int mSelectedItemAud = Settings.getAuditroomColumnsCount();
                View pickerView  = View.inflate(mContext, R.layout.view_column_selector, null);
                final NumberPicker numberPicker = (NumberPicker)pickerView.findViewById(R.id.select_column_number_picker);
                setNumberPickerTextColor(numberPicker,Color.BLACK);
                numberPicker.setMaxValue(5);
                numberPicker.setMinValue(2);

                numberPicker.setValue(mSelectedItemAud);

                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.dialog_columns_title))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.setAuditroomColumnsCount(numberPicker.getValue());
                                updateInformation();

                                dialog.cancel();
                            }
                        })
                        .setView(pickerView)
                        .create();

            case DIALOG_RESIZE_ITEMS:
                View rootView =  View.inflate(mContext, R.layout.view_resize_main_fragment_items, null);
                final CardView cardView = (CardView)rootView.findViewById(R.id.layout_main_fragment_disclaimer_card);
                final SeekBar mResizeSeekBar = (SeekBar)rootView.findViewById(R.id.view_resize_vertical_seekbar);
                mFrameGrid = (FrameLayout)rootView.findViewById(R.id.frame_for_grid_aud);

                final LinearLayout.LayoutParams cardViewLayoutParams = (LinearLayout.LayoutParams)cardView.getLayoutParams();
                final LinearLayout.LayoutParams frameGridParams = (LinearLayout.LayoutParams)mFrameGrid.getLayoutParams();

                RecyclerView mRoomsGrid = (RecyclerView)rootView.findViewById(R.id.main_fragment_auditroom_grid);
                mRoomsGrid.setClickable(false);
                mRoomsGrid.setLayoutManager(new GridLayoutManager(mContext, Settings.getAuditroomColumnsCount()));
                final AdapterMainRoomGridResizer mAdapter = new AdapterMainRoomGridResizer(RoomDB.readRoomsDB());
                mRoomsGrid.setAdapter(mAdapter);

                int weightCard = Settings.getDisclaimerWeight();
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
                                    Settings.setDisclaimerWeight(mResizeSeekBar.getProgress());
                                }else{
                                    Settings.setDisclaimerWeight(mProgress);
                                }

                                updateInformation();
                                dismiss();
                            }
                        })
                        .show();
            case DIALOG_ENTER_PASSWORD:

                final CloseRoomInterface mCloseRoomInterface = (CloseRoomInterface)getActivity();
                final int dialog_enter_password_type = getArguments().getInt(DIALOG_ENTER_PASSWORD_TYPE);
                final TextInputLayout textInputLayout = (TextInputLayout) View.inflate(mContext, R.layout.view_enter_password, null);
                final AppCompatEditText editPassword = (AppCompatEditText)textInputLayout.findViewById(R.id.view_enter_password_edit_text);
                AppCompatButton okButton = (AppCompatButton)textInputLayout.findViewById(R.id.view_enter_password_ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editPassword.getText().toString().equalsIgnoreCase("1212")){
                            switch (dialog_enter_password_type){
                                case DIALOG_ENTER_PASSWORD_TYPE_CLOSE_ROOM:
                                    new CloseRooms(mContext, getArguments().getString("tag"), mCloseRoomInterface).execute();
                                    dismiss();
                                    break;
                                case DIALOG_ENTER_PASSWORD_TYPE_ACCESS_FOR_PERSONS:
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(PersonsFr.PERSONS_FRAGMENT_TYPE, PersonsFr.PERSONS_FRAGMENT_SELECTOR);
                                    bundle.putString(Settings.AUDITROOM, getArguments().getString(Settings.AUDITROOM));
                                    PersonsFr persons_fr = PersonsFr.newInstance();
                                    persons_fr.setArguments(bundle);

                                    Launcher.showFragment(getActivity().getSupportFragmentManager(), persons_fr, R.string.navigation_drawer_item_persons);
                                    break;
                                default:
                                    break;
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

            case DIALOG_SQL_CONNECT:
                View dialogLayout = View.inflate(mContext, R.layout.layout_dialog_sql_connect, null);
                final AppCompatEditText inputServer = (AppCompatEditText) ((TextInputLayout)dialogLayout.findViewById(R.id.layout_dialog_sql_new_input_server)).getEditText();
                final AppCompatEditText inputLogin = (AppCompatEditText) ((TextInputLayout)dialogLayout.findViewById(R.id.layout_dialog_sql_new_input_login)).getEditText();
                final AppCompatEditText inputPassword = (AppCompatEditText) ((TextInputLayout)dialogLayout.findViewById(R.id.layout_dialog_sql_new_input_password)).getEditText();
                final ImageView serverStatusImage = (ImageView)dialogLayout.findViewById(R.id.layout_dialog_sql_new_image_status);
                final TextView serverStatusText = (TextView)dialogLayout.findViewById(R.id.layout_dialog_sql_new_text_status);
                final ImageView serverCheckConnection = (ImageView)dialogLayout.findViewById(R.id.layout_dialog_sql_new_image_reconnect);

                final ServerConnectionItem serverConnectionItem = Settings.getServerConnectionParams();

                inputServer.setText(serverConnectionItem.getServerName());
                inputLogin.setText(serverConnectionItem.getUserName());
                inputPassword.setText(serverConnectionItem.getUserPassword());

                if (Settings.getServerStatus()){
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
                            new SQL_Connection(newServerConnectionItem, new SQL_Connection.SQL_Connection_interface() {
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
                                Settings.setServerConnectionParams(newServerConnectionItem);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
            case DIALOG_LOG_OUT:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.title_dialog_log_out))
                        .setMessage(getString(R.string.dialog_log_out_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GetAccountInterface get_account__interface =
                                        (GetAccountInterface)getActivity();
                                get_account__interface.onChangeAccount();
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
                catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e){
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
