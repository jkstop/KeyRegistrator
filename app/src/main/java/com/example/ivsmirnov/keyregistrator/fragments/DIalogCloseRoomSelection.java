package com.example.ivsmirnov.keyregistrator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterMainRoomGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.util.ArrayList;

/**
 * Если открыто несколько помещений на 1 пользователя
 */
public class DialogCloseRoomSelection extends DialogFragment implements RecycleItemClickListener{

    private static final String BUNDLE_USER_RADIO_LABEL = "bundle_user_radio_label";

    private String userRadioLabel;
    private ArrayList<RoomItem> roomItems;

    public static DialogCloseRoomSelection newInstance(String userRadioLabel){
        DialogCloseRoomSelection dialogCloseRoomSelection = new DialogCloseRoomSelection();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_USER_RADIO_LABEL, userRadioLabel);
        dialogCloseRoomSelection.setArguments(bundle);
        return dialogCloseRoomSelection;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRadioLabel = getArguments().getString(BUNDLE_USER_RADIO_LABEL);
        roomItems = RoomDB.getRoomItemsForCurrentUser(userRadioLabel);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.view_dialog_close_room_selection, container, false);

        AdapterMainRoomGrid adapterMainRoomGrid = new AdapterMainRoomGrid(getContext(), roomItems, this);

        Toolbar toolbar = (Toolbar)dialogView.findViewById(R.id.toolbar_close_room_selection);
        toolbar.setTitle(getString(R.string.title_dialog_close_room_choise));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        RecyclerView recyclerView = (RecyclerView)dialogView.findViewById(R.id.recycler_close_room_selection);
        recyclerView.setAdapter(adapterMainRoomGrid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));

        return dialogView;
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        new BaseWriter(BaseWriter.UPDATE_CURRENT, (BaseWriter.Callback)getActivity()).execute(new BaseWriterParams()
                .setPersonTag(roomItems.get(position).getTag())
                .setOpenTime(roomItems.get(position).getTime()));

        getDialog().cancel();
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {

    }

}
