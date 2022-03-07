package com.imunyu.raft;

import com.imunyu.raft.transport.RpcClient;
import com.imunyu.raft.transport.RpcHandler;

public class Raft {

    public static void main(String[] args) {

        RpcClient rpcClient = new RpcClient();
        rpcClient.registerNode(RpcHandler.class, "127.0.0.1");

    }
}
