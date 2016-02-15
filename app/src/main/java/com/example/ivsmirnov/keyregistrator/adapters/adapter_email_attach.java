package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.LoadImageFromWeb;
import com.example.ivsmirnov.keyregistrator.interfaces.EmailClickItemsInterface;
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
    private EmailClickItemsInterface mListener;

    public adapter_email_attach (Context context, EmailClickItemsInterface emailClickItemsInterface, int type, ArrayList<String> items){
        this.mContext = context;
        this.mType = type;
        this.mItems = items;
        this.mListener = emailClickItemsInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case VIEW_RECEPIENTS_SIMPLE:
                final View rowView = layoutInflater.inflate(R.layout.card_email_attachments_and_recipients,parent,false);
                viewHolder = new viewHolderEmailAttach(rowView);
                ImageView deleteItem = (ImageView)rowView.findViewById(R.id.card_email_attach_delete);
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mType == RECIPIENTS){
                            mListener.onDeleteRecepient(finalViewHolder.getLayoutPosition(), v.getId());
                        }else if (mType == ATTACHMENTS){
                            mListener.onDeleteAttachment(finalViewHolder.getLayoutPosition(), v.getId());
                        }
                    }
                });
                break;
            case VIEW_RECEPIENTS_ADD_NEW:
                View rowViewNEW = layoutInflater.inflate(R.layout.card_email_add_new_recipient,parent,false);
                viewHolder = new viewHolderAddNew(rowViewNEW);
                ImageView save = (ImageView)rowViewNEW.findViewById(R.id.card_email_add_new_recepient_save);
                ImageView delete = (ImageView)rowViewNEW.findViewById(R.id.card_email_add_new_recepient_delete);
                final TextInputLayout textInputLayout = (TextInputLayout)rowViewNEW.findViewById(R.id.card_email_add_new_recepient_input);

                final RecyclerView.ViewHolder finalViewHolder1 = viewHolder;
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText = textInputLayout.getEditText().getText().toString();
                        if (inputText.contains("@") && inputText.contains(".")){
                            v.setTag(textInputLayout.getEditText().getText().toString());
                            mListener.onAddRecepient(v, finalViewHolder1.getLayoutPosition(), v.getId());
                        }else{
                            textInputLayout.setError("Должен быть введен e-mail");
                        }

                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onDeleteRecepient(finalViewHolder1.getLayoutPosition(), v.getId());
                    }
                });

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
                ((viewHolderEmailAttach)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.person));
                ((viewHolderEmailAttach)holder).mImageDelete.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_menu_delete));
            }

            //if (mItems.get(position).equals(mContext.getResources().getString(R.string.dialog_email_add_recipient))){
            //    holder.mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_add_black_24dp));
            //    holder.mImageDelete.setImageDrawable(null);
            //}else{

            //}
        }else if (mType == ATTACHMENTS){
            ((viewHolderEmailAttach)holder).mText.setText(mItems.get(position));

            ((viewHolderEmailAttach)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_attach_file_black_24dp));
            ((viewHolderEmailAttach)holder).mImageDelete.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_menu_delete));
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

        public ImageView mSave;
        public ImageView mDelete;

        public viewHolderAddNew(View itemView) {
            super(itemView);
            mSave = (ImageView)itemView.findViewById(R.id.card_email_add_new_recepient_save);
            mDelete = (ImageView)itemView.findViewById(R.id.card_email_add_new_recepient_delete);
        }
    }
}
