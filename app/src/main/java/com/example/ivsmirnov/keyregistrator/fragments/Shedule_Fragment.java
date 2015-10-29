package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.Load_shedule;
import com.example.ivsmirnov.keyregistrator.interfaces.Shedule_Load;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 08.09.2015.
 */
public class Shedule_Fragment extends Fragment implements Shedule_Load{

    private Context mContext;

    public static Shedule_Fragment newInstance(){
        return new Shedule_Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_shedule_fragment,container,false);
        mContext = rootView.getContext();
        ListView list = (ListView)rootView.findViewById(R.id.shedule_fragment_list);
        Load_shedule load_shedule = new Load_shedule(mContext,this);
        load_shedule.execute();
        return rootView;
    }


    @Override
    public void onFinish(ArrayList<String> items) {
        for (String s : items){
            Log.d("itemFromJSON",s);
        }
    }
}
