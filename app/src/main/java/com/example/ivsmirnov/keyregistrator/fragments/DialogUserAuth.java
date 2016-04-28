package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
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
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
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

    private int previousTab = 0;
    private boolean correctPass = false;

    @Override
    public void onStart() {
        super.onStart();
        setRetainInstance(true);
        Dialog dialog = getDialog();
        if (dialog!=null){
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onDestroyView() {

        System.out.println("DESTROY");
        Dialog dialog = getDialog();
        if (dialog!=null && getRetainInstance()){
            //dialog.setOnDismissListener(null);
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("create VIEW");
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

        mViewPager = (ViewPager)rootView.findViewById(R.id.user_auth_pager_view);
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mViewPagerAdapter.addFragment(UserAuthCard.newInstance("!!SELECTED ROOM!!"), "По карте");
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR, FavoriteDB.CLICK_USER_ACCESS, Settings.getLastClickedAuditroom()), "Без карты");
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR,0,Settings.getLastClickedAuditroom()), "Все пользователи");

        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout)rootView.findViewById(R.id.user_auth_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_credit_card_black_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_touch_app_black_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_supervisor_account_black_24dp);

        mTabLayout.setOnTabSelectedListener(

                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        if (tab.getText().equals("Все пользователи")){
                            showEnterPassPopup();
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        previousTab = tab.getPosition();
                    }
                });

        return rootView;
    }

    private void showEnterPassPopup(){
        View dialogView = View.inflate(getContext(),R.layout.view_enter_password,null);
        final PopupWindow popup = new PopupWindow(getContext());
        final TextInputLayout mPassInputLayout = (TextInputLayout)dialogView.findViewById(R.id.enter_pass_input_layout);
        dialogView.findViewById(R.id.enter_pass_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPassInputLayout.getEditText().getText().toString().equals("1212")){
                    correctPass = true;
                    popup.dismiss();
                } else {
                    mPassInputLayout.setError("Пароль акбар");
                }
            }
        });
        popup.setContentView(dialogView);
        popup.setHeight(getResources().getDimensionPixelOffset(R.dimen.layout_default_margin)*5);
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (correctPass){
                    popup.dismiss();
                } else {
                    mTabLayout.getTabAt(previousTab).select();
                }
                correctPass = false;
            }
        });
        System.out.println("SHOW POPUP IN " + mTabLayout.toString());

        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                popup.showAsDropDown(mTabLayout, 0, 0);
            }
        });
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
