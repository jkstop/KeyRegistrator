package com.example.ivsmirnov.keyregistrator.items;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;

/**
 * Параметры для загрузки пользователя
 */
public class GetPersonParams{
    private String personTag;
    private int personLocation;
    private int personPhotoDimension;
    private int personPhotoLocation;
    private ProgressBar personImageLoadProgressBar;
    private FavoriteDB favoriteDB;
    private ImageView personImageView, accessImageView;
    private TextView personLastname, personFirstname, personMidname, personDivision;
    private CardView personCard;
    private boolean isFreeUser;
    private boolean isAnimatedPhoto;
    private AppCompatCheckBox accessTypeContainer;

    public GetPersonParams setPersonImageLoadProgressBar (ProgressBar progressBar){
        this.personImageLoadProgressBar = progressBar;
        return this;
    }

    public ProgressBar getPersonImageLoadProgressBar(){
        return personImageLoadProgressBar;
    }

    public GetPersonParams setPersonPhotoLocation(int photoLocation){
        this.personPhotoLocation = photoLocation;
        return this;
    }

    public int getPersonPhotoLocation(){
        return personPhotoLocation;
    }


    public GetPersonParams setIsAnimatedPhoto(boolean isAnimatedPhoto){
        this.isAnimatedPhoto = isAnimatedPhoto;
        return this;
    }

    public GetPersonParams setAccessTypeContainer(AppCompatCheckBox appCompatCheckBox){
        this.accessTypeContainer = appCompatCheckBox;
        return this;
    }

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

    public GetPersonParams setDatabase(FavoriteDB database){
        this.favoriteDB = database;
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

    public boolean getIsAnimatedPhoto(){
        return isAnimatedPhoto;
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

    public FavoriteDB getFavoriteDB(){
        return favoriteDB;
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

    public AppCompatCheckBox getAccessTypeContainer(){
        return accessTypeContainer;
    }

}