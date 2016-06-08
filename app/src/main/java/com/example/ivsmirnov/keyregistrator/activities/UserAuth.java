package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.ViewPagerAdapter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.DialogPassword;
import com.example.ivsmirnov.keyregistrator.fragments.DialogSearch;
import com.example.ivsmirnov.keyregistrator.fragments.PersonsFr;
import com.example.ivsmirnov.keyregistrator.fragments.UserAuthCard;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;

/**
 * Экран авторизации пользователя
 */
public class UserAuth extends AppCompatActivity implements BaseWriterInterface, DialogPassword.Callback {

    private Context mContext;

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private int previousTab = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_autorization);
        mContext = this;
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
        mViewPagerAdapter.addFragment(UserAuthCard.newInstance("!!SELECTED ROOM!!"), getString(R.string.tab_card));
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR, FavoriteDB.CLICK_USER_ACCESS, Settings.getLastClickedAuditroom()), getString(R.string.tab_free));
        mViewPagerAdapter.addFragment(PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_SELECTOR,0,Settings.getLastClickedAuditroom()), getString(R.string.tab_all));
        mViewPagerAdapter.addFragment(DialogSearch.newInstance(getIntent().getExtras().getString(PersonsFr.PERSONS_SELECTED_ROOM), DialogSearch.LIKE_FRAGMENT, null), getString(R.string.tab_new));

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

    /*private void showEnterPassPopup(){
        View dialogView = View.inflate(mContext, R.layout.view_enter_password,null);
        final PopupWindow popup = new PopupWindow(mContext);
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

        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                popup.showAsDropDown(mTabLayout, 0, 0);
            }
        });
    }

    private void showEnterPassDialog(){
        View dialogView = View.inflate(mContext, R.layout.view_enter_password, null);
        final TextInputLayout textInputLayout = (TextInputLayout)dialogView.findViewById(R.id.enter_pass_input_layout);
        final AlertDialog dialogPass = new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setTitle(getString(R.string.view_enter_password_title))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTabLayout.getTabAt(previousTab).select();
                        dialog.dismiss();
                    }
                })
                .create();
        dialogPass.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                dialogPass.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textInputLayout.getEditText().getText().toString().equals("1212")){
                            dialog.dismiss();
                        } else {
                            textInputLayout.setError(getString(R.string.view_enter_password_entered_incorrect));
                        }
                    }
                });
            }
        });
        dialogPass.show();
    }*/

    @Override
    public void onSuccessBaseWrite() {
        finish();
    }

    @Override
    public void onDialogEnterPassDismiss() {
        mTabLayout.getTabAt(previousTab).select();
    }
}
