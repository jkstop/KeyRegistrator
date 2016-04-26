package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.ViewPagerAdapter;
import com.example.ivsmirnov.keyregistrator.others.App;

/**
 * Диалог авторизации пользователя
 */
public class DialogUserAuth extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog!=null){
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_autorization,container);

        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.user_auth_toolbar);
        toolbar.setTitle("Авторизация пользователя");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("back click");
                getDialog().cancel();
            }
        });


        ViewPager viewPager = (ViewPager)rootView.findViewById(R.id.user_auth_pager_view);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(UserAuthCard.newInstance("!!SELECTED ROOM!!"), "По карте");
        viewPagerAdapter.addFragment(new UserAuthClick(), "Без карты");
        viewPagerAdapter.addFragment(new UserAuthAllUsers(), "Все пользователи");
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout)rootView.findViewById(R.id.user_auth_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_credit_card_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_touch_app_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_person_black_24dp);

        return rootView;
    }

}
