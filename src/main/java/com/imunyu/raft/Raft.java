package com.imunyu.raft;

import com.imunyu.raft.rpc.RpcHandler;
import com.imunyu.raft.transport.RpcClient;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Raft {
    static RpcHandler rpcHandler = null;
    static  RpcClient rpcClient = null;
    public static void main(String[] args) {




        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String in = scanner.nextLine();
                if ("1".equals(in)) {
                    rpcClient = new RpcClient();
                    rpcHandler = rpcClient.registerNode(RpcHandler.class, "127.0.0.1:8080");
                    rpcHandler.appendEntries(null);
                    rpcHandler.requestVote(null);
                } else if("2".equals(in)) {
                    if (rpcHandler != null) {
                        rpcHandler.requestVote(null);
                        rpcHandler.appendEntries(null);
                    }

                } else {
                    System.out.println(in);
                    rpcClient.shutdown();
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
