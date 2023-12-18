import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import ru.stepup.task.*;

import java.time.Instant;
import java.util.HashMap;

public class Test {
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка имени на пустое значение")
    public void  testName(){
        Account acc = new Account("Vasia");
        Assertions.assertTrue(!(acc.getName() == null | acc.getName().equals("")));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Проверка имени на не корректные значение")
    public void  testIncName(){
        Account acc;
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Account(""));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Проверка баланса валюты на корректные значение")
    public void  testCurrenc(){
        Account acc = new Account("Vasia");;
        Currency currr = new Currency();
        acc.setCurrency(currr, 100);
        currr.setCurCurrency(2);
        acc.setCurrency(currr, 990);
        acc.setCurrency(currr, 888);
    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка баланса валюты на не корректные значение")
    public void  testIncCurrenc(){
        Account acc = new Account("Vasia");;
        Currency currr = new Currency();

        Assertions.assertThrows(IllegalArgumentException.class, () -> acc.setCurrency(currr, -100));
    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка undo")
    public void  testUndo(){
        Account acc = new Account("Denis");
        String str = acc.getName();
        acc.setName("Vasia");
        acc = acc.undo();
        Assertions.assertTrue(acc.getName().equals(str));

    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка undo, если отменять нечего")
    public void  testUndoInc(){
        Account acc = new Account("Vasia");;
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> acc.undo());
    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка Copy")
    public void  testCopy(){
        Account acc = new Account("Denis");
        String tmp = acc.getName();
        Save<Account> save =acc.save();
        Currency currr = new Currency();
        acc.setName("Vasia");
        acc.setCurrency(currr, 100);
        currr.setCurCurrency(2);
        acc.setCurrency(currr, 990);
        acc.setCurrency(currr, 888);
        save.restore();

        Assertions.assertEquals(tmp, acc.getName());

    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка Copy на не равно")
    public void  testnotCopy(){
        Account acc = new Account("Denis");
        String tmp = acc.getName();
        Save<Account> save =acc.save();
        Currency currr = new Currency();
        acc.setName("Vasia");
        acc.setCurrency(currr, 100);
        currr.setCurCurrency(2);
        acc.setCurrency(currr, 990);
        acc.setCurrency(currr, 888);
        Assertions.assertFalse(Boolean.parseBoolean(tmp), acc.getName());

    }

    @org.junit.jupiter.api.Test
    @DisplayName("Проверка UtilsCash на корректность")
    public void  testUtilsCash() throws ClassNotFoundException {
        Getable acc = UtilsCashe.cashe(new Account("Vasia"));
        acc.getName();
        acc.setName("Dima");
        String tmp = acc.getName();
        Assertions.assertEquals(tmp, acc.getName());

    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка UtilsCash на корректность кеша")
    public void  testUtilsCashEq() throws ClassNotFoundException {
        Getable acc = UtilsCashe.cashe(new Account("Vasia"));
        Currency currr = new Currency();
        acc.setCurrency(currr, 100);
        currr.setCurCurrency(2);
        acc.setCurrency(currr, 990);
        Account accTrue = new Account("Vasia");
        currr.setCurCurrency(1);
        accTrue.setCurrency(currr, 100);
        currr.setCurCurrency(2);
        accTrue.setCurrency(currr, 990);
        HashMap<String, Integer> tmpBal = accTrue.getCurBalance();
        //Здесь сравниваем разные значения
        Assertions.assertNotSame(accTrue.getCurBalance(), accTrue.getCurBalance());
        //Тут из кеша приходит один и тотже объект
        Assertions.assertTrue(acc.getCurBalance()== acc.getCurBalance());
    }
    @org.junit.jupiter.api.Test
    @DisplayName("Проверка UtilsCash очистка кеша")
    public void  testUtilsCashCl() throws InterruptedException {
        Instant inst = Instant.now();
        Getable acc = UtilsCashe.cashe(new Account("Vasia"));
        for (int i =0; i < 40; i++){
            acc.setName("Vasia" + i);
        }
//        время работы без очистки кеша
        long firstTime = Instant.now().toEpochMilli() - inst.toEpochMilli();
//        ждем 1000 мсек и заполняем кеш, чтоб сработала очистка
        Thread.sleep(1000);
        inst = Instant.now();
        for (int i =0; i < 40; i++){
            acc.setName("Vasia" + i);
        }
//        System.out.println( Instant.now().toEpochMilli() - inst.toEpochMilli());

        Assertions.assertTrue(firstTime >= (Instant.now().toEpochMilli() - inst.toEpochMilli()));
    }
}


