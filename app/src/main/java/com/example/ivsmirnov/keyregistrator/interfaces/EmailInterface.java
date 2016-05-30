package com.example.ivsmirnov.keyregistrator.interfaces;

import android.view.View;

/**
 * добавление/удаление вложений и адресатов для рассылки
 */
public interface EmailInterface {
    void onAddRecepient(View v, int position, int view_id);
    void onDeleteRecepient(int position, int view_id);
    void onDeleteAttachment(int position, int view_id);
    void onAddAttachment();
}
