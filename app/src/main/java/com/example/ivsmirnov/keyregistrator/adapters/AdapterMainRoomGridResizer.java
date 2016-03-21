package com.example.ivsmirnov.keyregistrator.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.fragments.MainFr;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.fragments.Dialogs;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.util.ArrayList;

/**
 * Адаптер layout для изменения размера
 */
public class AdapterMainRoomGridResizer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<RoomItem> mRoomItems;

    public AdapterMainRoomGridResizer(ArrayList<RoomItem> roomItems){
        this.mRoomItems = roomItems;
    }

    static class cardViewHolder extends RecyclerView.ViewHolder{

        public TextView mTextAuditroomName;

        public cardViewHolder(View itemView) {
            super(itemView);
            mTextAuditroomName = (TextView)itemView.findViewById(R.id.card_auditroom_free_text_auditroom);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View rootView = mInflater.inflate(R.layout.card_auditroom_free,parent,false);
        RecyclerView.LayoutParams viewParams = (RecyclerView.LayoutParams) rootView.getLayoutParams();
        viewParams.height = getItemScaleHeight();
        return new cardViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((cardViewHolder)holder).mTextAuditroomName.setText(mRoomItems.get(position).getAuditroom());
    }

    @Override
    public int getItemCount() {
        return mRoomItems.size();
    }


    private int getItemScaleHeight(){
        int  recyclerHeight;
        if (Dialogs.mFrameGrid!=null){
            recyclerHeight = Dialogs.mFrameGrid.getHeight();
        }else{
            recyclerHeight = MainFr.mAuditroomGrid.getHeight();
        }
        int recyclerChilds = mRoomItems.size();
        int recyclerRows = (int) Math.ceil((double) recyclerChilds / Settings.getAuditroomColumnsCount());
        return recyclerHeight/recyclerRows;
    }
}
