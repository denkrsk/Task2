package ru.stepup.task;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Date;

public class Start {
    public static void main(String[] args) throws InterruptedException {
        // Press Alt+Enter with your caret at the highlighted text to see how


        Currency currr = new Currency();
        Getable acc = UtilsCashe.cashe(new Account("Vasia"));
        System.out.println(acc.getName());
        System.out.println(acc.getName());
        acc.setName("Dima");
        acc.getName();
        System.out.println(acc.getName());

        Instant inst= Instant.now();
//        System.out.println("cur = " + inst.getLong(ChronoField.MILLI_OF_SECOND));
        long curTime = inst.toEpochMilli();
        System.out.println("cur = " + inst.toString());
        System.out.println("cur = " + inst.plusMillis(1000).toString());
        Thread.sleep(1000);

        inst = Instant.now();
        System.out.println("cur = " + inst.toString());
        long curTime2 = inst.toEpochMilli();
        System.out.println("cur = " + curTime2);
        System.out.println(curTime2 - curTime);

//        System.out.println(acc.getName());

    }
}
