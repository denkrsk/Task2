package ru.stepup.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UtilsCashe {
    Account account;

    public static <T> T cashe(T obj) {

        ClassLoader objClassloader = obj.getClass().getClassLoader();

        Class[] interfaces = obj.getClass().getInterfaces();
        CashHandler cashHandler = new CashHandler(obj);

        Thread thread = new Thread(cashHandler, "Clear cash");
        thread.setDaemon(true);
        thread.start();

        T proxyObj = (T) Proxy.newProxyInstance(objClassloader, interfaces, cashHandler);

        return proxyObj;

    }

}

class CashHandler implements InvocationHandler, Runnable {
    private Object object;

    public HashMap<MethodPar, Pair> cashHistory = new HashMap<>();

    public CashHandler(Object object) {
        this.object = object;
    }

    public synchronized void setCashHistory(HashMap<MethodPar, Pair> cashHistory) throws InterruptedException {
        this.cashHistory = cashHistory;
        wait();
    }

    public void casheClear() throws InterruptedException {
        boolean needCl = false;
        HashMap<MethodPar, Pair> tmpCash = new HashMap<>(this.cashHistory);
        Iterator<Map.Entry<MethodPar, Pair>> itr = tmpCash.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<MethodPar, Pair> entry = itr.next();
            Pair pair = entry.getValue();
            if (pair.timeLife < Instant.now().toEpochMilli()) {
                itr.remove();
                needCl = true;
            }
        }
//Меняем кэш если было что то для удаления
        if (needCl) setCashHistory(tmpCash);
    }

    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method tmp = object.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        Cashe cashe = (Cashe) tmp.getAnnotation(Cashe.class);
        Mutator mutator = tmp.getAnnotation(Mutator.class);
        long timeLife = 0;
        Instant inst = Instant.now();
        if (tmp.isAnnotationPresent(Cashe.class)) timeLife = inst.plusMillis(cashe.value()).toEpochMilli();
        if (tmp.isAnnotationPresent(Mutator.class)) timeLife = inst.plusMillis(mutator.value()).toEpochMilli();

        MethodPar methodPar = new MethodPar(method, args);

        if (tmp.isAnnotationPresent(Cashe.class) | tmp.isAnnotationPresent(Mutator.class)) {
            if (this.cashHistory.containsKey(methodPar)) {
                this.cashHistory.get(methodPar).timeLife = timeLife;
                return this.cashHistory.get(methodPar).obj;


            } else {
                this.cashHistory.put(methodPar, new Pair(method.invoke(object, args), timeLife));
                notify();
                return this.cashHistory.get(methodPar).obj;
            }
        }
//        if (tmp.isAnnotationPresent(Mutator.class)) {
////            this.cashHistory.clear();
//            System.out.println("Mutator met");
//            inst = Instant.now();
//            this.cashHistory.put(idx, new Pair(method.invoke(object, args), inst.plusMillis(mutator.value()).toEpochMilli()));
//            return method.invoke(object, args);
//        }
        return method.invoke(object, args);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                casheClear();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Pair {
    Object obj;
    long timeLife;

    public Pair(Object obj, long timeLife) {
        this.obj = obj;
        this.timeLife = timeLife;
    }

}

class MethodPar {
    Method met;
    Object[] lastPar;

    public MethodPar(Method met, Object[] lastPar) {
        this.met = met;
        this.lastPar = lastPar;
    }

    public String getKeyMP() {
        if (this.lastPar == null) return this.met.getName();

        return this.met.getName() + Arrays.toString(this.lastPar);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(met);
        result = 31 * result + Arrays.hashCode(lastPar);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodPar methodPar = (MethodPar) o;
        if (methodPar.lastPar == null && this.lastPar == null) return Objects.equals(met, methodPar.met);
        return Objects.equals(met, methodPar.met) && Arrays.equals(lastPar, methodPar.lastPar);
    }

}