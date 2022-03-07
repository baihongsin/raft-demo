package com.imunyu.raft.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ServiceRegistry {

    public interface RegistryListener {

        void register(String serverKey);

        void unregister(String serverKey);
    }

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

    private final Map<String, Object> serviceMap = new HashMap<>();

    private final List<RegistryListener> registryListeners = new ArrayList<>();

    public void addListener(RegistryListener listener) {
        if (!registryListeners.contains(listener)) {
            registryListeners.add(listener);
        }
    }

    public void register(String serverKey) {
        for (RegistryListener listener : registryListeners) {
            listener.register(serverKey);
        }
    }

    public void unregister(String serverKey) {
        for (RegistryListener listener : registryListeners) {
            listener.unregister(serverKey);
        }
    }


    public void addService(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (Class<?> c : interfaces) {
            serviceMap.put(c.getName(), service);
            log.debug("added service:{}", c.getName());
        }
    }

    public Object getService(String className) {
        return serviceMap.get(className);
    }

    public Set<String> serviceList() {
        return serviceMap.keySet();
    }


}
