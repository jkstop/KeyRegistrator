package com.example.ivsmirnov.keyregistrator.items;

/**
 * Created by ivsmirnov on 03.02.2016.
 */
public class BaseWriterParams {

    private String personTag;
    private String auditroom;
    private int accessType;


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
