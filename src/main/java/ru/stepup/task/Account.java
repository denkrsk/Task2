package ru.stepup.task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

public class Account implements Getable{
    private String name;
    private HashMap<String, Integer> curBalance = new HashMap<>();
    private Deque<Action> stUndo = new ArrayDeque<>();

    public Account(String name) {
        setName(name);
    }
    @Cashe
    public String getName() {
        return name;
    }
    @Setter
    public void setName(String name) {
        if (name == null | name.equals("")) throw new IllegalArgumentException("Имя не может быть пустым.");
        String oldName = this.name;
        stUndo.push(()->this.name = oldName);
        this.name = name;

    }
    @Cashe
    public HashMap<String, Integer> getCurBalance() {
        return new HashMap<>(curBalance);
    }
    @Setter
    public void setCurrency(Currency nameCur, Integer bal) {

        if (bal < 0) throw new IllegalArgumentException("Баланс валюты не может быть отрицательным.");
            if (curBalance.containsKey(nameCur.curCurrency)) {
                Integer old_bal = this.curBalance.get(nameCur.curCurrency);
                stUndo.push(() -> curBalance.put(nameCur.curCurrency, old_bal));
            }
             else stUndo.push(() -> this.curBalance.remove(nameCur));

             this.curBalance.put(nameCur.curCurrency, bal);
    }
    public Account undo() {
        stUndo.pop().run();
        if (stUndo.isEmpty()) throw new IndexOutOfBoundsException("Нет изменений для отмены.");
        return this;
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", currency=" + curBalance +
                '}';
    }

    public Save<Account> save(){
        return new AccountSave();
    }

    private class AccountSave implements Save<Account> {
    private String name;
    private HashMap<String, Integer> curBalance;
        public AccountSave(){
            this.name = Account.this.name;
            this.curBalance = new HashMap<>(Account.this.curBalance);
        }

        @Override
        public void restore() {
            Account.this.name = this.name;
            Account.this.curBalance = new HashMap<>(this.curBalance);

        }
    }
}

