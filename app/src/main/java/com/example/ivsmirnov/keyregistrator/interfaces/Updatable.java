package com.example.ivsmirnov.keyregistrator.interfaces;

/**
 * Created by ivsmirnov on 13.04.2016.
 */
public interface Updatable{
    void onUserDeleted(int position);
    void onUserChanged (String tag, int position);
}
