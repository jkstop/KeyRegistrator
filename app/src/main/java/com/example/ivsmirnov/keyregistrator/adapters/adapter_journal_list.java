package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.JournalItem;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
                mListener.onItemLongClick(v,viewHolderJournalItem.getLayoutPosition(),journalItems.get(viewHolderJournalItem.getLayoutPosition()).TimeIn);
                return true;
            }
        });
        return viewHolderJournalItem;
    }

    @Override
    public void onBindViewHolder(ViewHolderJournalItem holder, int position) {
        holder.mTextAuditroom.setText(journalItems.get(position).Auditroom);
        holder.mTextTimeIn.setText(String.valueOf(new Time(journalItems.get(position).TimeIn)));
        if (journalItems.get(position).TimeOut==0){
            holder.mTextTimeOut.setText(R.string.journal_card_during_lesson);
        }else{
            holder.mTextTimeOut.setText(String.valueOf(new Time(journalItems.get(position).TimeOut)));
        }
        holder.mTextLastname.setText(journalItems.get(position).PersonLastname);
        holder.mTextMidname.setText(journalItems.get(position).PersonMidname);
        holder.mTextFirstname.setText(journalItems.get(position).PersonFirstname);

        if (journalItems.get(position).AccessType==1){
            holder.mImageAccess.setImageResource(R.drawable.ic_credit_card_black_18dp);
        }else{
            holder.mImageAccess.setImageResource(R.drawable.ic_click_icon);
        }

        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()){
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }

        imageLoader.displayImage("file://"+journalItems.get(position).PersonPhoto,holder.mImagePerson);
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
        }
    }
/*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        card = items.get(position);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list,parent,false);
        textAud = (TextView)rowView.findViewById(R.id.auditoryName);
        textName = (TextView)rowView.findViewById(R.id.teacherName);
        textTime = (TextView)rowView.findViewById(R.id.time);
        textTimePut = (TextView)rowView.findViewById(R.id.timePut);


        if (card.get(0)!=null&&card.get(0).equals("_")){
            textAud.setText(" ");
        }else{
            textAud.setText(card.get(0));
        }


        textName.setText(card.get(1));

        if (Long.parseLong(card.get(2))==1){
            textTime.setText("");
            textName.setGravity(Gravity.CENTER);
        }else{
            textTime.setText(String.valueOf(new Time(Long.parseLong(card.get(2)))));
        }
        if (Long.parseLong(card.get(3))==0){
            textTimePut.setText("Не сдал");
        }else if (Long.parseLong(card.get(3))==1){
            textTimePut.setText("");
        }else{
            textTimePut.setText(String.valueOf(new Time(Long.parseLong(card.get(3)))));
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public String getString(){
        return textAud.getText() + " " + textName.getText() + " " + textTime.getText() + "    " + textTimePut.getText();
    }*/
}
