package com.example.ivsmirnov.keyregistrator.items;

/**
 * элемент карточки помещения
 */
public class RoomItem {
    private String Auditroom = "";
    private int Status;
    private int Access;
    private long Time = 0;
    private String LastVisiter = "";
    private String Tag = "";
    private int GridHeight = 0;
    private int GridOrient = 0;

    public RoomItem setGridOrient (int gridOrient){
        this.GridOrient = gridOrient;
        return this;
    }

    public int getGridOrient(){
        return GridOrient;
    }

    public RoomItem setGridHeight(int gridHeight){
        this.GridHeight = gridHeight;
        return this;
    }

    public int getGridHeight(){
        return GridHeight;
    }

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
