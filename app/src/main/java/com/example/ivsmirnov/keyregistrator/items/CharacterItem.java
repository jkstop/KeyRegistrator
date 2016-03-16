package com.example.ivsmirnov.keyregistrator.items;

/**
 * элемент поиска пользователей - первая буква фамилии
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
