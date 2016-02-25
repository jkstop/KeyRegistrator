package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetJournal;
import com.example.ivsmirnov.keyregistrator.items.GetJournalParams;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;

import java.sql.Time;
import java.util.ArrayList;

public class adapter_journal_list extends RecyclerView.Adapter<adapter_journal_list.ViewHolderJournalItem> {

    private final Context context;
    private ArrayList<JournalItem> journalItems;
    private RecycleItemClickListener mListener;


    public adapter_journal_list(Context context, RecycleItemClickListener recycleItemClickListener, ArrayList<JournalItem> journalItems) {
        this.context = context;
        this.journalItems = journalItems;
        this.mListener = recycleItemClickListener;
    }

    @Override
    public ViewHolderJournalItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_journal_item,parent,false);
        final ViewHolderJournalItem viewHolderJournalItem = new ViewHolderJournalItem(rowView);
        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClick(v,viewHolderJournalItem.getLayoutPosition(),journalItems.get(viewHolderJournalItem.getLayoutPosition()).getTimeIn());
                return true;
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

        new GetJournal(new GetJournalParams()
                .setTimeIn(journalItems.get(position).getTimeIn())
                .setCard(holder.mCard)
                .setImagePerson(holder.mImagePerson)
                .setImageAccess(holder.mImageAccess)
                .setTextAuditroom(holder.mTextAuditroom)
                .setTextLastname(holder.mTextLastname)
                .setTextFirstname(holder.mTextFirstname)
                .setTextMidname(holder.mTextMidname)
                .setTextTimeIn(holder.mTextTimeIn)
                .setTextTimeOut(holder.mTextTimeOut),
                AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)).execute();
    }

    @Override
    public int getItemCount() {
        return journalItems.size();
    }

    static class ViewHolderJournalItem extends RecyclerView.ViewHolder{

        public TextView mTextAuditroom;
        public TextView mTextTimeIn;
        public TextView mTextTimeOut;
        public ImageView mImagePerson;
        public TextView mTextLastname;
        public TextView mTextFirstname;
        public TextView mTextMidname;
        public ImageView mImageAccess;
        public CardView mCard;

        public ViewHolderJournalItem(View itemView) {
            super(itemView);
            mTextAuditroom = (TextView)itemView.findViewById(R.id.card_journal_item_text_auditroom);
            mTextTimeIn = (TextView)itemView.findViewById(R.id.card_journal_item_time_in);
            mTextTimeOut = (TextView)itemView.findViewById(R.id.card_journal_item_time_out);
            mImagePerson = (ImageView)itemView.findViewById(R.id.card_journal_item_person_image);
            mTextLastname = (TextView)itemView.findViewById(R.id.journal_card_text_lastname);
            mTextFirstname = (TextView)itemView.findViewById(R.id.journal_card_text_firstname);
            mTextMidname = (TextView)itemView.findViewById(R.id.journal_card_text_midname);
            mImageAccess = (ImageView)itemView.findViewById(R.id.card_journal_item_access_image);
            mCard = (CardView)itemView.findViewById(R.id.card_journal_item);
        }
    }
}
