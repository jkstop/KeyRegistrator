package com.example.ivsmirnov.keyregistrator.items;

import java.util.ArrayList;

/**
 * параметры для e-mail рассылки
 */
public class MailParams{

    public String Theme;
    public String Body;
    public ArrayList<String> Recepients;
    public ArrayList<String> Attachments;

    public MailParams setTheme(String theme){
        this.Theme = theme;
        return this;
    }

    public MailParams setBody(String body){
        this.Body = body;
        return this;
    }

    public MailParams setRecepients(ArrayList<String> recepients){
        this.Recepients = recepients;
        return this;
    }

    public MailParams setAttachments(ArrayList<String> attachments){
        this.Attachments = attachments;
        return this;
    }

    public String getTheme(){
        return Theme;
    }

    public String getBody(){
        return Body;
    }

    public ArrayList<String> getRecepients(){
        return Recepients;
    }

    public ArrayList<String> getAttachments(){
        return Attachments;
    }
}