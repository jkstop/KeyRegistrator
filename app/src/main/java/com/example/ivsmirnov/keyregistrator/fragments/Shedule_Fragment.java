package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_shedule_list;
import com.example.ivsmirnov.keyregistrator.async_tasks.Load_shedule;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseShedule;
import com.example.ivsmirnov.keyregistrator.interfaces.Shedule_Load;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by IVSmirnov on 08.09.2015.
 */
public class Shedule_Fragment extends Fragment implements Shedule_Load{

    private Context mContext;
    private DataBaseShedule dbShedule;
    private ArrayList<SparseArray> mItems;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private ListView list;
    private TextView textHead;

    public static Shedule_Fragment newInstance(){
        return new Shedule_Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_shedule_fragment,container,false);
        mContext = rootView.getContext();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        dbShedule = new DataBaseShedule(mContext);
        mItems = dbShedule.readShedule();
        dbShedule.closeDB();
        sortByABC(mItems);

        list = (ListView)rootView.findViewById(R.id.shedule_fragment_list);
        list.setAdapter(new adapter_shedule_list(mContext,mItems));

        textHead = (TextView)rootView.findViewById(R.id.layout_shedule_head_date);
        textHead.setText(mSharedPreferences.getString(Values.DATE_SHEDULE_UPDATE, "Нажмите ОБНОВИТЬ"));

        return rootView;
    }

    private void sortByABC(ArrayList <SparseArray> items){
        Collections.sort(items, new Comparator<SparseArray>() {
            @Override
            public int compare(SparseArray lhs, SparseArray rhs) {
                String first = String.valueOf(lhs.get(0));
                String second = String.valueOf(rhs.get(0));
                return first.compareToIgnoreCase(second);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_shedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_shedule_update:
                Load_shedule load_shedule = new Load_shedule(mContext,this);
                load_shedule.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFinish(Integer result) {
        if (result==1){
            dbShedule = new DataBaseShedule(mContext);
            mItems = dbShedule.readShedule();
            dbShedule.closeDB();
            sortByABC(mItems);

            list.setAdapter(new adapter_shedule_list(mContext, mItems));

            mSharedPreferencesEditor.putString(Values.DATE_SHEDULE_UPDATE,showDate());
            mSharedPreferencesEditor.commit();
            textHead.setText(showDate());

            Toast.makeText(mContext,"Обновлено",Toast.LENGTH_SHORT).show();
        }
    }

    public static String showDate() {
        Date currentDate =  new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM",new Locale("ru"));
        return String.valueOf(dateFormat.format(currentDate));
    }


}
