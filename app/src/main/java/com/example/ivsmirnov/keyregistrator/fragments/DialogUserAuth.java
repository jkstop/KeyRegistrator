package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.ViewPagerAdapter;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

/**
 * Авторизация пользователя
 */
public class DialogUserAuth extends DialogFragment implements
        BaseWriter.Callback,
        DialogPassword.Callback,
        DialogSearch.Callback{

    private Context mContext;

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private Callback mCallback;

    private int previousTab = 0;

    public static String mSelectedRoom;

    public static DialogUserAuth newInstance(String selectedRoom){
        Bundle bundle = new Bundle();
        bundle.putString(Users.PERSONS_SELECTED_ROOM, selectedRoom);
        DialogUserAuth dialogUserAuth = new DialogUserAuth();
        dialogUserAuth.setArguments(bundle);
        return dialogUserAuth;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mSelectedRoom = getArguments().getString(Users.PERSONS_SELECTED_ROOM);
        mCallback = (Callback)getActivity();
    }

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
        View dialogView = View.inflate(mContext, R.layout.dialog_user_auth, null);
        final Toolbar toolbar = (Toolbar) dialogView.findViewById(R.id.user_auth_toolbar);
        toolbar.setTitle(R.string.title_user_auth);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        mViewPager = (ViewPager)dialogView.findViewById(R.id.user_auth_pager_view);
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mViewPagerAdapter.addFragment(new AuthCard(), getString(R.string.auth_tab_card));
        mViewPagerAdapter.addFragment(Users.newInstance(Users.PERSONS_FRAGMENT_SELECTOR, FavoriteDB.CLICK_USER_ACCESS, mSelectedRoom), getString(R.string.auth_tab_free));
        mViewPagerAdapter.addFragment(Users.newInstance(Users.PERSONS_FRAGMENT_SELECTOR, 0, mSelectedRoom), getString(R.string.auth_tab_all));
        mViewPagerAdapter.addFragment(DialogSearch.newInstance(mSelectedRoom, DialogSearch.LIKE_FRAGMENT), getString(R.string.auth_tab_new));

        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout)dialogView.findViewById(R.id.user_auth_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_credit_card_black_24dp).setTag(getString(R.string.auth_tab_card));
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_touch_app_black_24dp).setTag(getString(R.string.auth_tab_free));
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_lock_black_24dp).setTag(getString(R.string.auth_tab_all));
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_person_add_black_24dp).setTag(getString(R.string.auth_tab_new));

        mTabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);

                        if (tab.getTag().equals(getString(R.string.auth_tab_all))){
                            new DialogPassword().show(getChildFragmentManager(), DialogPassword.PERSONS_ACCESS);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        previousTab = tab.getPosition();
                    }
                });
        return dialogView;
    }

    @Override
    public void onDialogEnterPassDismiss() {
        mTabLayout.getTabAt(previousTab).select();
    }

    @Override
    public void onSuccessBaseWrite() {
        mCallback.onSuccessBaseWrite();
        getDialog().cancel();
    }

    @Override
    public void onErrorBaseWrite() {
        mCallback.onErrorBaseWrite();
    }

    @Override
    public void onUserAdded(PersonItem personItem) {
        getDialog().cancel();
    }

    public interface Callback{
        void onSuccessBaseWrite();
        void onErrorBaseWrite();
    }
}
