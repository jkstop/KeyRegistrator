package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class Nfc_Fragment extends Fragment {

    private Context mContext;
    private Button mSelectButton;

    public static Nfc_Fragment newInstance(){
        return new Nfc_Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_nfc_fragment,container,false);
        mContext = rootView.getContext();

        mSelectButton = (Button)rootView.findViewById(R.id.nfc_fragment_handle_select_button);
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                bundle.putString(Values.AUDITROOM, getArguments().getString(Values.AUDITROOM));
                Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                persons_fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.tag_persons_fragment)).commit();
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher) getActivity()).getSupportActionBar() != null) {
            ((Launcher) getActivity()).getSupportActionBar().setTitle("");
        }
    }
}
