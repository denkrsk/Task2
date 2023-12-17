package ru.stepup.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

public class UtilsCashe {
    Account account;

    public static <T> T cashe(T obj) {

        ClassLoader objClassloader = obj.getClass().getClassLoader();

        Class[] interfaces = obj.getClass().getInterfaces();

        T proxyObj = (T) Proxy.newProxyInstance(objClassloader, interfaces, new CashHandler(obj));
//        Getable gt = (Getable) Proxy.newProxyInstance(acc.getClass().getClassLoader(),
//                acc.getClass().getInterfaces(),
//                new AccountWrapper( acc));
        return proxyObj;

    }

}

class CashHandler implements InvocationHandler {
    private Object object;

    private HashMap<Integer, Pair> cashHistory = new HashMap<>();
    Thread trThread = new Thread(() -> {
        try {
            this.clearCashe();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    });

    public CashHandler(Object object) {
        this.object = object;
    }

    public void clearCashe() throws InterruptedException {
        boolean notFirstNStart = false;
        boolean needCl = false;
        while (true) {
            HashMap<Integer, Pair> tmpCash = new HashMap<>(cashHistory);
            if (notFirstNStart) for (Integer met : tmpCash.keySet()) {
                Pair pair = tmpCash.get(met);
                if (pair.timeLife > Instant.now().toEpochMilli()) {
                    tmpCash.remove(met);
                    needCl = true;
                }
            }

            if (!notFirstNStart) notFirstNStart= true;
//Меняем кэш ес
            if(needCl) setCashHistory(tmpCash);

            needCl = false;

            trThread.suspend();
        }
    }

    public void setCashHistory(HashMap<Integer, Pair> cashHistory) {
        synchronized (cashHistory) {
            this.cashHistory = cashHistory;
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method tmp = object.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        Cashe cashe = (Cashe) tmp.getAnnotation(Cashe.class);
        Mutator mutator = tmp.getAnnotation(Mutator.class);
        long timeLife = 0;
        Instant inst = Instant.now();
        if (trThread.getState().equals(Thread.State.NEW)) trThread.start();
        if (tmp.isAnnotationPresent(Cashe.class)) timeLife = inst.plusMillis(cashe.value()).toEpochMilli();
        if (tmp.isAnnotationPresent(Mutator.class)) timeLife = inst.plusMillis(mutator.value()).toEpochMilli();

        int idx = new MethodPar(method, args).hashCode();

        if (tmp.isAnnotationPresent(Cashe.class) | tmp.isAnnotationPresent(Mutator.class)) {
            if (this.cashHistory.containsKey(idx)) {
                System.out.println("Cash put");
                this.cashHistory.get(idx).timeLife = timeLife;
                trThread.resume();
                return this.cashHistory.get(idx).obj;

            } else {
                System.out.println("real met");
                synchronized (cashHistory) {
                    this.cashHistory.put(idx, new Pair(method.invoke(object, args), timeLife));
                }
                return this.cashHistory.get(idx).obj;
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

    @Override
    public int hashCode() {
        if (lastPar != null) return (met.hashCode() + lastPar.length) * 29;

        return met.hashCode();
    }
}