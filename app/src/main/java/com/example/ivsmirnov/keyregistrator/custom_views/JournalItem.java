package com.example.ivsmirnov.keyregistrator.custom_views;

/**
 * Created by ivsmirnov on 11.01.2016.
 */
public class JournalItem {
    public String Auditroom;
    public Long TimeIn;
    public Long TimeOut;
    public int AccessType;
    public String PersonLastname;
    public String PersonFirstname;
    public String PersonMidname;
    public String PersonPhoto;

    public JournalItem (String auditroom, Long timeIn, Long timeOut, int accessType, String personLastname, String personFirstname, String personMidname, String personPhoto){
        this.Auditroom = auditroom;
        this.TimeIn = timeIn;
        this.TimeOut = timeOut;
        this.AccessType = accessType;
        this.PersonLastname = personLastname;
        this.PersonFirstname = personFirstname;
        this.PersonMidname = personMidname;
        this.PersonPhoto = personPhoto;
    }
}
