package com.example.ivsmirnov.keyregistrator.interfaces;

/**
 * Created by ivsmirnov on 13.04.2016.
 */
public interface Updatable{
    void onItemDeleted(int position);
    void onItemChanged(String tag, int position);
}
