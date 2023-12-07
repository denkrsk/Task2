package ru.stepup.task;

import java.util.HashMap;

public interface Getable {
    String getName();
    HashMap<String, Integer> getCurBalance();
    void setCurrency(Currency nameCur, Integer bal);
    void setName(String name);
}
