package com.example.ivsmirnov.keyregistrator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity extends Activity {

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_sql_activity);

        context = this;

        DataBases db = new DataBases(context);
        ArrayList <String> name = db.readFromBaseSQL(DataBasesRegist.COLUMN_IMYA);
        ArrayList <String> surname = db.readFromBaseSQL(DataBasesRegist.COLUMN_FAMILIA);
        ArrayList <String> lastname = db.readFromBaseSQL(DataBasesRegist.COLUMN_OTCHESTVO);
        ArrayList <String> kaf = db.readFromBaseSQL(DataBasesRegist.COLUMN_KAF);
        //ArrayList <String> photo = db.readFromBaseSQL(DataBasesRegist.COLUMN_FOTO);

        db.closeDBconnection();

        GridView gridView = (GridView)findViewById(R.id.grid_for_base_sql);
        gridView.setAdapter(new base_sql_activity_adapter(context,kaf,name,surname,lastname));
    }
}
