package ru.stepup.task;

import java.util.ArrayList;

public class Currency {
    public ArrayList<String> listCurrency = new ArrayList<String>();
    public String curCurrency;

    public Currency() {
        listCurrency.add("RUB");
        listCurrency.add("USD");
        listCurrency.add("EUR");
        this.curCurrency = listCurrency.get(0);
    }

    public void setCurCurrency(int i) {
        if (i < this.listCurrency.size()) this.curCurrency = this.listCurrency.get(i);
    }
}
