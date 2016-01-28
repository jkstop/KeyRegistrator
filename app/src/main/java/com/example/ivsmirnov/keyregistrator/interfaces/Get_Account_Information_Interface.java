package com.example.ivsmirnov.keyregistrator.interfaces;

import com.example.ivsmirnov.keyregistrator.custom_views.AccountItem;
import com.google.android.gms.auth.UserRecoverableAuthException;

/**
 * Created by ivsmirnov on 28.01.2016.
 */
public interface Get_Account_Information_Interface {
    void onServerRequest(AccountItem accountItem);
    void onUserRecoverableAuthException(UserRecoverableAuthException e);
}
