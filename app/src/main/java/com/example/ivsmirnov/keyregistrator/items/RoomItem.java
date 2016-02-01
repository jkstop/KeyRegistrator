package com.example.ivsmirnov.keyregistrator.items;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivsmirnov on 11.01.2016.
 */
public class RoomItem implements Parcelable{
    public String Auditroom;
    public int Status;
    public int Access;
    public long PositionInBase;
    public String LastVisiter;
    public String Tag;
    public String Photo;

    public RoomItem (String auditroom, int status, int access, long positionInBase, String lastVisiter, String tag, String photo){
        this.Auditroom = auditroom;
        this.Status = status;
        this.Access = access;
        this.PositionInBase = positionInBase;
        this.LastVisiter = lastVisiter;
        this.Tag = tag;
        this.Photo = photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Auditroom);
        dest.writeInt(Status);
        dest.writeInt(Access);
        dest.writeLong(PositionInBase);
    }
}
