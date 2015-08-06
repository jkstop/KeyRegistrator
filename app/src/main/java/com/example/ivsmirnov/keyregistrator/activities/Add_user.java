package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_Image;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.Adapter_SQL_popup;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 29.07.2015.
 */
public class Add_user extends ActionBarActivity {

    private AutoCompleteTextView autoCompleteTextView;
    private Button buttonOK;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_activity);

        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.input_users);
        buttonOK = (Button)findViewById(R.id.button_add_user);
        context = this;


        DataBases db = new DataBases(context);
        ArrayList<String> items = db.readSQL();
        db.closeDBconnection();

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

                //Loader_Image loader_image = new Loader_Image(context,new String[]{surname,name,lastname,kaf,gender},Add_user.this,onImageLoaded);
                //loader_image.execute();


            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                //Loader_Image loader_image = new Loader_Image(context,new String[]{surname,name,lastname,kaf,gender},Add_user.this,onImageLoaded);
                //loader_image.execute();
            }
        });
    }


}
