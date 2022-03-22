package raft;

import raft.rpc.RpcHandler;
import raft.transport.RpcClient;

public class Node {

    private String serverId;

    private String address;

    private RpcHandler rpcHandler;

    public Node(String serverId, String address) {
        this.serverId = serverId;
        this.address = address;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RpcHandler getRpcHandler() {
        if (rpcHandler == null) {
            RpcClient rpcClient = new RpcClient();
            rpcHandler = rpcClient.createService(RpcHandler.class, getAddress());
        }
        return rpcHandler;
    }

}
