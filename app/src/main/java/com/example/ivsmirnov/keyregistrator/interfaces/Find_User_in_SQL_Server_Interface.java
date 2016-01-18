package com.example.ivsmirnov.keyregistrator.interfaces;

import android.util.SparseArray;

import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 07.12.2015.
 */
public interface Find_User_in_SQL_Server_Interface {

    void changeProgressBar(int visibility);
    void updateGrid(ArrayList<PersonItem> items);
}
