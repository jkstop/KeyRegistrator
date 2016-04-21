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
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Адаптер пользователей без карты
 */
public class AdapterFreeUsersList extends RecyclerView.Adapter<AdapterFreeUsersList.viewHolder> {

    private Context mContext;
    private ArrayList<PersonItem> mPersons;
    private RecycleItemClickListener mListener;

    public AdapterFreeUsersList(Context context, ArrayList<PersonItem> persons, RecycleItemClickListener recycleItemClickListener){
        this.mContext = context;
        this.mPersons = persons;
        this.mListener = recycleItemClickListener;

    }

    @Override
    public AdapterFreeUsersList.viewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        final View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_person_free,parent,false);
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
    public void onViewDetachedFromWindow(viewHolder holder) {
        holder.mCard.clearAnimation();
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {

        //new getPersonItem(holder.mCard, AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in))
        //        .execute(new GetPersonParams()
        //                .setPersonTag(mTags.get(position))
        //                .setPersonLastname(holder.mTextLastName)
        //                .setPersonLocation(FavoriteDB.LOCAL_USER)
        //                .setPersonPhotoDimension(-1));
        holder.mTextLastName.setText(FavoriteDB
                .getPersonInitials(FavoriteDB.SHORT_INITIALS, mPersons.get(position).getLastname(), mPersons.get(position).getFirstname(), mPersons.get(position).getMidname()));
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
        return mPersons.size();
    }

    private class getPersonItem extends AsyncTask<GetPersonParams,Void,PersonItem>{

        private CardView mPersonCard;
        private Animation mAnimation;
        private TextView mPersonInitials;

        public getPersonItem(CardView cardView, Animation animation){
            this.mPersonCard = new WeakReference<>(cardView).get();
            this.mAnimation = animation;
        }

        @Override
        protected void onPreExecute() {
            System.out.println("get person item ********************************");
            mPersonCard.setVisibility(View.INVISIBLE);
        }

        @Override
        protected PersonItem doInBackground(GetPersonParams... params) {

            PersonItem personItem = FavoriteDB.getPersonItem(params[0].getPersonTag(), params[0].getPersonLocation(),false);
            mPersonInitials = new WeakReference<>(params[0].getPersonLastname()).get();

            return personItem;
        }

        @Override
        protected void onPostExecute(PersonItem personItem) {
            System.out.println("get person item --------------------------");
            if (personItem!=null){
                if (mPersonInitials!=null) mPersonInitials.setText(FavoriteDB.getPersonInitials(FavoriteDB.SHORT_INITIALS, personItem.getLastname(),personItem.getFirstname(),personItem.getMidname()));

                mPersonCard.setVisibility(View.VISIBLE);
                mPersonCard.startAnimation(mAnimation);
            }
        }
    }
}
