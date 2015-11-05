package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_autoComplete_teachersBase;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_Image;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.databases.DataBasesRegist;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

public class Dialog_Fragment extends android.support.v4.app.DialogFragment {

    private Context context;
    private int dialog_id;
    private String cntx;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LayoutInflater inflater;

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
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        switch (dialog_id){
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
                final ArrayList<String> items = db.readSQL();
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
                autoCompleteTextView.setAdapter(new adapter_autoComplete_teachersBase(context, items));
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
                        String pos = split[4];

                        String gender;
                        if (lastname.substring(lastname.length() - 1).equals("а")) {
                            gender = "Ж";
                        } else {
                            gender = "М";
                        }


                        Loader_Image loader_image = new Loader_Image(context, new String[]{surname, name, lastname, kaf, gender, pos}, Dialog_Fragment.this, (UpdateTeachers) getTargetFragment());
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
                                String pos = "-1";
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

                                UpdateTeachers updateTeachers;
                                updateTeachers = (UpdateTeachers) getTargetFragment();
                                Loader_Image loader_image = new Loader_Image(context, new String[]{surname, name, lastname, kaf, gender, pos}, Dialog_Fragment.this, updateTeachers);
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

                final View dialogView = inflater.inflate(R.layout.layout_dialog_edit, null);
                final TableLayout tableLayout = (TableLayout) dialogView.findViewById(R.id.layout_dialog_edit_table);
                final ImageView imageView = (ImageView) dialogView.findViewById(R.id.layout_dialog_edit_image_person);
                final String [] values = getArguments().getStringArray("valuesForEdit");
                final DataBases dbses = new DataBases(context);

                final EditText editSurname = (EditText) dialogView.findViewById(R.id.editSurname);
                final EditText editName = (EditText) dialogView.findViewById(R.id.editName);
                final EditText editLastname = (EditText) dialogView.findViewById(R.id.editLastname);
                final EditText editKaf = (EditText) dialogView.findViewById(R.id.editKaf);

                try {
                    assert values != null;
                    editSurname.setText(values[0]);
                    editName.setText(values[1]);
                    editLastname.setText(values[2]);
                    editKaf.setText(values[3]);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }


                try {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    if (!imageLoader.isInited()){
                        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                    }
                    imageLoader.displayImage("file://" + values[5], imageView);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builderEdit = new AlertDialog.Builder(getActivity());
                builderEdit.setTitle("Редактирование");
                builderEdit.setView(dialogView);
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
                        String[] edited = new String[4];

                        edited[0] = editSurname.getText().toString();
                        edited[1] = editName.getText().toString();
                        edited[2] = editLastname.getText().toString();
                        edited[3] = editKaf.getText().toString();

                        dbses.updateTeachersDB(source, edited);
                        dbses.closeDBconnection();


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
                final String ident = getArguments().getString("AudOrPer");
                final int mSelectedItemAud = sharedPreferences.getInt(Values.COLUMNS_AUD_COUNT, 0);
                final int mSelectedItemPer = sharedPreferences.getInt(Values.COLUMNS_PER_COUNT, 0);
                final NumberPicker numberPicker = new NumberPicker(context);
                numberPicker.setMinValue(2);
                numberPicker.setMaxValue(5);
                numberPicker.setGravity(Gravity.CENTER);
                if (ident != null && ident.equalsIgnoreCase("aud")) {
                    numberPicker.setValue(mSelectedItemAud);
                } else if (ident != null && ident.equalsIgnoreCase("per")) {
                    numberPicker.setValue(mSelectedItemPer);
                }
                return new AlertDialog.Builder(getActivity(), R.style.Base_Theme_AppCompat_Dialog_Alert)
                        .setTitle(getResources().getString(R.string.dialog_columns_title))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ident != null && ident.equalsIgnoreCase("aud")) {
                                    editor.putInt(Values.COLUMNS_AUD_COUNT, numberPicker.getValue());
                                    editor.commit();
                                    UpdateJournal updateJournal = (UpdateJournal) getTargetFragment();
                                    updateJournal.onDone();
                                } else if (ident != null && ident.equalsIgnoreCase("per")) {
                                    editor.putInt(Values.COLUMNS_PER_COUNT, numberPicker.getValue());
                                    editor.commit();
                                    UpdateTeachers updateTeachers = (UpdateTeachers) getTargetFragment();
                                    updateTeachers.onFinishEditing();
                                }
                                dialog.cancel();
                            }
                        })
                        .setView(numberPicker)
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
            default:
                return null;
        }

    }

}
