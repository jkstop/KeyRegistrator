package com.example.ivsmirnov.keyregistrator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;

/**
 * Created by ivsmirnov on 26.04.2016.
 */
public class UserAuthAllUsers extends Fragment {

    public UserAuthAllUsers (){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_auth_all_users,container,false);
        return rootView;
    }
}
