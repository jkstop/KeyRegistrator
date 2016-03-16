package com.example.ivsmirnov.keyregistrator.interfaces;

import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import java.util.ArrayList;

/**
 * поиск радиометок на сервере
 */
public interface TagSearcherInterface {

    void changeProgressBar(int visibility);
    void updateGrid(ArrayList<String> personTagList);
}
