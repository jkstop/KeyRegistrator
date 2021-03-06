package com.example.ivsmirnov.keyregistrator.items;

/**
 * User item
 */
public class PersonItem {
    private String Lastname;
    private String Firstname;
    private String Midname;
    private String Division;
    private String Sex;
    private String PhotoPath;
    private String Photo;
    private String RadioLabel;
    private int AccessType;

    public void clear(){
        Lastname = null;
        Firstname = null;
        Midname = null;
        Division = null;
        Sex = null;
        PhotoPath = null;
        RadioLabel = null;
        AccessType = 0;
    }

    public PersonItem setAccessType (int accessType){
        this.AccessType = accessType;
        return this;
    }

    public int getAccessType(){
        return AccessType;
    }

    public PersonItem setLastname (String lastname){
        this.Lastname = lastname;
        return this;
    }

    public String getLastname(){
        if (Lastname == null) Lastname = "";
        return Lastname;
    }

    public PersonItem setFirstname (String firstname){
        this.Firstname = firstname;
        return this;
    }

    public String getFirstname(){
        if (Firstname == null) Firstname = "";
        return Firstname;
    }

    public PersonItem setMidname (String midname){
        this.Midname = midname;
        return this;
    }

    public String getMidname(){
        if (Midname == null) Midname = "";
        return Midname;
    }

    public PersonItem setDivision (String division){
        this.Division = division;
        return this;
    }

    public String getDivision(){
        if (Division == null) Division = "";
        return Division;
    }

    public PersonItem setSex (String sex){
        this.Sex = sex;
        return this;
    }

    public String getSex(){
        if (Sex!=null) return Sex;
        else return "М";

    }

    public PersonItem setPhotoPath(String path){
       this.PhotoPath = path;
       return this;
    }

    public String getPhotoPath (){
        return PhotoPath;
    }

    public PersonItem setPhoto (String photo){
        this.Photo = photo;
        return this;
    }

    public String getPhoto (){
        return Photo;
    }

    public PersonItem setRadioLabel (String radioLabel){
        this.RadioLabel = radioLabel;
        return this;
    }

    public String getRadioLabel(){
        return RadioLabel;
    }
}
