package com.example.ivsmirnov.keyregistrator;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by ivsmirnov on 25.06.2015.
 */
public class Dialog_Fragment extends android.support.v4.app.DialogFragment {

    private Context context;
    private int dialog_id;

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
        context = getActivity().getApplicationContext();

    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        switch (dialog_id){
            case Values.DIALOG_SEEKBAR:
                final SeekBar seekBar = new SeekBar(getActivity());
                seekBar.setMax(40);
                seekBar.setProgress((int) (preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15)*100));
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

                        DataBases db = new DataBases(context);

                        //Loader_Image loader_image = new Loader_Image(context,new String[]{surname,name,lastname,kaf},null);
                        //loader_image.execute();

                        //String photo = db.writeCardInBase(surname, name, lastname, kaf);
                        db.writeInDBTeachers(surname, name, lastname, kaf, gender, "preload");
                        db.closeDBconnection();

                        String [] ed = new String[]{surname,name,lastname,kaf,gender};
                        EditDialogListener dialogListener = (EditDialogListener)getActivity();
                        dialogListener.onFinishEditDialog(ed, 0, 2);
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

                                DataBases db = new DataBases(context);
                                //String photo = db.writeCardInBase(surname, name, lastname, kaf);
                                //db.writeInDBTeachers(surname, name, lastname, kaf, gender,photo);
                                db.closeDBconnection();
                                String [] ed = new String[]{surname,name,lastname,kaf,gender};
                                EditDialogListener dialogListener = (EditDialogListener)getActivity();
                                dialogListener.onFinishEditDialog(ed, 0, 2);
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

                final DataBases dbses = new DataBases(context);
                for (int i=1;i<6;i++){
                    TableRow tableRow = new TableRow(context);
                    View rowLayot = inflater.inflate(R.layout.row_for_editor,null);

                    TextView textParametr = (TextView)rowLayot.findViewById(R.id.textEditParametr);
                    textParametr.setText(dbses.cursorTeachers.getColumnName(i));

                    EditText editParametr = (EditText)rowLayot.findViewById(R.id.editParemetr);
                    String [] values = getArguments().getStringArray("valuesForEdit");
                    editParametr.setText(values[i-1]);

                    tableRow.addView(rowLayot);
                    tableLayout.addView(tableRow);
                }

                tableLayout.setColumnStretchable(0, true);


                AlertDialog.Builder builderEdit = new AlertDialog.Builder(getActivity());
                builderEdit.setTitle("Редактирование");
                builderEdit.setView(tableLayout);
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
                        EditDialogListener activity = (EditDialogListener)getActivity();
                        activity.onFinishEditDialog(edited,position,1);

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
        }

        return null;
    }

    public interface EditDialogListener{
        void onFinishEditDialog(String [] values, int position, int type);
    }


}
