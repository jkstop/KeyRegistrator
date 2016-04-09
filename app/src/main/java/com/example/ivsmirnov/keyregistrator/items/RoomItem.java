package com.example.ivsmirnov.keyregistrator.items;

import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * элемент карточки помещения
 */
public class RoomItem {
    private String Auditroom = Values.EMPTY;
    private int Status;
    private int Access;
    private long Time = 0;
    private String LastVisiter  =Values.EMPTY;
    private String Tag = Values.EMPTY;
    private String Photo = Values.EMPTY;

    public RoomItem setAuditroom(String auditroom){
        this.Auditroom = auditroom;
        return this;
    }

    public RoomItem setStatus(int status){
        this.Status = status;
        return this;
    }

    public RoomItem setAccessType(int accessType){
        this.Access = accessType;
        return this;
    }

    public RoomItem setTime(long time){
        this.Time = time;
        return this;
    }

    public RoomItem setLastVisiter(String lastVisiter){
        this.LastVisiter = lastVisiter;
        return this;
    }

    public RoomItem setTag(String tag){
        this.Tag = tag;
        return this;
    }


    public String getAuditroom(){
        return Auditroom;
    }

    public int getStatus(){
        return Status;
    }

    public int getAccessType(){
        return Access;
    }

    public long getTime(){
        return Time;
    }

    public String getLastVisiter(){
        return LastVisiter;
    }

    public String getTag(){
        return Tag;
    }

}
