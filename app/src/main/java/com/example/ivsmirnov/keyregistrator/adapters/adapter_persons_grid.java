package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetPersons;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

public class adapter_persons_grid extends RecyclerView.Adapter<adapter_persons_grid.ViewHolder>{

    private SparseArray <String> card;
    private ArrayList <PersonItem> allItems;
    private int mType;
    private Context context;
    private LayoutInflater inflater;
    private RecycleItemClickListener mListener;
    public  DataBaseFavorite mDataBaseFavorite;

    public adapter_persons_grid(Context c, ArrayList<PersonItem> all,int type,RecycleItemClickListener listener) {
        allItems = all;
        context = c;
        mType = type;
        if (Launcher.mDataBaseFavorite!=null){
            mDataBaseFavorite = Launcher.mDataBaseFavorite;
            Log.d("database","getFromMain");
        }else{
            mDataBaseFavorite = new DataBaseFavorite(context);
            Log.d("database","createNew");
        }
        this.mListener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textLastname;
        public TextView textFirstname;
        public TextView textMidname;
        public TextView textDivision;
        public CardView personCard;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image_sql);
            textLastname = (TextView) itemView.findViewById(R.id.person_card_text_lastname);
            textFirstname = (TextView)itemView.findViewById(R.id.person_card_text_firstname);
            textMidname = (TextView)itemView.findViewById(R.id.person_card_text_midname);
            textDivision = (TextView)itemView.findViewById(R.id.person_card_text_division);
            personCard = (CardView)itemView.findViewById(R.id.person_card);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_person,parent,false);
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
        //holder.textLastname.setText(allItems.get(position).getLastname());
        //holder.textFirstname.setText(allItems.get(position).getFirstname());
        //holder.textMidname.setText(allItems.get(position).getMidname());
        //holder.textDivision.setText(allItems.get(position).getDivision());
        Animation fadeInanimation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        if (mType== Values.SHOW_FAVORITE_PERSONS){
            new GetPersons(holder.personCard, fadeInanimation).execute(new GetPersonParams().setPersonTag(allItems.get(position).getRadioLabel())
            .setDatabase(mDataBaseFavorite)
            .setPersonImageView(holder.imageView)
            .setPersonLastname(holder.textLastname)
            .setPersonFirstname(holder.textFirstname)
            .setPersonMidname(holder.textMidname)
            .setPersonDivision(holder.textDivision)
            .setPersonLocation(DataBaseFavorite.LOCAL_USER)
            .setPersonPhotoDimension(DataBaseFavorite.PREVIEW_PHOTO));
            //new GetPersonPhoto(context,allItems.get(position).getRadioLabel(), mDataBaseFavorite, holder.imageView, GetPersonPhoto.PREVIEW_IMAGE, GetPersonPhoto.LOCAL_PHOTO).execute();
            //new getPhoto(holder.imageView).execute(allItems.get(position).getRadioLabel());
            //photo = allItems.get(position).getPhotoPreview();
        }else if (mType==Values.SHOW_ALL_PERSONS){
            new GetPersons(holder.personCard, fadeInanimation).execute(new GetPersonParams().setPersonTag(allItems.get(position).getRadioLabel())
                    .setDatabase(mDataBaseFavorite)
                    .setPersonImageView(holder.imageView)
                    .setPersonLastname(holder.textLastname)
                    .setPersonFirstname(holder.textFirstname)
                    .setPersonMidname(holder.textMidname)
                    .setPersonDivision(holder.textDivision)
                    .setPersonLocation(DataBaseFavorite.SERVER_USER)
                    .setPersonPhotoDimension(DataBaseFavorite.PREVIEW_PHOTO));
            //new GetPersonPhoto(context,allItems.get(position).getRadioLabel(), mDataBaseFavorite, holder.imageView, GetPersonPhoto.PREVIEW_IMAGE, GetPersonPhoto.SERVER_PHOTO).execute();
            //new GetPersonPhoto(allItems.get(position).getRadioLabel(), mDataBaseFavorite, holder.imageView, GetPersonPhoto.ORIGINAL_IMAGE).execute();
            //photo = allItems.get(position).getPhotoOriginal();
        }

  /*      if (photo!=null){
            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            holder.imageView.setImageBitmap(bitmap);

        }*/


    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

}
