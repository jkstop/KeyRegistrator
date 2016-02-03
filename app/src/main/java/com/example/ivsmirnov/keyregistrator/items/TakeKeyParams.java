package com.example.ivsmirnov.keyregistrator.items;

import android.content.Context;

import com.example.ivsmirnov.keyregistrator.interfaces.KeyInterface;

/**
 * Created by ivsmirnov on 03.02.2016.
 */
public class TakeKeyParams {

    private PersonItem personItem;
    private String auditroom;
    private int accessType;
    private KeyInterface keyInterface;

    public TakeKeyParams setPublicInterface(KeyInterface keyInterface){
        this.keyInterface = keyInterface;
        return this;
    }

    public TakeKeyParams setPersonItem (PersonItem personItem){
        this.personItem = personItem;
        return this;
    }

    public TakeKeyParams setAuditroom (String auditroom){
        this.auditroom = auditroom;
        return this;
    }

    public TakeKeyParams setAccessType (int accessType){
        this.accessType = accessType;
        return this;
    }

    public KeyInterface getPublicInterface(){
        return keyInterface;
    }

    public PersonItem getPersonItem(){
        return personItem;
    }

    public String getAuditroom(){
        return auditroom;
    }

    public int getAccessType(){
        return accessType;
    }
}
