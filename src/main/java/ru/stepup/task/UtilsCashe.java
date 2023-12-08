package ru.stepup.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;

public class UtilsCashe {
    Account account;

    public static Getable casheU (Account acc){
        Getable gt = (Getable) Proxy.newProxyInstance(acc.getClass().getClassLoader(),
                acc.getClass().getInterfaces(),
                new AccountWrapper( acc));
        return gt;

    }

}
class AccountWrapper implements InvocationHandler {
    private Account account;

    private HashMap<String, Object> cashHistory = new HashMap<>();

    public AccountWrapper(Account account) {
        this.account = account;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method tmp = account.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (tmp.isAnnotationPresent(Cashe.class)) {
            if (this.cashHistory.containsKey(method.getName())) {

                return this.cashHistory.get(method.getName());
            }
            else{
                this.cashHistory.put(method.getName(), method.invoke(account, args));
                return this.cashHistory.get(method.getName());
            }
        }
        if (tmp.isAnnotationPresent(Setter.class)) {
            this.cashHistory.clear();
            return method.invoke(account, args);
        }
        return method.invoke(account, args);
    }
}