package com.example.ivsmirnov.keyregistrator.items;

/**
 * Created by ivsmirnov on 19.12.2015.
 */
public class NavigationItem {

    public String mText;
    public int mDraw;
    public boolean mSeparator;

    public NavigationItem setText(String itemText){
        this.mText = itemText;
        return this;
    }

    public NavigationItem setIcon(int drawalbe){
        this.mDraw = drawalbe;
        return this;
    }

    public NavigationItem setSeparator(boolean isSeparator){
        this.mSeparator = isSeparator;
        return this;
    }
}
