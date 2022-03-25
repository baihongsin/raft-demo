package raft.test;

import org.junit.jupiter.api.Test;
import raft.Node;
import raft.Raft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class ExampleTests {


    @Test
    public void TestCluster() throws InterruptedException {
        System.out.println("test raft cluster");
        String clusterAddr = "127.0.0.1:9000,127.0.0.1:9001,127.0.0.1:9002,127.0.0.1:9003";
        String[] addrList = clusterAddr.split(",");
        List<Raft> nodes = new ArrayList<>();
        for (String addr : addrList) {
            addr = addr.toLowerCase(Locale.ROOT).trim();
            Raft raft = Raft.newInstance();
            raft.setAddress(addr);
            raft.start();
            nodes.add(raft);
        }
        for (Raft raft1 : nodes) {
            for (Raft raft2 : nodes) {
                if (raft1 == raft2) {
                    continue;
                }
                raft1.addPeer(new Node(raft2.getId(), raft2.getAddress()));
            }
        }

        Thread.sleep(5000);
        String command = "content test";
        Node leader = nodes.get(0).pushCommand(command);
        if (leader != null) {
            for (Raft r : nodes) {
                if (r.getId().equals(leader.getServerId())) {
                    r.pushCommand(command);
                    r.pushCommand(command);
                    r.pushCommand(command);
                }
            }
        }

        new CountDownLatch(1).await();
    }



}
