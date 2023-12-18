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

    public void setCashHistory(HashMap<MethodPar, Pair> cashHistory) throws InterruptedException {
        this.cashHistory = cashHistory;
        System.out.println("Clear");
    }

    public synchronized void casheClear() throws InterruptedException {
        System.out.println("Carbage");
        boolean needCl = false;
        HashMap<MethodPar, Pair> tmpCash = new HashMap<>(this.cashHistory);
        Iterator<Map.Entry<MethodPar, Pair>> itr = tmpCash.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<MethodPar, Pair> entry = itr.next();
            Pair pair = entry.getValue();
            if (pair.timeLife > Instant.now().toEpochMilli()) {
                itr.remove();
                needCl = true;
            }
        }

//Меняем кэш если было что то для удаления
        if (needCl) setCashHistory(tmpCash);
        wait();
    }

    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method tmp = object.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        Cashe cashe = (Cashe) tmp.getAnnotation(Cashe.class);
        Mutator mutator = tmp.getAnnotation(Mutator.class);
        long timeLife = 0;
        Instant inst = Instant.now();
        if (tmp.isAnnotationPresent(Cashe.class)) timeLife = inst.plusMillis(cashe.value()).toEpochMilli();
        if (tmp.isAnnotationPresent(Mutator.class)) timeLife = inst.plusMillis(mutator.value()).toEpochMilli();

//        String methodPar = new MethodPar(method, args).getKeyMP();
        MethodPar methodPar = new MethodPar(method, args);

        if (tmp.isAnnotationPresent(Cashe.class) | tmp.isAnnotationPresent(Mutator.class)) {
            if (this.cashHistory.containsKey(methodPar)) {
                System.out.println("Cash put");
                this.cashHistory.get(methodPar).timeLife = timeLife;
                return this.cashHistory.get(methodPar).obj;


            } else {
                System.out.println("real met");
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
    public String getKeyMP(){
        if (this.lastPar == null) return this.met.getName();

        return this.met.getName() + Arrays.toString(this.lastPar);
    }
    @Override
    public boolean equals(Object o) {
        System.out.println("equals");
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodPar methodPar = (MethodPar) o;
        if (methodPar.lastPar == null & Objects.equals(met, methodPar.met)) return true;
        return Objects.equals(met, methodPar.met) && Arrays.equals(lastPar, methodPar.lastPar);
    }

}