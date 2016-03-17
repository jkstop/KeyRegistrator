package com.example.ivsmirnov.keyregistrator.items;


/**
 * элемент бокового меню навигации
 */
public class NavigationItem {

    private String mText;
    private int mDraw;
    private boolean mSeparator;
    private boolean mSelected = false;

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

    public NavigationItem setSelected(boolean selected){
        this.mSelected = selected;
        return this;
    }

    public boolean getSelected(){
        return mSelected;
    }

    public String getText(){
        return mText;
    }

    public int getDraw(){
        return mDraw;
    }

    public boolean getSeparator(){
        return mSeparator;
    }
}
