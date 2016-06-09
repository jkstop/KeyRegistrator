package com.example.ivsmirnov.keyregistrator.items;

/**
 * элемент журнала
 */
public class JournalItem {
    private String AccountID;
    private String Auditroom = "";
    private long TimeIn;
    private long TimeOut;
    private int AccessType;
    private String PersonPhoto;
    private String PersonPhotoPath;
    private String PersonInitials;
    private String PersonTag;

    public JournalItem setPersonTag (String tag){
        this.PersonTag = tag;
        return this;
    }

    public String getPersonTag(){
        return PersonTag;
    }

    public JournalItem setPersonInitials (String initials){
        this.PersonInitials = initials;
        return this;
    }

    public String getPersonInitials(){
        return PersonInitials;
    }


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

    public JournalItem setPersonPhoto (String personPhoto){
        this.PersonPhoto = personPhoto;
        return this;
    }

    public String getPersonPhoto(){
        return PersonPhoto;
    }
}
