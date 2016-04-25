package com.example.ivsmirnov.keyregistrator.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.MainFr;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class AdapterMainRoomGrid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<RoomItem> mRoomItems;
    private RecycleItemClickListener mListener;
    private Context mContext;

    public AdapterMainRoomGrid(Context context, ArrayList<RoomItem> roomItems, RecycleItemClickListener listener) {
        this.mRoomItems = roomItems;
        this.mListener = listener;
        this.mContext = context;
    }

    static class auditroomFreeViewHolder extends RecyclerView.ViewHolder{

        public ImageView mFreeImageKey;
        public TextView mFreeTextAuditroom;

        public auditroomFreeViewHolder(View itemView) {
            super(itemView);
            mFreeImageKey = (ImageView)itemView.findViewById(R.id.card_auditroom_free_image_key);
            mFreeTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_free_text_auditroom);
        }
    }

    static class auditroomBusyViewHolder extends RecyclerView.ViewHolder{

        public ImageView mBusyImagePerson;
        public TextView mBusyTextAuditroom;
        public TextView mBusyTextPerson;
        public CardView mBusyCard;
        public ProgressBar mProgress;

        public auditroomBusyViewHolder(View itemView) {
            super(itemView);
            mBusyImagePerson = (ImageView)itemView.findViewById(R.id.card_auditroom_busy_image_person);
            mBusyTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_auditroom);
            mBusyTextPerson = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_person);
            mBusyCard = (CardView)itemView.findViewById(R.id.card_auditroom_busy);
            mProgress = (ProgressBar)itemView.findViewById(R.id.card_auditroom_busy_image_progress);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mRoomItems.get(position).getStatus();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case 1:
                final View rootViewFree = layoutInflater.inflate(R.layout.card_auditroom_free,parent,false);
                viewHolder = new auditroomFreeViewHolder(rootViewFree);
                rootViewFree.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getItemScaleHeight()));
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                rootViewFree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder.getLayoutPosition(),0);
                    }
                });
                final RecyclerView.ViewHolder finalViewHolder1 = viewHolder;
                rootViewFree.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onItemLongClick(v, finalViewHolder1.getLayoutPosition(),0);
                        return true;
                    }
                });
                break;
            case 0:
                View rootViewBusy = layoutInflater.inflate(R.layout.card_auditroom_busy,parent,false);
                viewHolder = new auditroomBusyViewHolder(rootViewBusy);
                rootViewBusy.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getItemScaleHeight()));
                final RecyclerView.ViewHolder finalViewHolder2 = viewHolder;
                rootViewBusy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder2.getLayoutPosition(),0);
                    }
                });
                rootViewBusy.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onItemLongClick(v,finalViewHolder2.getLayoutPosition(),0);
                        return true;
                    }
                });
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 1:
                ((auditroomFreeViewHolder) holder).mFreeTextAuditroom.setText(mRoomItems.get(position).getAuditroom());

                break;
            case 0:
                ((auditroomBusyViewHolder)holder).mBusyTextAuditroom.setText(mRoomItems.get(position).getAuditroom());
                ((auditroomBusyViewHolder)holder).mBusyTextPerson.setText(mRoomItems.get(position).getLastVisiter());

                Picasso.with(mContext)
                        .load(FavoriteDB.getPersonPhotoPath(mRoomItems.get(position).getTag()))
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_not_found)
                        .into(((auditroomBusyViewHolder)holder).mBusyImagePerson);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mRoomItems.size();
    }

    private int getItemScaleHeight(){
        //int recyclerRows = (int) Math.ceil(12 / App.getAppContext().getResources().getInteger(R.integer.room_grid_count));

        if (App.getAppContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            return MainFr.mAuditroomGrid.getHeight()/ Settings.getRowsLandscape();
        } else {
            return MainFr.mAuditroomGrid.getHeight()/ Settings.getRowsPortrait();
        }


    }

}
