package raft.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.DelayLock;
import raft.NodeState;
import raft.Raft;
import raft.Utils;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class FollowerTask extends StateTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(FollowerTask.class);

    private final Raft raft;
    private final long heartbeatTimeout;

    private final DelayLock delayLock = new DelayLock();

    public FollowerTask(Raft raft) {
        this.raft = raft;
        this.heartbeatTimeout = raft.getConfig().getHeartbeatTimeout();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        stateProcess();
        while (true) {
            NodeState nodeState = takeState();
            if (nodeState == NodeState.FOLLOWER) {
                stateProcess();
            }
        }
    }

    private void stateProcess() {
        try {
            long hbTimeout = heartbeatTimeout;

            while(true) {
                if (raft.getState() != NodeState.FOLLOWER) {
                    return;
                }
                long delayTime = System.currentTimeMillis() - raft.getLastContact();
                if (delayTime > hbTimeout) {
                    log.info("raft follower timeout {}ms", System.currentTimeMillis() - raft.getLastContact());
                    break;
                }
                hbTimeout = heartbeatTimeout + Utils.randomTimeout(heartbeatTimeout);
                delayLock.delay(hbTimeout, TimeUnit.MILLISECONDS);
                log.info("raft follower waited for {}ms", hbTimeout);

            }
            // 随机心跳超时变为候选者
            raft.setLeader(null);
            raft.setState(NodeState.CANDIDATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
