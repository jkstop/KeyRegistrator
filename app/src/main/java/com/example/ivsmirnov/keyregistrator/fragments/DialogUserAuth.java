package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.ViewPagerAdapter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;

/**
 * Диалог авторизации пользователя
 */
public class DialogUserAuth extends DialogFragment implements PersonsFr.DialogNeedClose, UserAuthAllUsers.Callback {

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

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
        View rootView = inflater.inflate(R.layout.layout_autorization,container, false);

        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.user_auth_toolbar);
        toolbar.setTitle("Авторизация пользователя");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });



        UserAuthAllUsers userAuthAllUsers = new UserAuthAllUsers();

        mViewPager = (ViewPager)rootView.findViewById(R.id.user_auth_pager_view);
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mViewPagerAdapter.addFragment(UserAuthCard.newInstance("!!SELECTED ROOM!!"), "По карте");
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR, FavoriteDB.CLICK_USER_ACCESS, Settings.getLastClickedAuditroom()), "Без карты");
        mViewPagerAdapter.addFragment(userAuthAllUsers, "Все пользователи");

        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout)rootView.findViewById(R.id.user_auth_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_credit_card_black_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_touch_app_black_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_supervisor_account_black_24dp);

        View dialogView = View.inflate(getContext(),R.layout.view_enter_password,null);
        //final AlertDialog dialodPass = new AlertDialog.Builder(mViewPager.getContext())//popupWindow ???
        //        .setTitle("TITLE")
        //        .setView(dialogView)
         //       .create();

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(mViewPager.getContext());
        popup.setContentView(dialogView);
        popup.setHeight(getResources().getDimensionPixelOffset(R.dimen.layout_default_margin)*5);
        popup.setFocusable(true); //скидывается
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Clear the default translucent background
        //popup.setBackgroundDrawable();

        //popup.showAsDropDown(showPopupButton, 0, 0);

        mTabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        if (tab.getText().equals("Все пользователи")){
                            popup.showAsDropDown(mTabLayout, 0, 0);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        if (tab.getText().equals("Все пользователи")){
                            //dialodPass.hide();
                            popup.dismiss();
                        }
                    }
                });

        return rootView;
    }

    @Override
    public void close() {
        getDialog().cancel();
    }

    @Override
    public void unlockAccess() {
        System.out.println("SHOULD UNLOCK ACCESS");
        //mViewPagerAdapter.deleteFragment(2);
        //mViewPagerAdapter.notifyDataSetChanged();

        //mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR,0,null),"new");
        //mViewPagerAdapter.notifyDataSetChanged();

        //mTabLayout.setupWithViewPager(mViewPager);
    }
}
