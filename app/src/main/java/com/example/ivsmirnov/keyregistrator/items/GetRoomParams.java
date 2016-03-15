package com.example.ivsmirnov.keyregistrator.items;

import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ivsmirnov on 15.03.2016.
 */
public class GetRoomParams {

    private String RoomName;
    private TextView PersonInitialsText;
    private ImageView PersonImage;
    private CardView RoomCard;

    public GetRoomParams setRoomCard(CardView roomCard){
        this.RoomCard = roomCard;
        return this;
    }

    public GetRoomParams setRoomName(String roomName){
        this.RoomName = roomName;
        return this;
    }

    public GetRoomParams setPersonInitialsText (TextView personInitialsText){
        this.PersonInitialsText = personInitialsText;
        return this;
    }

    public GetRoomParams setPersonImage (ImageView personImage){
        this.PersonImage = personImage;
        return this;
    }

    public String getRoomName(){
        return RoomName;
    }

    public TextView getPersonInitialsText(){
        return PersonInitialsText;
    }

    public ImageView getPersonImage(){
        return PersonImage;
    }

    public CardView getRoomCard(){
        return RoomCard;
    }

}
