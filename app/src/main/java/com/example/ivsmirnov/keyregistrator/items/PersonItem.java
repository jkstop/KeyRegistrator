package com.example.ivsmirnov.keyregistrator.items;


import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 22.12.2015.
 */
public class PersonItem {
    private String Lastname = Values.EMPTY;
    private String Firstname = Values.EMPTY;
    private String Midname = Values.EMPTY;
    private String Division = Values.EMPTY;
    private String Sex = "М";
    private String PhotoPreview;
    private String PhotoOriginal;
    private String RadioLabel = Values.EMPTY;

    public boolean isEmpty(){
        return Lastname.equals(Values.EMPTY) &&
                Firstname.equals(Values.EMPTY) &&
                Midname.equals(Values.EMPTY) &&
                Division.equals(Values.EMPTY) &&
                RadioLabel.equals(Values.EMPTY);
    }

    public PersonItem setLastname (String lastname){
        this.Lastname = lastname;
        return this;
    }

    public String getLastname(){
        return Lastname;
    }

    public PersonItem setFirstname (String firstname){
        this.Firstname = firstname;
        return this;
    }

    public String getFirstname(){
        return Firstname;
    }

    public PersonItem setMidname (String midname){
        this.Midname = midname;
        return this;
    }

    public String getMidname(){
        return Midname;
    }

    public PersonItem setDivision (String division){
        this.Division = division;
        return this;
    }

    public String getDivision(){
        return Division;
    }

    public PersonItem setSex (String sex){
        this.Sex = sex;
        return this;
    }

    public String getSex(){
        return Sex;
    }

    public PersonItem setPhotoPreview (String photoPreview){
        this.PhotoPreview = photoPreview;
        return this;
    }

    public String getPhotoPreview(){
        return PhotoPreview;
    }

    public PersonItem setPhotoOriginal (String photoOriginal){
        this.PhotoOriginal = photoOriginal;
        return this;
    }

    public String getPhotoOriginal(){
        return PhotoOriginal;
    }

    public PersonItem setRadioLabel (String radioLabel){
        this.RadioLabel = radioLabel;
        return this;
    }

    public String getRadioLabel(){
        return RadioLabel;
    }
}
