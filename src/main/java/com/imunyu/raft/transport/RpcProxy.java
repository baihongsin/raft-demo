package com.imunyu.raft.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class RpcProxy implements InvocationHandler {

    private final RpcClient clientManager;

    public RpcProxy(RpcClient clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            switch (name) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                default:
                    throw new IllegalStateException(String.valueOf(method));
            }
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        String name = method.getDeclaringClass().getName();
        List<String> serverList = clientManager.getServerList(name);
        if (serverList == null) {
            throw new RpcException("not found server for :#" + method.getName());
        }
        RpcClientInvoker invoker = clientManager.select(serverList);
        if (invoker == null) {
            throw new RpcException("not found invokers for :#" + method.getName());
        }
        return invoker.invoke(
                method.getDeclaringClass().getName(),
                method.getName(),
                parameterTypes,
                args);
    }
}
