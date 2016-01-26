package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 26.01.2016.
 */
public class adapter_main_auditrooms_grid_resize extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<RoomItem> mRoomItems;
    private SharedPreferences mSharedPreferences;

    public  adapter_main_auditrooms_grid_resize (Context context, ArrayList<RoomItem> roomItems){
        this.mContext = context;
        this.mRoomItems = roomItems;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    static class cardViewHolder extends RecyclerView.ViewHolder{

        public TextView mTextAuditroomName;

        public cardViewHolder(View itemView) {
            super(itemView);
            mTextAuditroomName = (TextView)itemView.findViewById(R.id.card_auditroom_free_text_auditroom);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View rootView = mInflater.inflate(R.layout.card_auditroom_free,parent,false);
        RecyclerView.LayoutParams viewParams = (RecyclerView.LayoutParams) rootView.getLayoutParams();
        viewParams.height = getItemScaleHeight();
        return new cardViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((cardViewHolder)holder).mTextAuditroomName.setText(mRoomItems.get(position).Auditroom);
    }

    @Override
    public int getItemCount() {
        return mRoomItems.size();
    }


    private int getItemScaleHeight(){
        int  recyclerHeight;
        if (Dialog_Fragment.mFrameGrid!=null){
            recyclerHeight = Dialog_Fragment.mFrameGrid.getHeight();
        }else{
            recyclerHeight = Main_Fragment.mAuditroomGrid.getHeight();
        }
        int recyclerChilds = mRoomItems.size();
        int recyclerRows = (int) Math.ceil((double) recyclerChilds / mSharedPreferences.getInt(Values.COLUMNS_AUD_COUNT, 1));
        return recyclerHeight/recyclerRows;
    }
}
