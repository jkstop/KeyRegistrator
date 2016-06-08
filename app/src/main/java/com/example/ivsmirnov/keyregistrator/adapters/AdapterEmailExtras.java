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
import com.example.ivsmirnov.keyregistrator.custom_views.EmailRecipientsPreference;
import com.example.ivsmirnov.keyregistrator.interfaces.EmailInterface;

import java.util.ArrayList;

/**
 * адаптер для списка вложений и списка получателей email рассылки
 */
public class AdapterEmailExtras extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final String ADD_NEW_ITEM = "add_new_item";

    public static final int RECIPIENTS = 1;
    public static final int ATTACHMENTS = 2;
    public static final int ROOMS = 3;

    public static final int VIEW_RECEPIENTS_SIMPLE = 10;
    public static final int VIEW_RECEPIENTS_ADD_NEW = 20;

    private Context mContext;
    private int mType;
    private ArrayList<String> mItems;
    private Callback mCallback;

    public AdapterEmailExtras(Context context, int type, ArrayList<String> items, Callback callback){
        this.mContext = context;
        this.mType = type;
        this.mItems = items;
        mCallback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case VIEW_RECEPIENTS_SIMPLE:
                final View rowView = layoutInflater.inflate(R.layout.view_email_extra_item,parent,false);
                viewHolder = new viewHolderEmailExtra(rowView);
                ImageView deleteItem = (ImageView)rowView.findViewById(R.id.card_email_extra_delete);
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallback.onDeleteItem(finalViewHolder.getLayoutPosition());
                            //mEmailInterface.onDeleteRecepient(finalViewHolder.getLayoutPosition(), v.getId());
                            //mEmailInterface.onDeleteAttachment(finalViewHolder.getLayoutPosition(), v.getId());
                    }
                });
                break;
            case VIEW_RECEPIENTS_ADD_NEW:
                View rowViewNEW = layoutInflater.inflate(R.layout.view_email_add_new_recipient,parent,false);
                viewHolder = new viewHolderAddNew(rowViewNEW);
                ImageView save = (ImageView)rowViewNEW.findViewById(R.id.card_email_extra_new_recipient_add);
                ImageView delete = (ImageView)rowViewNEW.findViewById(R.id.card_email_extra_new_recipient_delete);
                final TextInputLayout textInputLayout = (TextInputLayout)rowViewNEW.findViewById(R.id.card_email_extra_new_recipient_input);
                switch (mType){
                    case RECIPIENTS:
                        textInputLayout.setHint("Новый пользователь");
                        break;
                    case ROOMS:
                        textInputLayout.setHint("Новое помещение");
                        break;
                    default:
                        break;
                }
                final RecyclerView.ViewHolder finalViewHolder1 = viewHolder;
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText = textInputLayout.getEditText().getText().toString();

                        if (inputText.length() == 0){
                            textInputLayout.setError(mContext.getResources().getString(R.string.input_empty_error));
                        }else{
                            switch (mType){
                                case RECIPIENTS:
                                    if (inputText.contains("@") && inputText.contains(".")){
                                        mCallback.onAddItem(inputText);
                                    } else {
                                        textInputLayout.setError(mContext.getResources().getString(R.string.email_fragment_error_not_email_entered));
                                    }
                                    break;
                                case ROOMS:
                                    mCallback.onAddItem(inputText);
                                    break;
                                default:
                                    break;
                            }
                            textInputLayout.getEditText().setText(null);
                        }

                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallback.onDeleteItem(finalViewHolder1.getLayoutPosition());
                        //mEmailInterface.onDeleteRecepient(finalViewHolder1.getLayoutPosition(), v.getId());
                    }
                });

                break;
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (mType){
            case RECIPIENTS:
                if (!mItems.get(position).equals(ADD_NEW_ITEM)){
                    ((viewHolderEmailExtra)holder).mTextHead.setText(mItems.get(position));
                    ((viewHolderEmailExtra)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_person_black_36dp));
                } else {
                    ((viewHolderAddNew)holder).mInput.requestFocus();
                }
                break;
            case ATTACHMENTS:
                String [] split = mItems.get(position).split("/");
                ((viewHolderEmailExtra)holder).mTextHead.setText(split[split.length -1]);
                ((viewHolderEmailExtra)holder).mTextSubHead.setText(mItems.get(position));
                ((viewHolderEmailExtra)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_insert_drive_file_black_36dp));
                break;
            case ROOMS:
                if (!mItems.get(position).equals(ADD_NEW_ITEM)){
                    ((viewHolderEmailExtra)holder).mTextHead.setText(mItems.get(position));
                    ((viewHolderEmailExtra)holder).mImagePreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_room_black_36dp));
                }else {
                    ((viewHolderAddNew)holder).mInput.requestFocus();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position).equals(ADD_NEW_ITEM)){
            return VIEW_RECEPIENTS_ADD_NEW;
        }else{
            return VIEW_RECEPIENTS_SIMPLE;
        }
    }



    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class viewHolderEmailExtra extends RecyclerView.ViewHolder{

        public ImageView mImagePreview;
        public TextView mTextHead, mTextSubHead;
        public ImageView mImageDelete;

        public viewHolderEmailExtra(View itemView) {
            super(itemView);
            mImagePreview = (ImageView)itemView.findViewById(R.id.card_email_extra_icon);
            mTextHead = (TextView)itemView.findViewById(R.id.card_email_extra_title);
            mTextSubHead = (TextView)itemView.findViewById(R.id.card_email_extra_subtitle);
            mImageDelete = (ImageView)itemView.findViewById(R.id.card_email_extra_delete);
        }
    }

    static class viewHolderAddNew extends RecyclerView.ViewHolder{

        public ImageView mSave;
        public ImageView mDelete;
        public TextInputLayout mInput;

        public viewHolderAddNew(View itemView) {
            super(itemView);
            mSave = (ImageView)itemView.findViewById(R.id.card_email_extra_new_recipient_add);
            mDelete = (ImageView)itemView.findViewById(R.id.card_email_extra_new_recipient_delete);
            mInput = (TextInputLayout)itemView.findViewById(R.id.card_email_extra_new_recipient_input);
        }
    }

   public interface Callback{
       void onDeleteItem(int position);
       void onAddItem(String item);
   }

}
