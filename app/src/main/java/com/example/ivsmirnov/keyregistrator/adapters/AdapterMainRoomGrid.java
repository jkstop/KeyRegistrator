package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class AdapterMainRoomGrid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<RoomItem> mRoomItems;
    private RecycleItemClickListener mListener;
    private Context mContext;

    private View cellFree;
    private View cellBusy;

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
            mFreeImageKey = (ImageView)itemView.findViewById(R.id.room_free_card_image_key);
            mFreeTextAuditroom = (TextView)itemView.findViewById(R.id.room_free_card_text_room);
        }
    }

    static class auditroomBusyViewHolder extends RecyclerView.ViewHolder{

        public ImageView mBusyImagePerson;
        public TextView mBusyTextAuditroom;
        public TextView mBusyTextPerson;
        public CardView mBusyCard;

        public auditroomBusyViewHolder(View itemView) {
            super(itemView);
            mBusyImagePerson = (ImageView)itemView.findViewById(R.id.room_busy_card_image_user);
            mBusyTextAuditroom = (TextView)itemView.findViewById(R.id.room_busy_card_text_room);
            mBusyTextPerson = (TextView)itemView.findViewById(R.id.room_busy_card_text_user);
            mBusyCard = (CardView)itemView.findViewById(R.id.room_busy_card);
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
                cellFree = layoutInflater.inflate(R.layout.view_room_free,parent,false);
                scaleCells(cellFree);
                viewHolder = new auditroomFreeViewHolder(cellFree);
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                cellFree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder.getLayoutPosition(),0);
                    }
                });
                final RecyclerView.ViewHolder finalViewHolder1 = viewHolder;
                cellFree.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onItemLongClick(v, finalViewHolder1.getLayoutPosition(),0);
                        return true;
                    }
                });
                break;
            case 0:
                cellBusy = layoutInflater.inflate(R.layout.view_room_busy,parent,false);
                scaleCells(cellBusy);
                viewHolder = new auditroomBusyViewHolder(cellBusy);
                final RecyclerView.ViewHolder finalViewHolder2 = viewHolder;
                cellBusy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder2.getLayoutPosition(),0);
                    }
                });
                cellBusy.setOnLongClickListener(new View.OnLongClickListener() {
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

    private void scaleCells(View cell){
        cell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Rooms.mAuditroomGrid.getHeight() / SharedPrefs.getGridRows()));
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


}
