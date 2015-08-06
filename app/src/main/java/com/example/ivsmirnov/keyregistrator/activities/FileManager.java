package com.example.ivsmirnov.keyregistrator.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivsmirnov on 27.04.2015.
 */
public class FileManager extends ListActivity{

    private List<String> mPathList = null;
    private String root,rootInternal,rootBase;
    boolean isNeedChoiseButton,isBase;
    int what;


    private static SharedPreferences.Editor editor;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);


        editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        root = Environment.getExternalStorageDirectory().getPath();
        rootInternal = "/";
        rootBase = "/data/data/" + getPackageName() + "/databases/";


        isNeedChoiseButton = getIntent().getBooleanExtra("buttonChoise", false);
        what = getIntent().getIntExtra("what",0);

        if (isNeedChoiseButton){
            getDir(preferences.getString(Values.PATH_FOR_COPY_ON_PC,rootInternal));
        }else if(isBase){
            getDir(rootBase);
        }else {
            getDir(root);
            }


    }

    private void getDir(String dirPAth) {

        mPathList = new ArrayList<>();
        File file = new File(dirPAth);
        File [] filesArray = file.listFiles();
        List<String> itemList = new ArrayList<>();
        itemList.add("../");
        mPathList.add(file.getParent());


        Arrays.sort(filesArray,fileComparator);


        for (File aFileArray : filesArray){
            file = aFileArray;

            if (!isNeedChoiseButton){
                if (!file.isHidden() && file.canRead()){
                    mPathList.add(file.getPath());
                    if (file.isDirectory()){
                        itemList.add(file.getName()+"/");
                    }else{
                        itemList.add(file.getName());
                    }
                }
            }else{
                if (file.isDirectory()){
                    mPathList.add(file.getPath());
                    itemList.add(file.getName()+"/");
                }
            }

        }

        if (isNeedChoiseButton){
            itemList.add(0,"*** Выбрать эту папку ***");
            mPathList.add(0,file.getParent());
            itemList.add(1,"Используется:"+preferences.getString(Values.PATH_FOR_COPY_ON_PC,""));
            mPathList.add(1,preferences.getString(Values.PATH_FOR_COPY_ON_PC,"/"));
        }



        ArrayAdapter <String> adapter = new ArrayAdapter<String>(this,R.layout.row,itemList);
        setListAdapter(adapter);
    }

    Comparator <? super File> fileComparator = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory()){
                if (rhs.isDirectory()) {
                    return String.valueOf(lhs.getName().toLowerCase()).compareTo(rhs.getName().toLowerCase());
                }else{
                    return  -1;
                }
            }else{
                if (rhs.isDirectory()){
                    return 1;
                }else {
                    return String.valueOf(lhs.getName().toLowerCase()).compareTo(rhs.getName().toLowerCase());
                }
            }
        }
    };

    private static long parseDate(String text)
            throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss",
                new Locale("ru"));
        return dateFormat.parse(text).getTime();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (position==0){
            File file = new File(mPathList.get(0));
            String absPath = file.getAbsolutePath();

            editor.putString(Values.PATH_FOR_COPY_ON_PC,absPath);
            editor.commit();

            finish();
        }else{

                File file = new File(mPathList.get(position));
                if (file.isDirectory()){
                    if (file.canRead())
                        getDir(mPathList.get(position));
                }else{
                    String absPath = file.getAbsolutePath();

                    if (what==10){
                        try {
                            Loader loader = new Loader(getApplicationContext(),FileManager.this,absPath,Values.LOAD_JOURNAL);
                            loader.execute();
                        }finally {
                            Log.d("done","loading");
                        }

                    }else if (what==11){
                        Loader loader = new Loader(getApplicationContext(),FileManager.this,absPath,Values.LOAD_TEACHERS);
                        loader.execute();
                    }else if(what==66){
                        Loader loader = new Loader(getApplicationContext(),FileManager.this,absPath,66);
                        loader.execute();
                    }else{
                        Toast.makeText(getApplicationContext(),"Error",  Toast.LENGTH_SHORT).show();
                    }

                }

        }


    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("onAttach","!!!!!!!!");
    }

    public static ArrayList<String> readFile (String path) throws IOException {
        File file = new File(path);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        int count = getStringCount(file);

        editor.putInt(Values.LINES_COUNT_IN_FILE,count);
        editor.commit();

        int i = 0;
        String line;
        ArrayList<String> lines = new ArrayList<>(count);
        while ((line = fin.readLine())!=null){
            if (i<count){
                if (!lines.contains(line)){
                    lines.add(line);
                    i++;
                    Log.d("lines",String.valueOf(i));
                }
            }
        }
        return lines;
    }

    public static void readLine (Context context,String path) throws IOException {
        File file = new File(path);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        int count = getStringCount(file);

        editor.putInt(Values.LINES_COUNT_IN_FILE,count);
        editor.commit();

        int i = 0;
        String line;
        ArrayList<String> lines = new ArrayList<>(count);
        DataBases db = new DataBases(context);
        db.clearBaseSQL();
        while ((line = fin.readLine())!=null){
            if (i<count){
                if (!lines.contains(line)){
                    String [] split = line.split(";");

                    db.writeInDBSQL(split[0],
                            split[1],
                            split[2],
                            split[3],
                            split[4]);

                    i++;
                    Log.d("lines",String.valueOf(i));
                }
            }
        }
        db.closeDBconnection();
    }


    public static int getStringCount(File file)
    {
        int i=0;
        BufferedReader bufferedReader = null;
        try{
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while(bufferedReader.readLine()!=null)
                i++;
            bufferedReader.close();
        }catch(Exception e){}
        return i;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
