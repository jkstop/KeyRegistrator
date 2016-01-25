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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.lang.reflect.Field;

public class Dialog_Fragment extends DialogFragment{

    private Context context;
    private int dialog_id;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LayoutInflater mInflater;
    private Resources mResources;
    private boolean pin_verify;

    DialogInterface.OnDismissListener onDismissListener;


    public Dialog_Fragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog_id = getArguments().getInt(Values.DIALOG_TYPE,0);
        context = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = context.getResources();
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
                                DataBaseJournal dbJournal = new DataBaseJournal(context);
                                dbJournal.clearJournalDB();
                                dbJournal.closeDB();

                                UpdateInterface listen = (UpdateInterface)getTargetFragment();
                                listen.updateInformation();

                                Toast.makeText(context,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
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
                                DataBaseFavorite dbFavorite = new DataBaseFavorite(context);
                                dbFavorite.clearTeachersDB();
                                dbFavorite.closeDB();

                                UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                updateInterface.updateInformation();

                                Toast.makeText(context,mResources.getString(R.string.done),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.DIALOG_EDIT:

                final View dialogView = mInflater.inflate(R.layout.layout_person_information, null);
                final ImageView personImage = (ImageView) dialogView.findViewById(R.id.person_information_image);
                final String [] values = getArguments().getStringArray("valuesForEdit");
                final DataBaseFavorite dbFavorite = new DataBaseFavorite(context);

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
                        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
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
                                DataBaseRooms dbRooms = new DataBaseRooms(context);
                                dbRooms.deleteFromRoomsDB(aud);
                                dbRooms.closeDB();

                                editor.remove(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud);
                                editor.remove(Values.POSITION_IN_BASE_FOR_ROOM + aud);
                                editor.remove(Values.POSITION_IN_LIST_FOR_ROOM + aud);
                                editor.commit();

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
                                DataBaseRooms dbRooms = new DataBaseRooms(context);
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
                            DataBaseRooms dbRooms = new DataBaseRooms(context);
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
                final int mSelectedItemAud = sharedPreferences.getInt(Values.COLUMNS_AUD_COUNT, 0);
                final int mSelectedItemPer = sharedPreferences.getInt(Values.COLUMNS_PER_COUNT, 0);
                LayoutInflater layoutInflater1 = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View pickerView  = layoutInflater1.inflate(R.layout.view_column_selector,null);
                final NumberPicker numberPicker = (NumberPicker)pickerView.findViewById(R.id.select_column_number_picker);
                setNumberPickerTextColor(numberPicker,Color.BLACK);
                numberPicker.setMaxValue(5);
                numberPicker.setMinValue(2);
                if (ident != null && ident.equalsIgnoreCase("aud")) {
                    numberPicker.setValue(mSelectedItemAud);
                } else if (ident != null && ident.equalsIgnoreCase("per")) {
                    numberPicker.setValue(mSelectedItemPer);
                }
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.dialog_columns_title))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ident != null && ident.equalsIgnoreCase("aud")) {
                                    editor.putInt(Values.COLUMNS_AUD_COUNT, numberPicker.getValue());
                                    editor.commit();
                                    UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
                                    updateInterface.updateInformation();
                                } else if (ident != null && ident.equalsIgnoreCase("per")) {
                                    editor.putInt(Values.COLUMNS_PER_COUNT, numberPicker.getValue());
                                    editor.commit();
                                    UpdateTeachers updateTeachers = (UpdateTeachers) getTargetFragment();
                                    updateTeachers.onFinishEditing();
                                }
                                dialog.cancel();
                            }
                        })
                        .setView(pickerView)
                        .create();
            case Values.DIALOG_SEEKBARS:

                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                String[] seekItems = getResources().getStringArray(R.array.seek_items);
                TableLayout tableLayout1 = new TableLayout(context);
                for (int i = 0; i < seekItems.length; i++) {
                    TableRow tableRow = new TableRow(context);
                    View row = layoutInflater.inflate(R.layout.layout_dialog_items_size, null);
                    TextView textR = (TextView) row.findViewById(R.id.text_seek_row);
                    textR.setText(seekItems[i]);

                    SeekBar seek = (SeekBar) row.findViewById(R.id.seek_seek_row);
                    if (i == 0) {
                        seek.setProgress((int) (sharedPreferences.getFloat(Values.GRID_SIZE, (float) 0.45) * 100));
                        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            float prog = 0;

                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                prog = (float) (progress / 100.0);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                editor.putFloat(Values.GRID_SIZE, prog);
                                editor.commit();
                            }
                        });
                    } else if (i == 1) {
                        seek.setMax(50);
                        seek.setProgress((int) (sharedPreferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15) * 100));
                        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            float prog = 0;

                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                prog = (float) (progress / 100.0);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                editor.putFloat(Values.DISCLAIMER_SIZE, prog);
                                editor.commit();
                            }
                        });
                    } else if (i == 2) {
                        seek.setMax(50);
                        seek.setProgress((int) (sharedPreferences.getFloat(Values.JOURNAL_SIZE, (float) 0.3) * 100));
                        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            float prog = 0;

                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                prog = (float) (progress / 100.0);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                editor.putFloat(Values.JOURNAL_SIZE, prog);
                                editor.commit();
                            }
                        });
                    }
                    tableRow.addView(row);
                    tableLayout1.addView(tableRow);
                }
                tableLayout1.setColumnStretchable(0, true);

                return new AlertDialog.Builder(getActivity())
                        .setView(tableLayout1)
                        .setTitle("Размер элементов")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UpdateMainFrame updateMainFrame = (UpdateMainFrame) getTargetFragment();
                                updateMainFrame.onFinish();
                                dialog.cancel();
                            }
                        })
                        .create();
            case Values.DIALOG_RESIZE_ITEMS:
                View rootView =  mInflater.inflate(R.layout.view_resize_main_fragment_items,null);
                CardView cardView = (CardView)rootView.findViewById(R.id.layout_main_fragment_disclaimer_card);
                cardView.getLayoutParams().height=200;
                return new AlertDialog.Builder(getActivity())
                        .setTitle("Resize")
                        .setView(rootView)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

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
                                DataBaseJournal dbJournal = new DataBaseJournal(context);
                                DataBaseRooms dbRooms = new DataBaseRooms(context);
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
                LayoutInflater inflater_sql = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View sqlDialogView = inflater_sql.inflate(R.layout.layout_dialog_sql_connect,null);
                final EditText inputServer = (EditText)sqlDialogView.findViewById(R.id.dialog_sql_connect_input_server);
                final EditText inputUser = (EditText)sqlDialogView.findViewById(R.id.dialog_sql_connect_input_user);
                final EditText inputPassowrd = (EditText)sqlDialogView.findViewById(R.id.dialog_sql_connect_input_password);
                final TextView connectionStatus = (TextView)sqlDialogView.findViewById(R.id.dialog_sql_connect_connection_status);
                Button mCheckButton = (Button)sqlDialogView.findViewById(R.id.dialog_sql_connect_check_button);

                inputServer.setText(sharedPreferences.getString(Values.SQL_SERVER,""));
                inputUser.setText(sharedPreferences.getString(Values.SQL_USER,""));
                inputPassowrd.setText(sharedPreferences.getString(Values.SQL_PASSWORD,""));
                if (sharedPreferences.getInt(Values.SQL_STATUS,0)==Values.SQL_STATUS_CONNECT){
                    connectionStatus.setText(R.string.connected);
                    connectionStatus.setTextColor(Color.GREEN);
                }else{
                    connectionStatus.setText(R.string.disconnected);
                    connectionStatus.setTextColor(Color.RED);
                }
                mCheckButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SQL_Connector.check_sql_connection(context,inputServer.getText().toString(),inputUser.getText().toString(),inputPassowrd.getText().toString())){
                            connectionStatus.setText(R.string.connected);
                            connectionStatus.setTextColor(Color.GREEN);

                            editor.putString(Values.SQL_SERVER,inputServer.getText().toString());
                            editor.putString(Values.SQL_USER,inputUser.getText().toString());
                            editor.putString(Values.SQL_PASSWORD, inputPassowrd.getText().toString());
                            editor.apply();
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
