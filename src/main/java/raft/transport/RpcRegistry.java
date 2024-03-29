package raft.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class RpcRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RpcRegistry.class);

    private final Map<String, Object> serviceMap = new HashMap<>();

    public void addService(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (Class<?> c : interfaces) {
            serviceMap.put(c.getName(), service);
            logger.debug("added service:{}", c.getName());
        }
    }

    public Object getService(String className) {
        return serviceMap.get(className);
    }


}
