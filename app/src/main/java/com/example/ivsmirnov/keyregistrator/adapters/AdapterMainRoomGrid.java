package com.example.ivsmirnov.keyregistrator.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetPersons;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.MainFr;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.util.ArrayList;

public class AdapterMainRoomGrid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<RoomItem> mRoomItems;
    private RecycleItemClickListener mListener;

    public AdapterMainRoomGrid(ArrayList<RoomItem> roomItems, RecycleItemClickListener listener) {
        this.mRoomItems = roomItems;
        this.mListener = listener;
    }

    static class auditroomFreeViewHolder extends RecyclerView.ViewHolder{

        public ImageView mFreeImageKey;
        public TextView mFreeTextAuditroom;

        public auditroomFreeViewHolder(View itemView) {
            super(itemView);
            mFreeImageKey = (ImageView)itemView.findViewById(R.id.card_auditroom_free_image_key);
            mFreeTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_free_text_auditroom);
        }
    }

    static class auditroomBusyViewHolder extends RecyclerView.ViewHolder{

        public ImageView mBusyImagePerson;
        public ImageView mBusyImageKey;
        public TextView mBusyTextAuditroom;
        public TextView mBusyTextPerson;
        public CardView mBusyCard;

        public auditroomBusyViewHolder(View itemView) {
            super(itemView);
            mBusyImagePerson = (ImageView)itemView.findViewById(R.id.card_auditroom_busy_image_person);
            mBusyImageKey = (ImageView)itemView.findViewById(R.id.card_auditroom_busy_image_key);
            mBusyTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_auditroom);
            mBusyTextPerson = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_person);
            mBusyCard = (CardView)itemView.findViewById(R.id.card_auditroom_busy);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mRoomItems.get(position).getStatus();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case 1:
                final View rootViewFree = layoutInflater.inflate(R.layout.card_auditroom_free,parent,false);
                viewHolder = new auditroomFreeViewHolder(rootViewFree);
                rootViewFree.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getItemScaleHeight()));
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                rootViewFree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder.getLayoutPosition(),0);
                    }
                });
                final RecyclerView.ViewHolder finalViewHolder1 = viewHolder;
                rootViewFree.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onItemLongClick(v, finalViewHolder1.getLayoutPosition(),0);
                        return true;
                    }
                });
                break;
            case 0:
                View rootViewBusy = layoutInflater.inflate(R.layout.card_auditroom_busy,parent,false);
                viewHolder = new auditroomBusyViewHolder(rootViewBusy);
                rootViewBusy.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getItemScaleHeight()));
                final RecyclerView.ViewHolder finalViewHolder2 = viewHolder;
                rootViewBusy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder2.getLayoutPosition(),0);
                    }
                });
                rootViewBusy.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onItemLongClick(v,finalViewHolder2.getLayoutPosition(),0);
                        return true;
                    }
                });
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 1:
                ((auditroomFreeViewHolder) holder).mFreeTextAuditroom.setText(mRoomItems.get(position).getAuditroom());
                break;
            case 0:
                ((auditroomBusyViewHolder)holder).mBusyTextAuditroom.setText(mRoomItems.get(position).getAuditroom());
                ((auditroomBusyViewHolder)holder).mBusyTextPerson.setText(mRoomItems.get(position).getLastVisiter());

                RelativeLayout.LayoutParams imageKeyParams = (RelativeLayout.LayoutParams) ((auditroomBusyViewHolder)holder).mBusyImageKey.getLayoutParams();
                imageKeyParams.width = imageKeyParams.height = (int)(getItemScaleHeight()/5.6);

                RelativeLayout.LayoutParams imagePersonParams = (RelativeLayout.LayoutParams) ((auditroomBusyViewHolder)holder).mBusyImagePerson.getLayoutParams();
                imagePersonParams.width = (int) (getItemScaleHeight()/1.4);

                ((auditroomBusyViewHolder)holder).mBusyImagePerson.setLayoutParams(imagePersonParams);
                ((auditroomBusyViewHolder)holder).mBusyImageKey.setLayoutParams(imageKeyParams);

                //загрузка и отображение фото из БД
                new GetPersons(App.getAppContext(),null, AnimationUtils.loadAnimation(App.getAppContext(),android.R.anim.fade_in)).execute(new GetPersonParams()
                        .setIsAnimatedPhoto(true)
                        .setPersonImageView(((auditroomBusyViewHolder)holder).mBusyImagePerson)
                        .setPersonLocation(FavoriteDB.LOCAL_USER)
                        .setPersonPhotoDimension(FavoriteDB.PREVIEW_PHOTO)
                        .setPersonTag(mRoomItems.get(position).getTag()));

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mRoomItems.size();
    }

    private int getItemScaleHeight(){
        int recyclerHeight = MainFr.mAuditroomGrid.getHeight();
        int recyclerChilds = mRoomItems.size();
        int recyclerRows = (int) Math.ceil((double) recyclerChilds / Settings.getAuditroomColumnsCount());
        return recyclerHeight/recyclerRows;
    }

/*
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;

        Button button;
        ImageView image;
        TextView textAud,textPerson;

        if (convertView == null) {
                if (mRoomItems.get(position).Status==1){
                    view = inflater.inflate(R.layout.cell_for_grid_is_free, null);
                }else{
                    view = inflater.inflate(R.layout.cell_for_grid_is_busy,null);
                }
            }else{
                view = convertView;
            }

        //int space = (int) context.getResources().getDimension(R.dimen.grid_vertical_spacing);
        //int heightGrid = MainFr.gridView.getHeight();
        //int childCount = MainFr.gridView.getCount();
        //int rows;
       // rows = (int) Math.ceil((double) childCount / preferences.getInt(Values.COLUMNS_AUD_COUNT, 1));
        //int btnHeight = heightGrid/rows - space;

        //AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //        btnHeight);
        //view.setLayoutParams(layoutParams);

        if (mRoomItems.get(position).Status==1){
            image = (ImageView)view.findViewById(R.id.imageButton);
            textAud = (TextView)view.findViewById(R.id.textButton);
            textAud.setText(mRoomItems.get(position).Auditroom);
            view.setBackgroundResource(R.drawable.button_background);
            image.setImageResource(R.drawable.key_colored);
        }else{
            image = (ImageView)view.findViewById(R.id.image_key_person);
            textAud = (TextView)view.findViewById(R.id.text_aud);
            textPerson = (TextView)view.findViewById(R.id.textButton);

            if (!mRoomItems.get(position).Photo.equalsIgnoreCase("")){
                ImageLoader imageLoader = ImageLoader.getInstance();
                if (!imageLoader.isInited()){
                    imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                }
                imageLoader.displayImage("file://"+mRoomItems.get(position).Photo,image);
            }else{
                image.setImageResource(R.drawable.person_male_colored);
            }
            textAud.setText(String.valueOf(mRoomItems.get(position).Auditroom));
            textPerson.setText(mRoomItems.get(position).LastVisiter);

            if (mRoomItems.get(position).Tag.equalsIgnoreCase("99 80 DC 1A 00 00")
                    ||mRoomItems.get(position).Tag.equalsIgnoreCase("EF 36 83 D9 00 00")
                    ||mRoomItems.get(position).Tag.equalsIgnoreCase("0F 4B 7C D9 00 00")
                    ||mRoomItems.get(position).Tag.equalsIgnoreCase("69 B1 D2 29 00 00")){
                view.setBackgroundResource(R.drawable.button_background_support);
            }else{
                view.setBackgroundResource(R.drawable.button_background_selected);
            }
        }

        view.setTag(mRoomItems.get(position).Auditroom);

        return view;
    }*/
}
