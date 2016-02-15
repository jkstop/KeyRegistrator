package com.example.ivsmirnov.keyregistrator.interfaces;

import android.view.View;

/**
 * Created by ivsmirnov on 15.02.2016.
 */
public interface EmailClickItemsInterface {
    void onAddRecepient(View v, int position, int view_id);
    void onDeleteRecepient(int position, int view_id);
    void onDeleteAttachment(int position, int view_id);
}
