package ru.stepup.task;

import ru.stepup.payments.mobile.Student;

public class Start {
    public static void main(String[] args) {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
//        System.out.printf("Welcome to Mobile Pay");

        Currency currr = new Currency();
        Getable acc = UtilsCashe.casheU(new Account("Vasia"));
        acc.getName();
        System.out.println(acc.getName());
        acc.setName("Dima");
        acc.getName();
        System.out.println(acc.getName());
        acc.setCurrency(currr, 100);
        currr.setCurCurrency(2);
        acc.setCurrency(currr, 990);
        System.out.println(acc.getCurBalance());
        System.out.println(acc.getCurBalance());
//        acc.getName();

//        System.out.println(acc.getName());

    }
}