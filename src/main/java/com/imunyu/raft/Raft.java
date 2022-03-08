package com.imunyu.raft;

import com.imunyu.raft.rpc.RpcHandler;
import com.imunyu.raft.rpc.RpcHandlerImpl;
import com.imunyu.raft.transport.RpcClient;
import com.imunyu.raft.transport.RpcExposer;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Raft {

    public static void main(String[] args) {

        RpcExposer rpcExposer = new RpcExposer("127.0.0.1", 8080);
        rpcExposer.addService(new RpcHandlerImpl());
        rpcExposer.start();

        RpcClient rpcClient = new RpcClient();
        RpcHandler rpcHandler = rpcClient.registerNode(RpcHandler.class, "127.0.0.1:8080");
        rpcHandler.appendEntries(null);
        rpcHandler.requestVote(null);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String in = scanner.nextLine();
                if ("1".equals(in)) {
                    rpcHandler.requestVote(null);
                } else if("2".equals(in)) {
                    rpcHandler.appendEntries(null);
                } else {
                    System.out.println(in);
                }
            }
        }).start();
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
