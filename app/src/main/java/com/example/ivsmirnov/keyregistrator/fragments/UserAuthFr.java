package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterFreeUsersList;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.custom_views.RecyclerWrapContentHeightManager;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.util.ArrayList;

/**
 * фрагмент авторизации пользователя
 */
public class UserAuthFr extends Fragment {

    private Context mContext;
    private RecyclerView mFreeUsersRecycler;
    //private ArrayList<String> mTags;
    private ArrayList<PersonItem> mFreeAccessPersonsList;

    private ActionBar mActionBar;

    public static UserAuthFr newInstance(){
        return new UserAuthFr();
    }


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.layout_autorization,container,false);
        mContext = rootView.getContext();



        //Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.user_auth_toolbar);
        //toolbar.setTitle("Авторизация пользователя");
        //toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        //toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        getActivity().onBackPressed();
        //    }
        //});

        //mTags = Settings.getFreeUsers();
        //mFreeAccessPersonsList = FavoriteDB.getPersonItems(null, FavoriteDB.CLICK_USER_ACCESS);

        //TextView mTextAud = (TextView)rootView.findViewById(R.id.nfc_fragment_aud);
        //mTextAud.setText(Settings.getLastClickedAuditroom());

        //ImageView imageView = (ImageView)rootView.findViewById(R.id.nfc_fragment_reader_image);
        //AnimationDrawable animationDrawable = (AnimationDrawable)imageView.getDrawable();
        //animationDrawable.start();

        /*final Button showPopupButton = (Button)rootView.findViewById(R.id.nfc_fragment_free_users_button);
        showPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScrollView viewGroup = (ScrollView) rootView.findViewById(R.id.layout_popup_free_persons);
                View layout = inflater.inflate(R.layout.layout_popup_free_persons, viewGroup);

                mFreeUsersRecycler = (RecyclerView)layout.findViewById(R.id.nfc_fragment_free_users_recycler);
                mFreeUsersRecycler.setLayoutManager(new RecyclerWrapContentHeightManager(mContext, LinearLayoutManager.VERTICAL, false));
                mFreeUsersRecycler.setHasFixedSize(true);

                // Creating the PopupWindow
                final PopupWindow popup = new PopupWindow(mContext);
                popup.setContentView(layout);
                popup.setHeight(getResources().getDimensionPixelOffset(R.dimen.layout_default_margin)*5);
                popup.setFocusable(true);
                popup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Clear the default translucent background
                popup.setBackgroundDrawable(new BitmapDrawable());

                popup.showAsDropDown(showPopupButton, 0, 0);

                mFreeUsersRecycler.setAdapter(new AdapterFreeUsersList(mContext, mFreeAccessPersonsList, new RecycleItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position, int viewID) {

                        popup.dismiss();

                        new BaseWriter(mContext, (BaseWriterInterface)getActivity()).execute(new BaseWriterParams()
                                .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                                .setAuditroom(Settings.getLastClickedAuditroom())
                                .setPersonTag(mFreeAccessPersonsList.get(position).getRadioLabel()));
                    }

                    @Override
                    public void onItemLongClick(View v, int position, long timeIn) {

                    }
                }));
            }
        });*/

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mActionBar!= null){
            mActionBar.setBackgroundDrawable(null);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (mActionBar != null){
            mActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
            mActionBar.setTitle("Авторизация пользователя");

            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }
        //if (((Launcher) getActivity()).getSupportActionBar() != null) {
        //    ((Launcher) getActivity()).getSupportActionBar().setTitle("");
        //}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//getActivity().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_nfc,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.menu_nfc_all_persons:
                //запуск диалога ввода пароля
                Dialogs dialogs = new Dialogs();
                Bundle bundle = new Bundle();
                bundle.putString(Settings.AUDITROOM, getArguments().getString(Settings.AUDITROOM));
                bundle.putInt(Dialogs.DIALOG_ENTER_PASSWORD_TYPE, Dialogs.DIALOG_ENTER_PASSWORD_TYPE_ACCESS_FOR_PERSONS);
                bundle.putInt(Dialogs.DIALOG_TYPE,Dialogs.DIALOG_ENTER_PASSWORD);
                dialogs.setArguments(bundle);
                dialogs.show(getChildFragmentManager(),"enter_pin");

                //есди нужен доступ без пароля
                /*Bundle bundle = new Bundle();
                bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                PersonsFr persons_fragment = PersonsFr.newInstance();
                persons_fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                       .beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.fragment_tag_persons)).commit();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
