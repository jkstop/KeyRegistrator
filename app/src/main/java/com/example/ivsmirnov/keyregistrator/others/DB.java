package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;

/**
 * Created by ivsmirnov on 01.03.2016.
 */
public class DB {

    private Context mContext;
    private DataBaseJournal DataBaseJournal;
    private DataBaseFavorite DataBaseFavorite;
    private DataBaseRooms DataBaseRooms;
    private DataBaseAccount DataBaseAccount;

    public DB (Context context){
        this.mContext = context;
        this.DataBaseJournal = new DataBaseJournal(mContext);
        this.DataBaseFavorite = new DataBaseFavorite(mContext);
        this.DataBaseRooms = new DataBaseRooms(mContext);
        this.DataBaseAccount = new DataBaseAccount(mContext);
    }

    public DataBaseJournal getDataBaseJournal(){
        if (DataBaseJournal!=null){
            return DataBaseJournal;
        } else {
            return new DataBaseJournal(mContext);
        }
    }

    public DataBaseFavorite getDataBaseFavorite(){
        if (DataBaseFavorite!=null){
            return DataBaseFavorite;
        } else {
            return new DataBaseFavorite(mContext);
        }
    }

    public DataBaseRooms getDataBaseRooms(){
        if (DataBaseRooms != null){
            return DataBaseRooms;
        } else {
            return new DataBaseRooms(mContext);
        }
    }

    public DataBaseAccount getDataBaseAccount(){
        if (DataBaseAccount != null){
            return DataBaseAccount;
        } else {
            return new DataBaseAccount(mContext);
        }
    }

}
