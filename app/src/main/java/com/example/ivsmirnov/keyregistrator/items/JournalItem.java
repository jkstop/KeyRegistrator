package com.example.ivsmirnov.keyregistrator.items;

import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * элемент журнала
 */
public class JournalItem {
    private String AccountID;
    private String Auditroom = Values.EMPTY;
    private Long TimeIn;
    private Long TimeOut;
    private int AccessType;
    private String PersonLastname = Values.EMPTY;
    private String PersonFirstname = Values.EMPTY;
    private String PersonMidname = Values.EMPTY;
    private String PersonPhoto;

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
        this.TimeIn = timeIn;
        return this;
    }

    public Long getTimeIn(){
        return TimeIn;
    }

    public JournalItem setTimeOut (Long timeOut){
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
