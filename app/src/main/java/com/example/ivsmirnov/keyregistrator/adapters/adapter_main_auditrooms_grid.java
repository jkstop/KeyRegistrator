package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;

import java.util.ArrayList;

public class adapter_main_auditrooms_grid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<RoomItem> mRoomItems;
    private RecycleItemClickListener mListener;
    private LayoutInflater inflater;
    private SharedPreferences preferences;


    public adapter_main_auditrooms_grid(Context c, ArrayList<RoomItem> roomItems, RecycleItemClickListener listener) {
        context = c;
        this.mRoomItems = roomItems;
        this.mListener = listener;
        inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
        public TextView mBusyTextAuditroom;
        public TextView mBusyTextPerson;

        public auditroomBusyViewHolder(View itemView) {
            super(itemView);
            mBusyImagePerson = (ImageView)itemView.findViewById(R.id.card_auditroom_busy_image_person);
            mBusyTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_auditroom);
            mBusyTextPerson = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_person);
        }
    }
    /*
        public class ViewHolder extends RecyclerView.ViewHolder{

            public ImageView mFreeImageKey;
            TextView mFreeTextAuditroom;

            public ImageView mBusyImagePerson;
            public TextView mBusyTextAuditroom;
            public TextView mBusyTextPerson;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);
                if (viewType==0){
                    mFreeImageKey = (ImageView)itemView.findViewById(R.id.card_auditroom_free_image_key);
                    mFreeTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_free_text_auditroom);
                }else if (viewType==1){
                    mBusyImagePerson = (ImageView)itemView.findViewById(R.id.card_auditroom_busy_image_person);
                    mBusyTextAuditroom = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_auditroom);
                    mBusyTextPerson = (TextView)itemView.findViewById(R.id.card_auditroom_busy_text_person);
                }
            }
        }
    */
    @Override
    public int getItemViewType(int position) {
        return mRoomItems.get(position).Status;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType){
            case 1:
                final View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_auditroom_free,parent,false);
                viewHolder = new auditroomFreeViewHolder(rootView);
                rootView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                final RecyclerView.ViewHolder finalViewHolder = viewHolder;
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder.getLayoutPosition());
                    }
                });
                final RecyclerView.ViewHolder finalViewHolder1 = viewHolder;
                rootView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mListener.onItemLongClick(v, finalViewHolder1.getLayoutPosition(),0);
                        return true;
                    }
                });
                break;
            case 0:
                View rootViewBusy = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_auditroom_busy,parent,false);
                viewHolder = new auditroomBusyViewHolder(rootViewBusy);
                rootViewBusy.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                final RecyclerView.ViewHolder finalViewHolder2 = viewHolder;
                rootViewBusy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(v, finalViewHolder2.getLayoutPosition());
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
                ((auditroomFreeViewHolder) holder).mFreeTextAuditroom.setText(mRoomItems.get(position).Auditroom);
                break;
            case 0:
                ((auditroomBusyViewHolder)holder).mBusyTextAuditroom.setText(mRoomItems.get(position).Auditroom);
                ((auditroomBusyViewHolder)holder).mBusyTextPerson.setText(mRoomItems.get(position).LastVisiter);

                Bitmap bitmap;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                if (mRoomItems.get(position).Photo!=null){
                    byte[] decodedString = Base64.decode(mRoomItems.get(position).Photo, Base64.DEFAULT);
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
                    options.inSampleSize = DataBaseFavorite.calculateInSampleSize(options, 120, 160);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
                    ((auditroomBusyViewHolder)holder).mBusyImagePerson.setImageBitmap(bitmap);
                }

                break;
        }
    }

 //   @Override
 //   public void onBindViewHolder(auditroomFreeViewHolder holder, int position) {
 //       holder.mFreeTextAuditroom.setText(mRoomItems.get(position).Auditroom);
 //   }

    @Override
    public int getItemCount() {
        return mRoomItems.size();
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
        //int heightGrid = Main_Fragment.gridView.getHeight();
        //int childCount = Main_Fragment.gridView.getCount();
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
