package com.example.ivsmirnov.keyregistrator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;

/**
 * Фрагмент авторизации по карте
 */
public class UserAuthCard extends Fragment {

    public static final String SELECTED_ROOM = "SELECTED_ROOM";

    public static UserAuthCard newInstance (String selectedRoom){
        UserAuthCard userAuthCard = new UserAuthCard();
        Bundle bundle = new Bundle();
        bundle.putString(SELECTED_ROOM, selectedRoom);
        userAuthCard.setArguments(bundle);
        return userAuthCard;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_auth_card, container,false);
        return rootView;
    }
}
