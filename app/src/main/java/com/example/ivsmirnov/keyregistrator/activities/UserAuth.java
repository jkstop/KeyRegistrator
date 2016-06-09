package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.ViewPagerAdapter;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.DialogPassword;
import com.example.ivsmirnov.keyregistrator.fragments.DialogSearch;
import com.example.ivsmirnov.keyregistrator.fragments.PersonsFr;
import com.example.ivsmirnov.keyregistrator.fragments.UserAuthCard;
import com.example.ivsmirnov.keyregistrator.others.Settings;

/**
 * Экран авторизации пользователя
 */
public class UserAuth extends AppCompatActivity implements BaseWriter.Callback, DialogPassword.Callback {

    private Context mContext;

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private int previousTab = 0;

    public static String mSelectedRoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_autorization);
        mContext = this;
        mSelectedRoom = getIntent().getExtras().getString(PersonsFr.PERSONS_SELECTED_ROOM);
        initUI();
    }

    private void initUI(){
        //set actionbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.user_auth_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager)findViewById(R.id.user_auth_pager_view);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(UserAuthCard.newInstance(mSelectedRoom), getString(R.string.tab_card));
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR, FavoriteDB.CLICK_USER_ACCESS, mSelectedRoom), getString(R.string.tab_free));
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR, 0, mSelectedRoom), getString(R.string.tab_all));
        mViewPagerAdapter.addFragment(DialogSearch.newInstance(mSelectedRoom, DialogSearch.LIKE_FRAGMENT), getString(R.string.tab_new));

        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.user_auth_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_credit_card_black_24dp).setTag(getString(R.string.tab_card));
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_touch_app_black_24dp).setTag(getString(R.string.tab_free));
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_lock_black_24dp).setTag(getString(R.string.tab_all));
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_person_add_black_24dp).setTag(getString(R.string.tab_new));

        mTabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);

                        if (tab.getTag().equals(getString(R.string.tab_all))){
                            new DialogPassword().show(getSupportFragmentManager(), DialogPassword.PERSONS_ACCESS);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        previousTab = tab.getPosition();
                    }
                });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSuccessBaseWrite() {
        finish();
    }

    @Override
    public void onErrorBaseWrite() {

    }

    @Override
    public void onDialogEnterPassDismiss() {
        mTabLayout.getTabAt(previousTab).select();
    }
}
