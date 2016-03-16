package com.example.ivsmirnov.keyregistrator.interfaces;

import android.graphics.Bitmap;

import com.google.android.gms.auth.UserRecoverableAuthException;

/**
 * загрузка информации об аккаунте
 */

public interface GetAccountInterface {
    void onUserRecoverableAuthException(UserRecoverableAuthException e);
    void onChangeAccount();
    void onAccountImageLoaded(Bitmap bitmap);
}
