package com.example.ivsmirnov.keyregistrator.items;

import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * класс для получения записи журнала
 */
public class GetJournalParams {

    private long timeIn;
    private TextView TextAuditroom;
    private TextView TextTimeIn;
    private TextView TextTimeOut;
    private ImageView ImagePerson;
    private TextView mTextInitials;
    //private TextView TextLastname;
    //private TextView TextFirstname;
    //private TextView TextMidname;
    private ImageView ImageAccess;
    private CardView Card;

    public GetJournalParams setTextInitials(TextView textInitials){
        this.mTextInitials = textInitials;
        return this;
    }

    public GetJournalParams setTimeIn(long timeIn){
        this.timeIn = timeIn;
        return this;
    }

    public GetJournalParams setCard(CardView cardView){
        this.Card = cardView;
        return this;
    }

    public GetJournalParams setTextAuditroom(TextView textAuditroom){
        this.TextAuditroom = textAuditroom;
        return this;
    }

    public GetJournalParams setTextTimeIn(TextView textTimeIn){
        this.TextTimeIn = textTimeIn;
        return this;
    }

    public GetJournalParams setTextTimeOut(TextView textTimeOut){
        this.TextTimeOut = textTimeOut;
        return this;
    }

    public GetJournalParams setImagePerson(ImageView imagePerson){
        this.ImagePerson = imagePerson;
        return this;
    }

   // public GetJournalParams setTextLastname(TextView textLastname){
   //     this.TextLastname = textLastname;
   //     return this;
   // }

//    public GetJournalParams setTextFirstname(TextView textFirstname){
 //       this.TextFirstname = textFirstname;
  //      return this;
  //  }

    //public GetJournalParams setTextMidname(TextView textMidname){
    //    this.TextMidname = textMidname;
    //    return this;
    //}

    public GetJournalParams setImageAccess(ImageView imageAccess){
        this.ImageAccess = imageAccess;
        return this;
    }

    public CardView getCard(){
        return Card;
    }

    public long getTimeIn(){
        return timeIn;
    }

    public TextView getTextAuditroom(){
        return TextAuditroom;
    }

    public TextView getTextTimeIn(){
        return TextTimeIn;
    }

    public TextView getTextTimeOut(){
        return TextTimeOut;
    }

    public ImageView getImagePerson(){
        return ImagePerson;
    }

    public TextView getTextInitials(){
        return mTextInitials;
    }

   // public TextView getTextLastname(){
   //     return TextLastname;
   // }

    //public TextView getTextFirstname(){
    //    return TextFirstname;
    //}

    //public TextView getTextMidname(){
     //   return TextMidname;
   // }

    public ImageView getImageAccess(){
        return ImageAccess;
    }


}
