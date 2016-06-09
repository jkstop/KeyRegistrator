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
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.ArrayList;

public class AdapterJournalList extends RecyclerView.Adapter<AdapterJournalList.ViewHolderJournalItem> {

    private final Context context;
    private ArrayList<JournalItem> mJournalItems;
    private RecycleItemClickListener mListener;
    private JournalItem mBindedItem;


    public AdapterJournalList(Context context, RecycleItemClickListener recycleItemClickListener, ArrayList<JournalItem> journalItems) {
        this.context = context;
        this.mJournalItems = journalItems;
        this.mListener = recycleItemClickListener;
    }

    @Override
    public ViewHolderJournalItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_journal_item,parent,false);
        final ViewHolderJournalItem viewHolderJournalItem = new ViewHolderJournalItem(rowView);
        rowView.findViewById(R.id.journal_card_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, viewHolderJournalItem.getLayoutPosition(), v.getId());
            }
        });
        return viewHolderJournalItem;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolderJournalItem holder) {
        holder.mCard.clearAnimation();
    }

    @Override
    public void onBindViewHolder(ViewHolderJournalItem holder, int position) {
        mBindedItem = mJournalItems.get(position);

        holder.mTextPesonInitials.setText(mBindedItem.getPersonInitials());
        holder.mTextAuditroom.setText(mBindedItem.getAuditroom());
        holder.mTextTimeIn.setText(String.valueOf(new Time(mBindedItem.getTimeIn())));

        if (mBindedItem.getTimeOut() == 0){
            holder.mTextTimeOut.setText(R.string.journal_card_during_lesson);
        } else {
            holder.mTextTimeOut.setText(String.valueOf(new Time(mBindedItem.getTimeOut())));
        }

        if (mBindedItem.getAccessType() == FavoriteDB.CLICK_USER_ACCESS){
            holder.mImageAccess.setImageResource(R.drawable.ic_touch_app_white_24dp);
        } else {
            holder.mImageAccess.setImageResource(R.drawable.ic_credit_card_white_24dp);
        }

        Picasso.with(context)
                .load(FavoriteDB.getPersonPhotoPath(mBindedItem.getPersonTag()))
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_user_not_found)
                .into(holder.mImagePerson);
    }

    @Override
    public int getItemCount() {
        return mJournalItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mJournalItems.get(position).getTimeIn();
    }

    static class ViewHolderJournalItem extends RecyclerView.ViewHolder{

        public TextView mTextAuditroom;
        public TextView mTextTimeIn;
        public TextView mTextTimeOut;
        public ImageView mImagePerson;
        public TextView mTextPesonInitials;
        public ImageView mImageAccess;
        public ImageView mDelete;
        public CardView mCard;

        public ViewHolderJournalItem(View itemView) {
            super(itemView);
            mTextAuditroom = (TextView)itemView.findViewById(R.id.journal_card_room);
            mTextTimeIn = (TextView)itemView.findViewById(R.id.journal_card_time_in);
            mTextTimeOut = (TextView)itemView.findViewById(R.id.journal_card_time_out);
            mImagePerson = (ImageView)itemView.findViewById(R.id.journal_card_photo);
            mTextPesonInitials = (TextView)itemView.findViewById(R.id.journal_card_user);
            mImageAccess = (ImageView)itemView.findViewById(R.id.journal_card_access_icon);
            mCard = (CardView)itemView.findViewById(R.id.card_journal_item);
            mDelete = (ImageView)itemView.findViewById(R.id.journal_card_delete);
        }
    }
}
