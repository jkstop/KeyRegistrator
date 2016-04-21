package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetJournal;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.GetJournalParams;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
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
        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClick(v,viewHolderJournalItem.getLayoutPosition(), mJournalItems.get(viewHolderJournalItem.getLayoutPosition()).getTimeIn());
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
            holder.mImageAccess.setImageResource(R.drawable.ic_touch_app_black_24dp);
        } else {
            holder.mImageAccess.setImageResource(R.drawable.ic_credit_card_black_24dp);
        }

        Picasso.with(context)
                .load(FavoriteDB.getPersonPhotoPath(mBindedItem.getPersonTag()))
                .resizeDimen(R.dimen.journal_item_card_image_width, R.dimen.journal_item_card_height)
                .centerCrop()
                .placeholder(R.drawable.ic_user_not_found)
                .into(holder.mImagePerson);

        /*new GetJournal(new GetJournalParams()
                .setTimeIn(mJournalItemTags.get(position))
                .setCard(holder.mCard)
                .setImagePerson(holder.mImagePerson)
                .setImageAccess(holder.mImageAccess)
                .setTextAuditroom(holder.mTextAuditroom)
                .setTextInitials(holder.mTextPesonInitials)
                //.setTextLastname(holder.mTextLastname)
                //.setTextFirstname(holder.mTextFirstname)
                //.setTextMidname(holder.mTextMidname)
                .setTextTimeIn(holder.mTextTimeIn)
                .setTextTimeOut(holder.mTextTimeOut),
                AnimationUtils.loadAnimation(context, android.R.anim.fade_in)).execute();*/
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
        //public TextView mTextLastname;
        //public TextView mTextFirstname;
        //public TextView mTextMidname;
        public ImageView mImageAccess;
        public CardView mCard;

        public ViewHolderJournalItem(View itemView) {
            super(itemView);
            mTextAuditroom = (TextView)itemView.findViewById(R.id.card_journal_item_text_auditroom);
            mTextTimeIn = (TextView)itemView.findViewById(R.id.card_journal_item_time_in);
            mTextTimeOut = (TextView)itemView.findViewById(R.id.card_journal_item_time_out);
            mImagePerson = (ImageView)itemView.findViewById(R.id.card_journal_item_person_image);
            mTextPesonInitials = (TextView)itemView.findViewById(R.id.card_journal_item_person_initials);
            //mTextLastname = (TextView)itemView.findViewById(R.id.journal_card_text_lastname);
            //mTextFirstname = (TextView)itemView.findViewById(R.id.journal_card_text_firstname);
            //mTextMidname = (TextView)itemView.findViewById(R.id.journal_card_text_midname);
            mImageAccess = (ImageView)itemView.findViewById(R.id.card_journal_item_access_image);
            mCard = (CardView)itemView.findViewById(R.id.card_journal_item);
        }
    }
}
