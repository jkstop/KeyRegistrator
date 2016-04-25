package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.interfaces.Updatable;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Диалог для установки размеров сетки главного экрана
 */
public class DialogPickers extends DialogFragment implements UpdateInterface {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("create");
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("create view");
        View pagerView = View.inflate(getContext(), R.layout.layout_tab, null);
        ViewPager viewPager = (ViewPager)pagerView.findViewById(R.id.pager_pickers);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(Picker.newInstance(Picker.PICKER_PORTRAIT),"Портретный");
        viewPagerAdapter.addFragment(Picker.newInstance(Picker.PICKER_LANDSCAPE), "Альбомный");
        viewPager.setAdapter(viewPagerAdapter);

        Toolbar toolbar = (Toolbar)pagerView.findViewById(R.id.toolbar_pickers);
        toolbar.setTitle("Размер сетки");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                updateInformation();
            }
        });

        TabLayout tabLayout = (TabLayout)pagerView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_stay_current_portrait_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_stay_current_landscape_black_24dp);

        if (App.getAppContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            tabLayout.getTabAt(1).select();
        }

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return pagerView;
    }

    @Override
    public void updateInformation() {
        UpdateInterface updateInterface = (UpdateInterface)getTargetFragment();
        updateInterface.updateInformation();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
