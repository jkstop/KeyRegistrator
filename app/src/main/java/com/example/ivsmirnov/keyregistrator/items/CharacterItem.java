package com.example.ivsmirnov.keyregistrator.items;

/**
 * Created by ivsmirnov on 22.02.2016.
 */
public class CharacterItem {

    private String mCharacter;
    private boolean isSelected;

    public CharacterItem setCharacter(String character){
        this.mCharacter = character;
        return this;
    }

    public CharacterItem setSelection(boolean selection){
        this.isSelected = selection;
        return this;
    }

    public String getCharacter(){
        return mCharacter;
    }

    public boolean getSelection(){
        return isSelected;
    }
}
