package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.fragments.EmailFr;
import com.example.ivsmirnov.keyregistrator.interfaces.EmailInterface;

import java.util.ArrayList;

/**
 * адаптер для списка вложений и списка получателей email рассылки
 */
public class AdapterEmailExtras extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final int RECIPIENTS = 1;
    public static final int ATTACHMENTS = 2;

    public static final int VIEW_RECEPIENTS_SIMPLE = 3;
    public static final int VIEW_RECEPIENTS_ADD_NEW = 4;

    private Context mContext;
    private int mType;
    private ArrayList<String> mItems;
    private EmailInterface mEmailInterface;

    public AdapterEmailExtras(Context context, EmailInterface emailInterface, int type, ArrayList<String> items){
        this.mContext = context;
        this.mType = type;
        this.mItems = items;
        this.mEmailInterface = emailInterface;
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
                            mEmailInterface.onDeleteRecepient(finalViewHolder.getLayoutPosition(), v.getId());
                        }else if (mType == ATTACHMENTS){
                            mEmailInterface.onDeleteAttachment(finalViewHolder.getLayoutPosition(), v.getId());
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
                            mEmailInterface.onAddRecepient(v, finalViewHolder1.getLayoutPosition(), v.getId());
                        }else{
                            textInputLayout.setError(mContext.getResources().getString(R.string.email_fragment_error_not_email_entered));
                        }

                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEmailInterface.onDeleteRecepient(finalViewHolder1.getLayoutPosition(), v.getId());
                    }
                });

                break;
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mType == RECIPIENTS){
            if (!mItems.get(position).equals(EmailFr.ADD_NEW_RECIPIENT)){
                ((viewHolderEmailAttach)holder).mText.setText(mItems.get(position));
                ((viewHolderEmailAttach)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_assignment_ind_black_48dp));
                ((viewHolderEmailAttach)holder).mImageDelete.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_menu_delete));
            }
        }else if (mType == ATTACHMENTS){
            String [] split = mItems.get(position).split("/");
            ((viewHolderEmailAttach)holder).mText.setText(split[split.length -1]);
            ((viewHolderEmailAttach)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_assignment_black_48dp));
            ((viewHolderEmailAttach)holder).mImageDelete.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_menu_delete));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position).equals(EmailFr.ADD_NEW_RECIPIENT)){
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
