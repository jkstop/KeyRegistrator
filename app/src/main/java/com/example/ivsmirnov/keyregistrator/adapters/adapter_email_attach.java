package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 13.02.2016.
 */
public class adapter_email_attach extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final int RECIPIENTS = 1;
    public static final int ATTACHMENTS = 2;

    public static final int VIEW_RECEPIENTS_SIMPLE = 3;
    public static final int VIEW_RECEPIENTS_ADD_NEW = 4;

    private Context mContext;
    private int mType;
    private ArrayList<String> mItems;
    private RecycleItemClickListener mListener;

    public adapter_email_attach (Context context, RecycleItemClickListener recycleItemClickListener, int type, ArrayList<String> items){
        this.mContext = context;
        this.mType = type;
        this.mItems = items;
        this.mListener = recycleItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case VIEW_RECEPIENTS_SIMPLE:
                View rowView = layoutInflater.inflate(R.layout.card_email_attachments_and_recipients,parent,false);
                viewHolder = new viewHolderEmailAttach(rowView);
                ImageView deleteItem = (ImageView)rowView.findViewById(R.id.card_email_attach_delete);
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder.getLayoutPosition());
                    }
                });
                break;
            case VIEW_RECEPIENTS_ADD_NEW:
                View rowViewNEW = layoutInflater.inflate(R.layout.card_email_add_new_recipient,parent,false);
                viewHolder = new viewHolderAddNew(rowViewNEW);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mType == RECIPIENTS){

            if (mItems.get(position).equals("add_new")){

            }else{
                ((viewHolderEmailAttach)holder).mText.setText(mItems.get(position));
                ((viewHolderEmailAttach)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.person_male_colored));
                ((viewHolderEmailAttach)holder).mImageDelete.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_menu_delete));
            }

            //if (mItems.get(position).equals(mContext.getResources().getString(R.string.dialog_email_add_recipient))){
            //    holder.mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_add_black_24dp));
            //    holder.mImageDelete.setImageDrawable(null);
            //}else{

            //}
        }else if (mType == ATTACHMENTS){

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position).equals("add_new")){
            return VIEW_RECEPIENTS_ADD_NEW;
        }else{
            return VIEW_RECEPIENTS_SIMPLE;
        }
    }



    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class viewHolderEmailAttach extends RecyclerView.ViewHolder{

        public ImageView mImagePreview;
        public TextView mText;
        public ImageView mImageDelete;

        public viewHolderEmailAttach(View itemView) {
            super(itemView);
            mImagePreview = (ImageView)itemView.findViewById(R.id.card_email_attach_image);
            mText = (TextView)itemView.findViewById(R.id.card_email_attach_text);
            mImageDelete = (ImageView)itemView.findViewById(R.id.card_email_attach_delete);
        }
    }

    static class viewHolderAddNew extends RecyclerView.ViewHolder{
//определить кнопки, сделать обработчики при нажатии
        public viewHolderAddNew(View itemView) {
            super(itemView);
        }
    }
}
