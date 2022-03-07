package com.imunyu.raft.transport;

public interface RpcClientInvoker {

    Object invoke(String className, String name, Class<?>[] parameterTypes, Object[] parameters);

}
