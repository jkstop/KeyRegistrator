package com.example.ivsmirnov.keyregistrator.items;

/**
 * Server conn item
 */
public class ServerConnectionItem {

    private String mServerName;
    private String mUserName;
    private String mUserPassword;

    public ServerConnectionItem setServerName (String serverName){
        this.mServerName = serverName;
        return this;
    }

    public ServerConnectionItem setUserName (String userName){
        this.mUserName = userName;
        return this;
    }

    public ServerConnectionItem setUserPassword (String userPassword){
        this.mUserPassword = userPassword;
        return this;
    }



    public String getServerName(){
        return mServerName;
    }

    public String getUserName(){
        return mUserName;
    }

    public String getUserPassword(){
        return mUserPassword;
    }

}
