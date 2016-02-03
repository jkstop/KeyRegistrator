package com.example.ivsmirnov.keyregistrator.items;

import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.RoomInterface;

/**
 * Created by ivsmirnov on 03.02.2016.
 */
public class CloseRoomsParams {

    private String Tag;
    private RoomInterface RoomInterface;


    public CloseRoomsParams setTag(String tag){
        this.Tag = tag;
        return this;
    }

    public CloseRoomsParams setRoomInterface(RoomInterface roomInterface){
        this.RoomInterface = roomInterface;
        return this;
    }

    public String getTag(){
        return Tag;
    }

    public RoomInterface getRoomInterface(){
        return RoomInterface;
    }
}
