package com.imunyu.raft;

import com.imunyu.raft.rpc.RpcHandlerImpl;
import com.imunyu.raft.transport.RpcServer;

public class Raft2 {

    public static void main(String[] args) {


        RpcServer rpcServer = new RpcServer("127.0.0.1", 8080);
        rpcServer.addService(new RpcHandlerImpl());
        rpcServer.start();
    }
}
