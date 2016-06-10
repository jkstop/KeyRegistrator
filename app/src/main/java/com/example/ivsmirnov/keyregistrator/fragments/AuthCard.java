package com.example.ivsmirnov.keyregistrator.fragments;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.ivsmirnov.keyregistrator.R;

/**
 * Фрагмент авторизации по карте
 */
public class AuthCard extends Fragment {

    private String mSelectedRoom;

    public static AuthCard newInstance (String selectedRoom){
        AuthCard authCard = new AuthCard();
        Bundle bundle = new Bundle();
        bundle.putString(Users.PERSONS_SELECTED_ROOM, selectedRoom);
        authCard.setArguments(bundle);
        return authCard;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras!=null){
            mSelectedRoom = extras.getString(Users.PERSONS_SELECTED_ROOM, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_auth_card, container,false);
        ImageView mImageWayContainer = (ImageView)rootView.findViewById(R.id.user_auth_card_arrow_down);
        Drawable mDrawArrow = getContext().getResources().getDrawable(R.drawable.down);
        mDrawArrow.setColorFilter(getContext().getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
        mImageWayContainer.setImageDrawable(mDrawArrow);
        Animation fadeAnim = AnimationUtils.loadAnimation(getContext(),R.anim.fade_arrow);
        mImageWayContainer.startAnimation(fadeAnim);
        return rootView;
    }
}
