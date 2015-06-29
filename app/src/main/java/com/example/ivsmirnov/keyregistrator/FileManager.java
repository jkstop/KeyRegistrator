package com.example.ivsmirnov.keyregistrator;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.TimeZone;

/**
 * Created by ivsmirnov on 27.04.2015.
 */
public class FileManager extends ListActivity {

    private List<String> mPathList = null;
    private String root,rootInternal,rootBase;
    boolean isNeedChoiseButton,isBase;
    int what;


    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);


        editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

        root = Environment.getExternalStorageDirectory().getPath();
        rootInternal = "/";
        rootBase = "/data/data/" + getPackageName() + "/databases/";


        isNeedChoiseButton = getIntent().getBooleanExtra("buttonChoise", false);
        what = getIntent().getIntExtra("what",0);

        if (isNeedChoiseButton){
            getDir(rootInternal);
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
        if (!dirPAth.equals(root)){
            itemList.add(root);
            mPathList.add(root);
            itemList.add("../");
            mPathList.add(file.getParent());
        }

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
                    String [] items = null;
                    DataBases db = new DataBases(getApplicationContext());

                    if (what==10){
                        FragmentManager fragmentManager = getFragmentManager();
                        Loader loader = new Loader(getApplicationContext(),absPath,fragmentManager);
                        loader.execute();
                       /* db.clearJournalDB();
                        try {
                            items = readFile(getApplicationContext(),absPath);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        for (String s : items){
                            String [] split = s.split("\\s+");

                            String aud = "";
                            String name = "";
                            Long time = (long)1;
                            Long timePut = (long)1;

                            if(split.length>5){
                                aud = split[0];
                                int nameIndexLast = split.length-3;
                                for (int i=1;i<=nameIndexLast;i++){
                                    name += split[i]+" ";
                                }
                                try {
                                    time = parseDate(split[split.length-2]);
                                    timePut = parseDate(split[split.length-1]);
                                } catch (ParseException e) {
                                    time = (long)1;
                                    timePut = (long)1;
                                }
                            }else if(split.length<5){
                                if (split[0].length()!=3){
                                    aud = "_";
                                    for (int i=0;i<=split.length-1;i++){
                                        name += split[i]+" ";
                                    }
                                }else{
                                    aud = split[0];
                                    for (int i = 1;i<=split.length-3;i++){
                                        name += split[i]+" ";
                                    }
                                    try {
                                        time = parseDate(split[split.length-2]);
                                        timePut = parseDate(split[split.length-1]);
                                    } catch (ParseException e) {
                                        time = (long)1;
                                        timePut = (long)1;
                                    }
                                }
                            }else{
                                aud = split[0];
                                name = split[1]+ " "+ split[2];
                                try {
                                    time = parseDate(split[3]);
                                    timePut = parseDate(split[4]);
                                } catch (ParseException e) {
                                    time = (long)1;
                                    timePut = (long)1;
                                }
                            }
                            db.writeInDBJournal(aud,name,time,timePut,true);

                        }*/
                    }else{
                        db.clearTeachersDB();
                        try {
                            items = readFile(getApplicationContext(),absPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for (String s : items){
                            db.writeInDBTeachers(s);
                        }
                    }

                    db.closeDBconnection();
                    finish();
                }

        }


    }

    public static String[] readFile (Context context, String path) throws IOException {
        File file = new File(path);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        int count = getStringCount(file);
        int i = 0;
        String line;
        String [] lines = new String[count];
        while ((line = fin.readLine())!=null){
            if (i<count){
                lines [i] = line;
                i++;
            }
        }
//        Toast.makeText(context,"Из файла загружено "+count+" записей",Toast.LENGTH_LONG).show();
        return lines;
    }

    private static int getStringCount(File file)
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
