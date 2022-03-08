package com.imunyu.raft.tests;


import com.imunyu.raft.transport.RpcClient;
import com.imunyu.raft.transport.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationTests {

    public static final Logger log = LoggerFactory.getLogger(ApplicationTests.class);

    interface IGreeter {
        void greet();
    }

    static class Greeter implements IGreeter {

        @Override
        public void greet() {
            log.info("greet");
        }
    }

    public static void main(String[] args) {

        RpcServer rpcServer = new RpcServer(8080);
        rpcServer.addService(new Greeter());
        rpcServer.start();
        RpcClient rpcClient = new RpcClient();
        IGreeter greeter = rpcClient.registerNode(IGreeter.class, "127.0.0.1:8080");
        boolean flag = true;
        long secs = 2;
        while (flag) {
            greeter.greet();
            secs--;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (secs <= 0) {
                flag = false;
            }
        }

        rpcClient.shutdown();
        rpcServer.shutdown();

    }

}
