package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Add_user;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_Image;
import com.example.ivsmirnov.keyregistrator.databases.DataBasesRegist;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.adapters.Adapter_SQL_popup;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivsmirnov on 25.06.2015.
 */
public class Dialog_Fragment extends android.support.v4.app.DialogFragment {

    private Context context;
    private int dialog_id;
    private String cntx;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    DialogInterface.OnDismissListener onDismissListener;


    public Dialog_Fragment(){
    }
/*
    public Dialog_Fragment(Context c,int id){
        context = c;
        dialog_id = id;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog_id = getArguments().getInt(Values.DIALOG_TYPE,0);
        cntx = getArguments().getString("cntx", "nicht");
        context = getActivity().getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();


    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        switch (dialog_id){
            case Values.DIALOG_SEEKBAR:
                final SeekBar seekBar = new SeekBar(getActivity());
                seekBar.setMax(40);
                seekBar.setProgress((int) (sharedPreferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15)*100));
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float prog = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        prog = (float) (progress/100.0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        editor.putFloat(Values.DISCLAIMER_SIZE,prog);
                        editor.commit();
                    }
                });

                return new AlertDialog.Builder(getActivity())
                        .setTitle("Размер уведомления")
                        .setView(seekBar)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UpdateMainFrame updateMainFrame = (UpdateMainFrame)getTargetFragment();
                                updateMainFrame.onFinish();
                                dialog.cancel();
                            }
                        })
                        .create();
            case Values.DIALOG_CLEAR_JOURNAL:
                return new AlertDialog.Builder(getActivity())
                        .setTitle("ВНИМАНИЕ!")
                        .setMessage("Из журнала будут удалены все записи. Продолжить?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBases db = new DataBases(context);
                                db.clearJournalDB();
                                db.closeDBconnection();
                                UpdateJournal listen = (UpdateJournal)getTargetFragment();
                                listen.onDone();


                                Toast.makeText(context,"Готово!",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.DIALOG_CLEAR_TEACHERS:
                return new AlertDialog.Builder(getActivity())
                        .setTitle("ВНИМАНИЕ!")
                        .setMessage("Из базы данных преподавателей будут удалены все записи. Продолжить?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBases db = new DataBases(context);
                                db.clearTeachersDB();
                                db.closeDBconnection();
                                UpdateTeachers updateTeachers = (UpdateTeachers)getTargetFragment();
                                updateTeachers.onFinishEditing();
                                Toast.makeText(context,"Готово!",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
            case Values.INPUT_DIALOG:
                DataBases db = new DataBases(context);
                ArrayList <String> items = db.readSQL();
                db.closeDBconnection();
                final AutoCompleteTextView autoCompleteTextView =  new AutoCompleteTextView(context);
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                autoCompleteTextView.setAdapter(new Adapter_SQL_popup(context, items));
                autoCompleteTextView.setTextColor(Color.BLACK);
                autoCompleteTextView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String source = autoCompleteTextView.getText().toString();
                        String[] split = source.split(";");
                        String surname = split[0];
                        String name = split[1];
                        String lastname = split[2];
                        String kaf = split[3];

                        String gender;
                        if (lastname.substring(lastname.length() - 1).equals("а")) {
                            gender = "Ж";
                        } else {
                            gender = "М";
                        }


                        Loader_Image loader_image = new Loader_Image(context,new String[]{surname,name,lastname,kaf,gender},Dialog_Fragment.this,(UpdateTeachers)getTargetFragment());
                        loader_image.execute();


                        dismiss();

                    }
                });
                AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
                        builder.setTitle("Ввод")
                        .setView(autoCompleteTextView)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String source = autoCompleteTextView.getText().toString();
                                String[] split = source.split("\\s+");
                                String surname = "";
                                String name = "";
                                String lastname = "";
                                String kaf = "";
                                String gender = "";
                                try {
                                    surname = split[0];
                                    name = split[1];
                                    if (split.length > 2) {
                                        lastname = split[2];
                                        if (lastname.substring(lastname.length() - 1).equals("а")) {
                                            gender = "Ж";
                                        } else {
                                            gender = "М";
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Loader_Image loader_image = new Loader_Image(context,new String[]{surname,name,lastname,kaf,gender},Dialog_Fragment.this,(UpdateTeachers)getTargetFragment());
                                loader_image.execute();
                               dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.gravity = Gravity.TOP;
                return dialog;
            case Values.DIALOG_EDIT:
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final TableLayout tableLayout = new TableLayout(context);
                final String [] values = getArguments().getStringArray("valuesForEdit");
                final DataBases dbses = new DataBases(context);
                for (int i=1;i<6;i++){
                    TableRow tableRow = new TableRow(context);
                    View rowLayot = inflater.inflate(R.layout.row_for_editor,null);

                    TextView textParametr = (TextView)rowLayot.findViewById(R.id.textEditParametr);
                    textParametr.setText(dbses.cursorTeachers.getColumnName(i));

                    EditText editParametr = (EditText)rowLayot.findViewById(R.id.editParemetr);
                    //String [] values = getArguments().getStringArray("valuesForEdit");
                    if (values!=null){
                        editParametr.setText(values[i-1]);
                    }


                    tableRow.addView(rowLayot);
                    tableLayout.addView(tableRow);
                }

                tableLayout.setColumnStretchable(0, true);


                AlertDialog.Builder builderEdit = new AlertDialog.Builder(getActivity());
                builderEdit.setTitle("Редактирование");
                builderEdit.setView(tableLayout);
                builderEdit.setNeutralButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (values!=null){
                            dbses.deleteFromTeachersDB(values[0], values[1], values[2], values[3]);
                            dbses.closeDBconnection();
                        }
                        UpdateTeachers updateTeachers = (UpdateTeachers)getTargetFragment();
                        updateTeachers.onFinishEditing();
                    }
                });
                builderEdit.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dbses.closeDBconnection();
                    }
                });
                builderEdit.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String [] source = getArguments().getStringArray("valuesForEdit");
                        String [] edited = new String[5];
                        for (int i = 0; i < tableLayout.getChildCount(); i++) {
                            LinearLayout linearLayout = (LinearLayout)tableLayout.getChildAt(i);
                            EditText editText = (EditText)linearLayout.findViewById(R.id.editParemetr);
                            String text = editText.getText().toString();
                            edited[i] = text;
                        }
                        dbses.updateTeachersDB(source, edited);
                        dbses.closeDBconnection();

                        int position = getArguments().getInt("position");
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
            case 67:
                ImageView imageView = new ImageView(context);

                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard,"str.txt");
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line;


                    while ((line=bufferedReader.readLine())!= null){
                        text.append(line);
                    }

                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d("length", String.valueOf(text.toString().length()));



                byte[] decodedString = Base64.decode(text.toString(), Base64.NO_PADDING);
                Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(image);


                return new AlertDialog.Builder(getActivity())
                        .setTitle("Image")
                        .setView(imageView)
                        .create();
            case Values.DELETE_ROOM_DIALOG:
                final int pos = getArguments().getInt("pos");
                final int aud = getArguments().getInt("aud");
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.dialog_delete_room_title))
                        .setMessage(getResources().getString(R.string.dialog_delete_room_message) + " "+ aud + "?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBases db = new DataBases(context);
                                db.cursorRoom.moveToPosition(pos);
                                db.deleteFromRoomsDB(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud,
                                        db.cursorRoom.getInt(db.cursorRoom.getColumnIndex(DataBasesRegist._ID))));
                                db.closeDBconnection();

                                editor.remove(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud);
                                editor.remove(Values.POSITION_IN_BASE_FOR_ROOM + aud);
                                editor.remove(Values.POSITION_IN_LIST_FOR_ROOM + aud);
                                editor.commit();

                                UpdateJournal updateJournal = (UpdateJournal) getTargetFragment();
                                updateJournal.onDone();
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
            case Values.ADD_ROOM_DIALOG:
                final EditText editText = new EditText(context);
                editText.setGravity(Gravity.CENTER);
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setTextColor(Color.BLACK);
                return new AlertDialog.Builder(getActivity())
                        .setTitle("Добавить")
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int item = Integer.parseInt(editText.getText().toString());
                                DataBases db = new DataBases(context);
                                db.writeInRoomsDB(item);
                                db.closeDBconnection();
                                editText.setText("");

                                UpdateJournal updateJournal = (UpdateJournal) getTargetFragment();
                                updateJournal.onDone();

                                dialog.cancel();
                            }
                        })
                        .create();
            case Values.SELECT_COLUMNS_DIALOG:
                final String [] mItems = getResources().getStringArray(R.array.dialog_columns_items);
                final int mSelectedItem = sharedPreferences.getInt(Values.COLUMNS_COUNT,0)-2;
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.dialog_columns_title))
                        .setSingleChoiceItems(mItems, mSelectedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putInt(Values.COLUMNS_COUNT, Integer.parseInt(String.valueOf(mItems[which].charAt(0))));
                                editor.commit();

                                UpdateJournal updateJournal = (UpdateJournal) getTargetFragment();
                                updateJournal.onDone();
                                dialog.cancel();
                            }
                        })

                        .create();

        }

        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


}
