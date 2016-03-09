package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 03.03.2016.
 */
public class adapter_free_users extends RecyclerView.Adapter<adapter_free_users.viewHolder> {

    private Context mContext;
    private ArrayList<String> mTags;
    private DataBaseFavorite mDataBaseFavorite;
    private RecycleItemClickListener mListener;

    public adapter_free_users (Context context, ArrayList<String> tags, RecycleItemClickListener recycleItemClickListener){
        this.mContext = context;
        this.mTags = tags;
        this.mListener = recycleItemClickListener;

        if (Launcher.mDataBaseFavorite!=null){
            mDataBaseFavorite = Launcher.mDataBaseFavorite;
        }else{
            mDataBaseFavorite = new DataBaseFavorite(mContext);
        }
    }

    @Override
    public adapter_free_users.viewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        final View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_free_user,parent,false);
        final viewHolder holder = new viewHolder(rowView);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, holder.getLayoutPosition(),0);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {

        new getPersonItem(holder.mCard, AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in))
                .execute(new GetPersonParams()
                        .setPersonTag(mTags.get(position))
                        .setPersonLastname(holder.mTextLastName)
                        .setPersonLocation(DataBaseFavorite.LOCAL_USER)
                        .setPersonPhotoDimension(-1)
                        .setDatabase(mDataBaseFavorite));
    }

    static class viewHolder extends RecyclerView.ViewHolder {

        public TextView mTextLastName;
        public CardView mCard;
        public viewHolder(View itemView) {
            super(itemView);
            mTextLastName = (TextView)itemView.findViewById(R.id.card_free_user_lastname);
            mCard = (CardView)itemView.findViewById(R.id.card_free_user);
        }
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    private class getPersonItem extends AsyncTask<GetPersonParams,Void,PersonItem>{

        private CardView mPersonCard;
        private Animation mAnimation;
        private TextView mPersonInitials;

        public getPersonItem(CardView cardView, Animation animation){
            this.mPersonCard = new WeakReference<CardView>(cardView).get();
            this.mAnimation = animation;
        }

        @Override
        protected void onPreExecute() {
            mPersonCard.setVisibility(View.INVISIBLE);
        }

        @Override
        protected PersonItem doInBackground(GetPersonParams... params) {
            DataBaseFavorite dataBaseFavorite = params[0].getDataBaseFavorite();
            if (dataBaseFavorite!=null){
                PersonItem personItem = dataBaseFavorite.getPersonItem(params[0].getPersonTag(), params[0].getPersonLocation(), params[0].getPersonPhotoDimension());

                mPersonInitials = new WeakReference<TextView>(params[0].getPersonLastname()).get();

                return personItem;
            }
            return null;
        }

        @Override
        protected void onPostExecute(PersonItem personItem) {
            if (personItem!=null){
                if (mPersonInitials!=null) mPersonInitials.setText(Persons_Fragment.getPersonInitials(personItem.getLastname(),personItem.getFirstname(),personItem.getMidname()));

                mPersonCard.setVisibility(View.VISIBLE);
                mPersonCard.startAnimation(mAnimation);
            }
        }
    }
}
