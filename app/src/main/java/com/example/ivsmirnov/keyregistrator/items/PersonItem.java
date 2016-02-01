package com.example.ivsmirnov.keyregistrator.items;

/**
 * Created by ivsmirnov on 22.12.2015.
 */
public class PersonItem {
    public String Lastname;
    public String Firstname;
    public String Midname;
    public String Division;
    public String Sex;
    public String PhotoPreview;
    public String PhotoOriginal;
    public String RadioLabel;

    public PersonItem (String lastname, String firstname, String midname, String division, String sex, String photoPreview, String photoOriginal, String radioLabel){
        this.Lastname = lastname;
        this.Firstname = firstname;
        this.Midname = midname;
        this.Division = division;
        this.Sex = sex;
        this.PhotoPreview = photoPreview;
        this.PhotoOriginal = photoOriginal;
        this.RadioLabel = radioLabel;
    }
}
