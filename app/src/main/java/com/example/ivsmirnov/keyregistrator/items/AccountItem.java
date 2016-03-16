package com.example.ivsmirnov.keyregistrator.items;

/**
 * элемент аккаунта
 */
public class AccountItem {

    private String Lastname;
    private String Firstname;
    private String Email;
    private String Photo;
    private String AccountID;

    public AccountItem setLastname(String lastname){
        this.Lastname = lastname;
        return this;
    }

    public AccountItem setFirstname(String firstname){
        this.Firstname = firstname;
        return this;
    }

    public AccountItem setEmail(String email){
        this.Email = email;
        return this;
    }

    public AccountItem setPhoto(String photo){
        this.Photo = photo;
        return this;
    }

    public AccountItem setAccountID(String accountID){
        this.AccountID = accountID;
        return this;
    }

    public String getLastname(){
        return Lastname;
    }

    public String getFirstname(){
        return Firstname;
    }

    public String getEmail(){
        return Email;
    }

    public String getPhoto(){
        return Photo;
    }

    public String getAccountID(){
        return AccountID;
    }
}
