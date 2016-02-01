package com.example.ivsmirnov.keyregistrator.items;

/**
 * Created by ivsmirnov on 28.01.2016.
 */
public class AccountItem {

    public String Lastname;
    public String Firstname;
    public String Email;
    public String Photo;
    public String AccountID;

    public AccountItem (String lastname, String firstname, String email, String photo, String id){
        this.Lastname = lastname;
        this.Firstname = firstname;
        this.Email = email;
        this.Photo = photo;
        this.AccountID = id;
    }
}
