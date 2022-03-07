package com.imunyu.raft.transport;

import java.util.Set;

public class ServiceNode {

    private String serviceKey;

    private Set<String> serviceList;

    public ServiceNode(String serviceKey, Set<String> serviceList) {
        this.serviceKey = serviceKey;
        this.serviceList = serviceList;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public Set<String> getServiceList() {
        return serviceList;
    }

    public void setServiceList(Set<String> serviceList) {
        this.serviceList = serviceList;
    }
}
