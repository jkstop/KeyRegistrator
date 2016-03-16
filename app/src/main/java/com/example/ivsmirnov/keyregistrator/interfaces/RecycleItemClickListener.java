package com.example.ivsmirnov.keyregistrator.interfaces;

import android.view.View;

/**
 * клики в recyclerView. Встроенный onClick() пока не поддерживается
 */
public interface RecycleItemClickListener {
    void onItemClick(View v,int position, int viewID);
    void onItemLongClick(View v, int position, long timeIn);
}
