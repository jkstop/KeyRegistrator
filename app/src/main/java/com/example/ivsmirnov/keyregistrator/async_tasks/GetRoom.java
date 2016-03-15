package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.items.GetRoomParams;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.App;

import java.lang.ref.WeakReference;

/**
 * Created by ivsmirnov on 15.03.2016.
 */
public class GetRoom extends AsyncTask <GetRoomParams, Void, RoomItem> {

    private CardView mRoomCard;
    private Animation mAnimation;
    private TextView mPersonTextInitials;
    private ImageView mPersonImage;

    public GetRoom (CardView roomCard){
        this.mRoomCard = roomCard;
        this.mAnimation = AnimationUtils.loadAnimation(App.getAppContext(), android.R.anim.fade_in);
    }

    @Override
    protected void onPreExecute() {

        mRoomCard.setVisibility(View.INVISIBLE);
    }

    @Override
    protected RoomItem doInBackground(GetRoomParams... params) {
        mPersonTextInitials = new WeakReference<TextView>(params[0].getPersonInitialsText()).get();
        mPersonImage = new WeakReference<ImageView>(params[0].getPersonImage()).get();
        return DataBaseRooms.getRoomItem(null, params[0].getRoomName());
    }

    @Override
    protected void onPostExecute(RoomItem roomItem) {
        if (mPersonTextInitials!=null) mPersonTextInitials.setText(roomItem.getLastVisiter());

        byte[] decodedString = Base64.decode(roomItem.getPhoto(), Base64.DEFAULT);
        if (mPersonImage!=null) mPersonImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));

        mRoomCard.setVisibility(View.VISIBLE);
        mRoomCard.startAnimation(mAnimation);
    }
}
