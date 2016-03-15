package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_free_users;
import com.example.ivsmirnov.keyregistrator.async_tasks.TakeKey;
import com.example.ivsmirnov.keyregistrator.custom_views.RecyclerWrapContentHeightManager;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.KeyInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.TakeKeyParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class Nfc_Fragment extends Fragment {

    private Context mContext;
    private Button mSelectButton;
    private Button mDekanatPmit,mDekanatAR;
    private RecyclerView mFreeUsersRecycler;
    private ArrayList<String> mTags;

    public static Nfc_Fragment newInstance(){
        return new Nfc_Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.layout_nfc_fragment,container,false);
        mContext = rootView.getContext();

        mTags = Settings.getFreeUsers();

        ImageView imageView = (ImageView)rootView.findViewById(R.id.nfc_fragment_reader_image);
        AnimationDrawable animationDrawable = (AnimationDrawable)imageView.getDrawable();
        animationDrawable.start();

        final Button showPopupButton = (Button)rootView.findViewById(R.id.nfc_fragment_free_users_button);
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

                mFreeUsersRecycler.setAdapter(new adapter_free_users(mContext, mTags, new RecycleItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position, int viewID) {

                        new TakeKey(mContext).execute(new TakeKeyParams()
                                .setAccessType(DataBaseJournal.ACCESS_BY_CLICK)
                                .setAuditroom(Settings.getLastClickedAuditroom())
                                .setPersonTag(mTags.get(position))
                                .setPublicInterface(new KeyInterface() {
                                    @Override
                                    public void onTakeKey() {
                                        popup.dismiss();
                                        getFragmentManager().beginTransaction()
                                                .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main))
                                                .commit();
                                    }
                                }));
                    }

                    @Override
                    public void onItemLongClick(View v, int position, long timeIn) {

                    }
                }));
            }
        });

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher) getActivity()).getSupportActionBar() != null) {
            ((Launcher) getActivity()).getSupportActionBar().setTitle("");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_nfc,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_nfc_all_persons:
                //запуск диалога ввода пароля
                Dialogs dialogs = new Dialogs();
                Bundle bundle = new Bundle();
                bundle.putString(Values.AUDITROOM, getArguments().getString(Values.AUDITROOM));
                bundle.putInt(Dialogs.DIALOG_ENTER_PASSWORD_TYPE, Dialogs.DIALOG_ENTER_PASSWORD_TYPE_ACCESS_FOR_PERSONS);
                bundle.putInt(Dialogs.DIALOG_TYPE,Dialogs.DIALOG_ENTER_PASSWORD);
                dialogs.setArguments(bundle);
                dialogs.show(getChildFragmentManager(),"enter_pin");

                //есди нужен доступ без пароля
                /*Bundle bundle = new Bundle();
                bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                persons_fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                       .beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.fragment_tag_persons)).commit();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
