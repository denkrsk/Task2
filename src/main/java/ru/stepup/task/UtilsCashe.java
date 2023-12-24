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
    private boolean flCasheClear = false;
    public HashMap<MethodPar, Pair> cashHistory = new HashMap<>();

    public CashHandler(Object object) {
        this.object = object;
    }

    public void casheClear() throws InterruptedException {

        Iterator<Map.Entry<MethodPar, Pair>> itr = cashHistory.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<MethodPar, Pair> entry = itr.next();
            Pair pair = entry.getValue();
            if (pair.timeLife < Instant.now().toEpochMilli()) {
                synchronized (this) {
                    itr.remove();
                }

            }
        }

        flCasheClear = false;

    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
                    synchronized (this) {
                        this.cashHistory.get(methodPar).timeLife = timeLife;
                    }
                    flCasheClear = true;
                    return this.cashHistory.get(methodPar).obj;

                } else {
                    Pair tmpPair = new Pair(method.invoke(object, args), timeLife);
                    synchronized (this) {
                        this.cashHistory.put(methodPar, tmpPair);
                    }
                    flCasheClear = true;
                    return tmpPair.obj;
                }

        }

        return method.invoke(object, args);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                if (flCasheClear)
                    casheClear();

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