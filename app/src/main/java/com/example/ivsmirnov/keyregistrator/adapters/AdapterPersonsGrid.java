package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class AdapterPersonsGrid extends RecyclerView.Adapter<AdapterPersonsGrid.ViewHolder>{

    public static final int SHOW_FAVORITE_PERSONS = 0;
    public static final int SHOW_ALL_PERSONS = 1;

   // private ArrayList <String> mTags;
    private ArrayList <PersonItem> mPersonList;
    private int mType;
    private Context mContext;
    private RecycleItemClickListener mListener;
    //private ArrayList<String> isFreeUsers;

    public AdapterPersonsGrid(Context c, ArrayList<PersonItem> personItems, int type, RecycleItemClickListener listener) {
        mPersonList = personItems;
        mContext = c;
        mType = type;

        this.mListener = listener;
        //isFreeUsers = SharedPrefs.getFreeUsers();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView, accessImageView;
        public TextView textLastname;
        public TextView textFirstname;
        public TextView textMidname;
        public TextView textDivision;
        public CardView personCard;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.person_card_image_user);
            accessImageView = (ImageView) itemView.findViewById(R.id.person_card_user_access_icon);
            textLastname = (TextView) itemView.findViewById(R.id.person_card_user_lastname);
            textFirstname = (TextView)itemView.findViewById(R.id.person_card_user_firstname);
            textMidname = (TextView)itemView.findViewById(R.id.person_card_user_midname);
            textDivision = (TextView)itemView.findViewById(R.id.person_card_user_division_text);
            personCard = (CardView)itemView.findViewById(R.id.person_card);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_person_item,parent,false);
        final ViewHolder viewHolder = new ViewHolder(rowView);

        rowView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v,viewHolder.getLayoutPosition(),0);
            }
        });
        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClick(v,viewHolder.getLayoutPosition(),0);
                return true;
            }
        });
        return viewHolder;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        holder.personCard.clearAnimation();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        holder.textLastname.setText(mPersonList.get(position).getLastname());
        holder.textFirstname.setText(mPersonList.get(position).getFirstname());
        holder.textMidname.setText(mPersonList.get(position).getMidname());
        holder.textDivision.setText(mPersonList.get(position).getDivision());
        holder.imageView.setImageDrawable(null);


        if (mType== SHOW_FAVORITE_PERSONS){
            if (mPersonList.get(position).getAccessType() == FavoriteDB.CLICK_USER_ACCESS){
                holder.accessImageView.setImageResource(R.drawable.ic_touch_app_black_24dp);
            } else {
                holder.accessImageView.setImageResource(R.drawable.ic_credit_card_black_24dp);
            }

//загрузка фото из хранилища
            Picasso.with(mContext)
                    .load(FavoriteDB.getPersonPhotoPath(mPersonList.get(position).getRadioLabel()))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_not_found)
                    .into(holder.imageView);


        }else if (mType == SHOW_ALL_PERSONS){

            Picasso.with(mContext)
                    .load(new File(mPersonList.get(position).getPhotoPath()))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_not_found)
                    .into(holder.imageView);

            //загрузка фото с сервера, сборка в Bitmap и вывод в ImageView. Пока так, а вообще надо хранить фотки на сервере и обращаться к ним по url
            /*new GetPersonPhoto(new GetPersonParams()
                    .setPersonTag(mPersonList.get(position).getRadioLabel())
                    .setPersonPhotoLocation(FavoriteDB.SERVER_PHOTO)
                    .setPersonPhotoDimension(FavoriteDB.PREVIEW_PHOTO)
                    .setPersonImageView(holder.imageView)
                    .setPersonImageLoadProgressBar(holder.progressBar)).execute();*/

        }
    }

    @Override
    public int getItemCount() {
        return mPersonList.size();
    }

}
