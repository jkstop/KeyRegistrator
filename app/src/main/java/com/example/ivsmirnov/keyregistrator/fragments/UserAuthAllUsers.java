package com.example.ivsmirnov.keyregistrator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
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
        /*final TextInputLayout mPassInputLayout = (TextInputLayout)rootView.findViewById(R.id.enter_pass_input_layout);
        rootView.findViewById(R.id.enter_pass_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPassInputLayout.getEditText().getText().toString().equals("1212")){
                    Callback mCallback;
                    if (getParentFragment()!=null){
                        mCallback = (Callback)getParentFragment();
                    } else {
                        mCallback = (Callback)getActivity();
                    }
                    mCallback.unlockAccess();
                } else {
                    mPassInputLayout.setError("Пароль акбар");
                }
            }
        });*/
        return rootView;
    }

    public interface Callback{
        void unlockAccess();
    }
}
