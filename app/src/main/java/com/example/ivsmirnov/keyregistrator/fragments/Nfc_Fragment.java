package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class Nfc_Fragment extends Fragment {

    private Context mContext;
    private Button mSelectButton;
    private Button mDekanatPmit,mDekanatAR;

    public static Nfc_Fragment newInstance(){
        return new Nfc_Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_nfc_fragment,container,false);
        mContext = rootView.getContext();
        mDekanatPmit = (Button)rootView.findViewById(R.id.nfc_fragment_button_dekanat_pmit);
        mDekanatAR = (Button)rootView.findViewById(R.id.nfc_fragment_button_dekanat_ar);
        mDekanatPmit.setOnClickListener(clickDekanat);
        mDekanatAR.setOnClickListener(clickDekanat);
        return rootView;
    }

    View.OnClickListener clickDekanat = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String aud = getArguments().getString(Values.AUDITROOM);
            String path = "";
            String tag = "";
            Persons_Fragment.writeIt(mContext,aud, ((Button) v).getText().toString(), System.currentTimeMillis(), path,tag,"hand");
            getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main)).commit();
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher) getActivity()).getSupportActionBar() != null) {
            ((Launcher) getActivity()).getSupportActionBar().setTitle("");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_nfc,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_nfc_all_persons:
                Dialog_Fragment dialog_fragment = new Dialog_Fragment();
                Bundle bundle = new Bundle();
                bundle.putString(Values.AUDITROOM, getArguments().getString(Values.AUDITROOM));
                bundle.putInt(Values.DIALOG_CLOSE_ROOM_TYPE,Values.DIALOG_CLOSE_ROOM_TYPE_PERSONS);
                bundle.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLOSE_ROOM);
                dialog_fragment.setArguments(bundle);
                dialog_fragment.show(getChildFragmentManager(),"enter_pin");
                /*Bundle bundle = new Bundle();
                bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                bundle.putString(Values.AUDITROOM, getArguments().getString(Values.AUDITROOM));
                Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                persons_fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.tag_persons_fragment)).commit();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
