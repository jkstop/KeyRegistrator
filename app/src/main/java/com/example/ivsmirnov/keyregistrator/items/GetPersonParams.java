package com.example.ivsmirnov.keyregistrator.items;

import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;

/**
 * Created by ivsmirnov on 21.02.2016.
 */
public class GetPersonParams{
    private String personTag;
    private int personLocation;
    private int personPhotoDimension;
    private DataBaseFavorite dataBaseFavorite;
    private ImageView personImageView, accessImageView;
    private TextView personLastname, personFirstname, personMidname, personDivision;
    private CardView personCard;
    private boolean isFreeUser;

    public GetPersonParams setFreeUser(boolean isFreeUser){
        this.isFreeUser = isFreeUser;
        return this;
    }

    public GetPersonParams setPersonCard(CardView personCard){
        this.personCard = personCard;
        return this;
    }

    public GetPersonParams setAccessImageView(ImageView accessImageView){
        this.accessImageView = accessImageView;
        return this;
    }

    public GetPersonParams setPersonTag(String tag){
        this.personTag = tag;
        return this;
    }

    public GetPersonParams setPersonLocation(int personLocation){
        this.personLocation = personLocation;
        return this;
    }

    public GetPersonParams setPersonPhotoDimension(int personPhotoDimension){
        this.personPhotoDimension = personPhotoDimension;
        return this;
    }

    public GetPersonParams setDatabase(DataBaseFavorite database){
        this.dataBaseFavorite = database;
        return this;
    }

    public GetPersonParams setPersonImageView(ImageView imageView){
        this.personImageView = imageView;
        return this;
    }

    public GetPersonParams setPersonLastname(TextView personLastname){
        this.personLastname = personLastname;
        return this;
    }

    public GetPersonParams setPersonFirstname(TextView personFirstname){
        this.personFirstname = personFirstname;
        return this;
    }
    public GetPersonParams setPersonMidname(TextView personMidname){
        this.personMidname = personMidname;
        return this;
    }

    public GetPersonParams setPersonDivision(TextView personDivision){
        this.personDivision = personDivision;
        return this;
    }

    public boolean getFreeUser(){
        return isFreeUser;
    }

    public String getPersonTag(){
        return personTag;
    }

    public int getPersonLocation(){
        return personLocation;
    }

    public int getPersonPhotoDimension(){
        return personPhotoDimension;
    }

    public DataBaseFavorite getDataBaseFavorite(){
        return dataBaseFavorite;
    }

    public ImageView getPersonImageView(){
        return personImageView;
    }

    public TextView getPersonLastname(){
        return personLastname;
    }

    public TextView getPersonFirstname(){
        return personFirstname;
    }

    public TextView getPersonMidname(){
        return personMidname;
    }

    public TextView getPersonDivision(){
        return personDivision;
    }

    public CardView getPersonCard(){
        return personCard;
    }

    public ImageView getAccessImageView(){
        return accessImageView;
    }

}