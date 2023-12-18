package ru.stepup.task;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Date;

public class Start {
    public static void main(String[] args) throws InterruptedException {
        // Press Alt+Enter with your caret at the highlighted text to see how


        Getable acc = UtilsCashe.cashe(new Account("Vasia"));
        System.out.println(acc.getName());
        Thread.sleep(1000);

        System.out.println(acc.getName());
        acc.setName("Dima");
        acc.getName();
        System.out.println(acc.getName());


        Thread.sleep(1000);


        System.out.println(acc.getName());

    }
}
