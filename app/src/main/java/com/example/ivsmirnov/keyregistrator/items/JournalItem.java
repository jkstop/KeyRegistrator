package com.example.ivsmirnov.keyregistrator.items;

import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * элемент журнала
 */
public class JournalItem {
    private String AccountID;
    private String Auditroom = Values.EMPTY;
    private long TimeIn;
    private long TimeOut;
    private int AccessType;
    private String PersonLastname = Values.EMPTY;
    private String PersonFirstname = Values.EMPTY;
    private String PersonMidname = Values.EMPTY;
    private String PersonPhoto;
    private String PersonPhotoPath;

    public JournalItem setPersonPhotoPath(String photoPath){
        this.PersonPhotoPath = photoPath;
        return this;
    }

    public String getPersonPhotoPath(){
        return PersonPhotoPath;
    }

    public JournalItem setAccountID(String accountID){
        this.AccountID = accountID;
        return this;
    }

    public String getAccountID(){
        return AccountID;
    }

    public JournalItem setAuditroom (String auditroom){
        this.Auditroom = auditroom;
        return this;
    }

    public String getAuditroom (){
        return Auditroom;
    }

    public JournalItem setTimeIn (Long timeIn){
        if (timeIn == null) timeIn = (long) 0;
        this.TimeIn = timeIn;
        return this;
    }

    public JournalItem setTimeIn (String timeIn){
        long time;
        try {
            time = Long.parseLong(timeIn);
        } catch (NumberFormatException e){
            time = (long) 0;
        }
        this.TimeIn = time;
        return this;
    }

    public JournalItem setTimeOut (String timeOut){
        long time;
        try {
            time = Long.parseLong(timeOut);
        } catch (NumberFormatException e){
            time = (long) 0;
        }
        this.TimeOut = time;
        return this;
    }

    public Long getTimeIn(){
        return TimeIn;
    }

    public JournalItem setTimeOut (Long timeOut){
        if (timeOut == null) timeOut = (long)0;
        this.TimeOut = timeOut;
        return this;
    }

    public Long getTimeOut(){
        return TimeOut;
    }

    public JournalItem setAccessType (int accessType){
        this.AccessType = accessType;
        return this;
    }

    public int getAccessType(){
        return AccessType;
    }

    public JournalItem setPersonLastname (String personLastname){
        this.PersonLastname = personLastname;
        return this;
    }

    public String getPersonLastname(){
        return PersonLastname;
    }

    public JournalItem setPersonFirstname (String personFirstname){
        this.PersonFirstname = personFirstname;
        return this;
    }

    public String getPersonFirstname(){
        return PersonFirstname;
    }

    public JournalItem setPersonMidname (String personMidname){
        this.PersonMidname = personMidname;
        return this;
    }

    public String getPersonMidname(){
        return PersonMidname;
    }

    public JournalItem setPersonPhoto (String personPhoto){
        this.PersonPhoto = personPhoto;
        return this;
    }

    public String getPersonPhoto(){
        return PersonPhoto;
    }
}
