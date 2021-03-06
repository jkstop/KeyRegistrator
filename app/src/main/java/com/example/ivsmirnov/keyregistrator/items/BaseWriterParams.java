package com.example.ivsmirnov.keyregistrator.items;

/**
 * Параметры для записи в базу
 */
public class BaseWriterParams {

    private String personTag;
    private String auditroom;
    private int accessType;
    private long openTime;

    public BaseWriterParams setOpenTime (long openTime){
        this.openTime = openTime;
        return this;
    }

    public long getOpenTime(){
        return openTime;
    }

    public BaseWriterParams setPersonTag (String personTag){
        this.personTag = personTag;
        return this;
    }

    public BaseWriterParams setAuditroom (String auditroom){
        this.auditroom = auditroom;
        return this;
    }

    public BaseWriterParams setAccessType (int accessType){
        this.accessType = accessType;
        return this;
    }

    public String getPersonTag(){
        return personTag;
    }

    public String getAuditroom(){
        return auditroom;
    }

    public int getAccessType(){
        return accessType;
    }
}
